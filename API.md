# OXIDE HUB API

Independent Cloudflare Worker API for the native Android app.

## Current endpoints
- `GET /health`: service/database binding check only.
- `GET /api/me`: returns the signed-in user's OX public ID and points.
- `POST /api/auth/migrate`: exchanges a one-time migration claim for a 30-day app session.

## Database setup
1. Create a NEW D1 database named `oxide-hub-data`.
2. Put its database ID into `wrangler.toml`.
3. Apply migrations in order:
   - `wrangler d1 migrations apply oxide-hub-data --remote`
4. Deploy the Worker.

## Security and migration safeguards
- The Android app never receives the legacy cookie-signing secret or Telegram bot token.
- App sessions are random opaque bearer tokens; only SHA-256 hashes are stored.
- Migration claims are one-time and expire. They must only be issued by a trusted legacy-side endpoint after validating the user's existing signed `oxide_visitor` cookie.
- Do not issue claims from the Android app itself and do not expose a public endpoint that accepts arbitrary legacy user IDs.
- Existing accounts are not imported by these files. Export and verify the legacy database first, preserve each exact `user_id`, and ensure the derived `OX-XXXXXXXX` public IDs are collision-free.
- The old website and its D1 database must remain online until account migration and verification are complete.
