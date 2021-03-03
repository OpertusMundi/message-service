-- Optional application specific event type
ALTER TABLE messaging.notification ADD IF NOT EXISTS "event_type" character varying NULL;