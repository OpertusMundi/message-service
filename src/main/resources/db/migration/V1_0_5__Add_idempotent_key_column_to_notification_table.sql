ALTER TABLE messaging.notification ADD IF NOT EXISTS "idempotent_key" character varying NULL;

CREATE INDEX IF NOT EXISTS idx_notification_idempotent_key ON messaging.notification USING btree (recipient, idempotent_key);