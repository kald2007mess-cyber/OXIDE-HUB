#!/usr/bin/env node
// Read-only audit for an exported legacy visitors JSON file.
// Usage: node scripts/audit-legacy-identities.mjs legacy-visitors.json
// Expected input: an array of rows containing user_id and optionally points/created_at.
// This script NEVER writes to D1 or changes legacy data.

import fs from "node:fs";

const file = process.argv[2];
if (!file) {
  console.error("Usage: node scripts/audit-legacy-identities.mjs <legacy-visitors.json>");
  process.exit(2);
}

const parsed = JSON.parse(fs.readFileSync(file, "utf8"));
const rows = Array.isArray(parsed) ? parsed : parsed.results;
if (!Array.isArray(rows)) {
  throw new Error("Input must be a JSON array or an object with a results array.");
}

const seenPublicIds = new Map();
const seenLegacyIds = new Set();
const conflicts = [];
const report = [];

for (const row of rows) {
  const legacyId = String(row.user_id ?? "").trim();
  if (!legacyId) {
    conflicts.push({ reason: "MISSING_LEGACY_USER_ID", row });
    continue;
  }
  if (seenLegacyIds.has(legacyId)) {
    conflicts.push({ reason: "DUPLICATE_LEGACY_USER_ID", legacy_user_id: legacyId });
    continue;
  }
  seenLegacyIds.add(legacyId);

  const compact = legacyId.replace(/-/g, "");
  if (!/^[0-9a-f]{32}$/i.test(compact)) {
    conflicts.push({ reason: "UNEXPECTED_LEGACY_ID_FORMAT", legacy_user_id: legacyId });
    continue;
  }

  // Preserve the public ID format used by the existing website.
  const publicId = "OX-" + compact.slice(0, 8).toUpperCase();
  const prior = seenPublicIds.get(publicId);
  if (prior) {
    conflicts.push({
      reason: "PUBLIC_ID_COLLISION",
      public_id: publicId,
      legacy_user_ids: [prior, legacyId]
    });
    continue;
  }
  seenPublicIds.set(publicId, legacyId);

  report.push({
    legacy_user_id: legacyId,
    preserved_public_id: publicId,
    points: Number.isFinite(Number(row.points)) ? Number(row.points) : 0,
    created_at: row.created_at ?? null,
    migration_status: "pending"
  });
}

const output = {
  mode: "READ_ONLY_AUDIT",
  source_rows: rows.length,
  valid_unique_identities: report.length,
  conflicts,
  identities: report
};

console.log(JSON.stringify(output, null, 2));
if (conflicts.length) process.exitCode = 1;
