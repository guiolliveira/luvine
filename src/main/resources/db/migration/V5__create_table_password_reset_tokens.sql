ALTER TABLE users
DROP COLUMN cpf,
DROP COLUMN avatar_url;

CREATE TABLE password_reset_tokens(
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    device_info VARCHAR(255) NOT NULL,
    ip_address VARCHAR(55) NOT NULL
);

CREATE INDEX idx_pass_reset_token_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_pass_reset_token_expires_at ON password_reset_tokens(expires_at);