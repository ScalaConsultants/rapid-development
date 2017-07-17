CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  email VARCHAR(50) NOT NULL UNIQUE,
  avatar_url VARCHAR(200) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  version INTEGER NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_u ON users(email);

CREATE TABLE IF NOT EXISTS authentication_providers (
  id UUID PRIMARY KEY,
  provider_id VARCHAR(50) NOT NULL,
  provider_key VARCHAR(200) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  version INTEGER NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_auth_prov_id_key_u ON authentication_providers(provider_id, provider_key);

CREATE TABLE IF NOT EXISTS tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL UNIQUE,
  expiry TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_tokens_email_u on tokens(user_id);
