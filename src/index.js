const JSON_HEADERS = {
  "content-type": "application/json; charset=utf-8",
  "cache-control": "no-store"
};

function reply(data, status = 200) {
  return new Response(JSON.stringify(data), { status, headers: JSON_HEADERS });
}

function base64url(bytes) {
  let binary = "";
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary).split("+").join("-").split("/").join("_").replace(/=+$/g, "");
}

async function sha256(value) {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(value));
  return [...new Uint8Array(digest)].map(x => x.toString(16).padStart(2, "0")).join("");
}

function randomToken(bytes = 32) {
  const data = crypto.getRandomValues(new Uint8Array(bytes));
  return base64url(data);
}

function getBearer(request) {
  const header = request.headers.get("authorization") || "";
  return header.startsWith("Bearer ") ? header.slice(7).trim() : "";
}

async function authenticatedUser(request, env) {
  const token = getBearer(request);
  if (!token) return null;
  const tokenHash = await sha256(token);
  const row = await env.DB.prepare(`
    SELECT u.user_uuid, u.public_id, u.points, u.status
    FROM app_sessions s
    JOIN users u ON u.user_uuid = s.user_uuid
    WHERE s.token_hash = ? AND s.revoked_at IS NULL
      AND s.expires_at > ? AND u.status = 'active'
  `).bind(tokenHash, Math.floor(Date.now() / 1000)).first();
  return row || null;
}

async function profile(request, env) {
  const user = await authenticatedUser(request, env);
  if (!user) return reply({ success: false, error: "UNAUTHORIZED" }, 401);
  return reply({
    success: true,
    user: {
      id: user.public_id,
      points: Number(user.points || 0),
      status: user.status
    }
  });
}

async function exchangeMigrationClaim(request, env) {
  if (request.method !== "POST") return reply({ error: "METHOD_NOT_ALLOWED" }, 405);
  const body = await request.json().catch(() => ({}));
  const claim = String(body.claim || "").trim();
  if (claim.length < 20 || claim.length > 200) {
    return reply({ success: false, error: "INVALID_CLAIM" }, 400);
  }

  const claimHash = await sha256(claim);
  const now = Math.floor(Date.now() / 1000);
  const stored = await env.DB.prepare(`
    SELECT legacy_user_id FROM migration_claims
    WHERE claim_hash = ? AND used_at IS NULL AND expires_at > ?
  `).bind(claimHash, now).first();

  if (!stored) return reply({ success: false, error: "CLAIM_EXPIRED_OR_USED" }, 401);

  const user = await env.DB.prepare(`
    SELECT user_uuid, public_id, points, status
    FROM users WHERE legacy_user_id = ? OR user_uuid = ?
  `).bind(stored.legacy_user_id, stored.legacy_user_id).first();

  if (!user || user.status !== "active") {
    return reply({ success: false, error: "LEGACY_ACCOUNT_NOT_MIGRATED" }, 409);
  }

  const sessionToken = randomToken(32);
  const sessionHash = await sha256(sessionToken);
  const expiresAt = now + 60 * 60 * 24 * 30;

  const result = await env.DB.prepare(`
    UPDATE migration_claims SET used_at = ?
    WHERE claim_hash = ? AND used_at IS NULL AND expires_at > ?
  `).bind(now, claimHash, now).run();

  if (Number(result.meta?.changes || 0) !== 1) {
    return reply({ success: false, error: "CLAIM_ALREADY_USED" }, 409);
  }

  await env.DB.prepare(`
    INSERT INTO app_sessions(token_hash, user_uuid, created_at, expires_at)
    VALUES (?, ?, ?, ?)
  `).bind(sessionHash, user.user_uuid, now, expiresAt).run();

  return reply({
    success: true,
    token: sessionToken,
    expiresAt,
    user: { id: user.public_id, points: Number(user.points || 0) }
  });
}

export default {
  async fetch(request, env) {
    try {
      if (!env.DB) return reply({ success: false, error: "DATABASE_NOT_CONFIGURED" }, 503);
      const url = new URL(request.url);
      if (url.pathname === "/health" && request.method === "GET") {
        return reply({ success: true, service: "OXIDE HUB API", database: "configured" });
      }
      if (url.pathname === "/api/me" && request.method === "GET") {
        return await profile(request, env);
      }
      if (url.pathname === "/api/auth/migrate" && request.method === "POST") {
        return await exchangeMigrationClaim(request, env);
      }
      return reply({ success: false, error: "NOT_FOUND" }, 404);
    } catch {
      return reply({ success: false, error: "INTERNAL_SERVER_ERROR" }, 500);
    }
  }
};
