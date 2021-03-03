-- Optional application specific notification data 
ALTER TABLE messaging.notification ADD IF NOT EXISTS "data" jsonb NULL;