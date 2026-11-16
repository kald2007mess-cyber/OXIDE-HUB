-- OXIDE HUB independent database schema
-- SQLite / Cloudflare D1 compatible.
-- Safe, additive initial schema. Contains no DROP, DELETE, or legacy data reset.
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
  user_uuid TEXT PRIMARY KEY,
  public_id TEXT NOT NULL UNIQUE,
  legacy_user_id TEXT UNIQUE,
  points INTEGER NOT NULL DEFAULT 0 CHECK (points >= 0),
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','blocked','pending')),
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS point_ledger (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uuid TEXT NOT NULL REFERENCES users(user_uuid),
  delta INTEGER NOT NULL CHECK (delta <> 0),
  balance_after INTEGER NOT NULL CHECK (balance_after >= 0),
  reason TEXT NOT NULL,
  reference_type TEXT,
  reference_id TEXT,
  admin_telegram_id TEXT,
  created_at INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_point_ledger_user_time
  ON point_ledger(user_uuid, created_at DESC, id DESC);

CREATE TABLE IF NOT EXISTS referrals (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  referrer_uuid TEXT NOT NULL REFERENCES users(user_uuid),
  referred_uuid TEXT NOT NULL UNIQUE REFERENCES users(user_uuid),
  points_awarded INTEGER NOT NULL DEFAULT 0 CHECK (points_awarded >= 0),
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','confirmed','rejected')),
  created_at INTEGER NOT NULL,
  confirmed_at INTEGER
);
CREATE INDEX IF NOT EXISTS idx_referrals_referrer
  ON referrals(referrer_uuid, created_at DESC);

CREATE TABLE IF NOT EXISTS missions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  description TEXT NOT NULL DEFAULT '',
  platform TEXT NOT NULL DEFAULT '',
  reward_points INTEGER NOT NULL DEFAULT 0 CHECK (reward_points >= 0),
  enabled INTEGER NOT NULL DEFAULT 1 CHECK (enabled IN (0,1)),
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS mission_submissions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uuid TEXT NOT NULL REFERENCES users(user_uuid),
  mission_id INTEGER NOT NULL REFERENCES missions(id),
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','approved','rejected')),
  proof_file_key TEXT,
  reviewer_telegram_id TEXT,
  created_at INTEGER NOT NULL,
  reviewed_at INTEGER,
  UNIQUE(user_uuid, mission_id)
);
CREATE INDEX IF NOT EXISTS idx_mission_submissions_status
  ON mission_submissions(status, created_at DESC);

CREATE TABLE IF NOT EXISTS withdrawals (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uuid TEXT NOT NULL REFERENCES users(user_uuid),
  amount_points INTEGER NOT NULL CHECK (amount_points > 0),
  payout_method TEXT NOT NULL,
  payout_details_encrypted TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','approved','rejected','paid')),
  admin_note TEXT,
  created_at INTEGER NOT NULL,
  reviewed_at INTEGER,
  reviewer_telegram_id TEXT
);
CREATE INDEX IF NOT EXISTS idx_withdrawals_status_time
  ON withdrawals(status, created_at DESC);

CREATE TABLE IF NOT EXISTS notifications (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uuid TEXT NOT NULL REFERENCES users(user_uuid),
  type TEXT NOT NULL,
  title TEXT NOT NULL,
  body TEXT NOT NULL DEFAULT '',
  reward_points INTEGER NOT NULL DEFAULT 0 CHECK (reward_points >= 0),
  created_at INTEGER NOT NULL,
  read_at INTEGER,
  claimed_at INTEGER
);
CREATE INDEX IF NOT EXISTS idx_notifications_user_time
  ON notifications(user_uuid, created_at DESC);

CREATE TABLE IF NOT EXISTS support_conversations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uuid TEXT NOT NULL UNIQUE REFERENCES users(user_uuid),
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open','closed')),
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS support_messages (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  conversation_id INTEGER NOT NULL REFERENCES support_conversations(id),
  sender TEXT NOT NULL CHECK (sender IN ('user','admin')),
  message_type TEXT NOT NULL DEFAULT 'text',
  message_text TEXT NOT NULL,
  created_at INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_support_messages_conversation
  ON support_messages(conversation_id, id);

CREATE TABLE IF NOT EXISTS ad_events (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_uuid TEXT NOT NULL REFERENCES users(user_uuid),
  provider TEXT NOT NULL,
  provider_event_id TEXT NOT NULL,
  reward_points INTEGER NOT NULL DEFAULT 0 CHECK (reward_points >= 0),
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','credited','rejected')),
  created_at INTEGER NOT NULL,
  credited_at INTEGER,
  UNIQUE(provider, provider_event_id)
);
CREATE INDEX IF NOT EXISTS idx_ad_events_user_time
  ON ad_events(user_uuid, created_at DESC);

CREATE TABLE IF NOT EXISTS admin_permissions (
  telegram_user_id TEXT NOT NULL,
  permission TEXT NOT NULL,
  active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0,1)),
  created_at INTEGER NOT NULL,
  PRIMARY KEY(telegram_user_id, permission)
);

CREATE TABLE IF NOT EXISTS app_settings (
  setting_key TEXT PRIMARY KEY,
  setting_value TEXT NOT NULL,
  updated_at INTEGER NOT NULL
);

-- Record migration provenance separately from the live user record.
-- public_id must be retained exactly where collision-free.
CREATE TABLE IF NOT EXISTS identity_migrations (
  legacy_user_id TEXT PRIMARY KEY,
  preserved_public_id TEXT NOT NULL UNIQUE,
  new_user_uuid TEXT UNIQUE REFERENCES users(user_uuid),
  migration_status TEXT NOT NULL DEFAULT 'pending'
    CHECK (migration_status IN ('pending','verified','conflict','migrated')),
  checked_at INTEGER,
  migrated_at INTEGER,
  notes TEXT
);
