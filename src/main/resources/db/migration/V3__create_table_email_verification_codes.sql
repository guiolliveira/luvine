ALTER TABLE users
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN last_verification_email_sent_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN verification_email_request_count INTEGER NOT NULL;

CREATE TABLE email_verification_codes(
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    verification_code VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_email_ver_user_id ON email_verification_codes(user_id);
CREATE INDEX idx_email_ver_verification_code ON email_verification_codes(verification_code);
CREATE INDEX idx_email_ver_expires_at ON email_verification_codes(expires_at);