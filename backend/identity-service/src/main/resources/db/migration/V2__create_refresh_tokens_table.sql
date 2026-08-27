CREATE TABLE refresh_tokens
(
  id                   UUID        NOT NULL,
  user_id              UUID        NOT NULL,
  token_hash           VARCHAR(64) NOT NULL,
  expires_at           TIMESTAMPTZ NOT NULL,
  revoked_at           TIMESTAMPTZ,
  replaced_by_token_id UUID,
  created_at           TIMESTAMPTZ NOT NULL,

  CONSTRAINT pk_refresh_tokens
    PRIMARY KEY (id),

  CONSTRAINT uk_refresh_tokens_token_hash
    UNIQUE (token_hash),

  CONSTRAINT fk_refresh_tokens_user
    FOREIGN KEY (user_id)
      REFERENCES users (id)
      ON DELETE CASCADE,

  CONSTRAINT fk_refresh_tokens_replaced_by
    FOREIGN KEY (replaced_by_token_id)
      REFERENCES refresh_tokens (id)
);

CREATE INDEX idx_refresh_tokens_user_id
  ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_expires_at
  ON refresh_tokens (expires_at);
