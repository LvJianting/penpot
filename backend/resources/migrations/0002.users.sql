CREATE TABLE users (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),

  created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  modified_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  deleted_at timestamptz NULL,

  fullname text NOT NULL DEFAULT '',
  username text NOT NULL,
  email text NOT NULL,
  photo text NOT NULL,
  password text NOT NULL,

  lang text NULL,
  theme text NULL,
  is_demo boolean NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS user_attrs (
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,

  created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  modified_at timestamptz NOT NULL DEFAULT clock_timestamp(),

  key text NOT NULL,
  val bytea NOT NULL,

  PRIMARY KEY (key, user_id)
);

CREATE TABLE IF NOT EXISTS tokens (
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token text NOT NULL,

  created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  used_at timestamptz NULL,

  PRIMARY KEY (token, user_id)
);

CREATE TABLE IF NOT EXISTS sessions (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),

  created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  modified_at timestamptz NOT NULL DEFAULT clock_timestamp(),

  user_id uuid REFERENCES users(id) ON DELETE CASCADE,
  user_agent text NULL
);

-- Insert a placeholder system user.

INSERT INTO users (id, fullname, username, email, photo, password)
VALUES ('00000000-0000-0000-0000-000000000000'::uuid,
        'System User',
        '00000000-0000-0000-0000-000000000000',
        'system@uxbox.io',
        '',
        '!');

CREATE UNIQUE INDEX users__username__idx
    ON users (username)
 WHERE deleted_at IS null;

CREATE UNIQUE INDEX users__email__idx
    ON users (email)
 WHERE deleted_at IS null;

CREATE INDEX users__is_demo
    ON users(is_demo)
 WHERE deleted_at IS null
   AND is_demo IS true;

CREATE TRIGGER users__modified_at__tgr
BEFORE UPDATE ON users
   FOR EACH ROW EXECUTE PROCEDURE update_modified_at();

CREATE TRIGGER user_attrs__modified_at__tgr
BEFORE UPDATE ON user_attrs
   FOR EACH ROW EXECUTE PROCEDURE update_modified_at();

