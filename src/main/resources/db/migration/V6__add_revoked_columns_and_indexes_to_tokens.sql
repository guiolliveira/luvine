ALTER TABLE password_reset_tokens
ADD COLUMN revoked BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_refresh_token_user_revoked ON refresh_tokens(user_id, revoked);
CREATE INDEX idx_pass_reset_token_user_revoked ON password_reset_tokens(user_id, revoked);