CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  email VARCHAR(50) NOT NULL UNIQUE,
  avatar_url VARCHAR(200),
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  version INTEGER NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_u ON users(email);

CREATE TABLE IF NOT EXISTS authentication_providers (
  id UUID PRIMARY KEY,
  provider_id VARCHAR(50) NOT NULL,
  provider_key VARCHAR(200) NOT NULL REFERENCES users(email) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  version INTEGER NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_auth_prov_id_key_u ON authentication_providers(provider_id, provider_key);

CREATE TABLE IF NOT EXISTS tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  expiry TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_tokens_email_u on tokens(user_id);

CREATE TABLE IF NOT EXISTS password_information (
  id UUID PRIMARY KEY,
  auth_provider_id UUID NOT NULL UNIQUE REFERENCES authentication_providers(id) ON DELETE CASCADE,
  hasher VARCHAR(200) NOT NULL,
  password VARCHAR(200) NOT NULL,
  salt VARCHAR(200),
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pass_info_hasher on password_information(hasher);
