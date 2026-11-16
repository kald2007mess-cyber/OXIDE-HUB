-- Secure app sessions and one-time legacy migration claims.
-- Apply after 0001_initial.sql. No destructive statements.

CREATE TABLE IF NOT EXISTS app_sessions (
  token_hash TEXT PRIMARY KEY,
  user_uuid TEXT NOT NULL REFERENCES users(user_uuid),
  created_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL,
  revoked_at INTEGER
);
CREATE INDEX IF NOT EXISTS idx_app_sessions_user
  ON app_sessions(user_uuid, expires_at);

CREATE TABLE IF NOT EXISTS migration_claims (
  claim_hash TEXT PRIMARY KEY,
  legacy_user_id TEXT NOT NULL,
  expires_at INTEGER NOT NULL,
  used_at INTEGER,
  created_at INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_migration_claims_legacy
  ON migration_claims(legacy_user_id, expires_at);
