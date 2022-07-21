-- Rename thread column first
DO $$
BEGIN
  IF EXISTS(
    SELECT * FROM information_schema.columns
    WHERE table_schema = 'messaging' and table_name='message' and column_name='thread'
  )
  THEN
    ALTER TABLE messaging.message RENAME COLUMN "thread" TO "thread_key";
  END IF;
END $$;

-- Create message thread table
CREATE SEQUENCE messaging.message_thread_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;
CREATE TABLE    messaging.message_thread
(
  "id"                  integer                     NOT NULL  DEFAULT nextval('messaging.message_thread_id_seq'::regclass),
  "key"                 uuid                        NOT NULL,
  "owner"               uuid                        NOT NULL,
  "count"               integer                     NOT NULL,
  "count_unread"        integer                     NOT NULL,
  "last_message"        integer,
  CONSTRAINT pk_message_thread     PRIMARY KEY (id),
  CONSTRAINT uq_message_thread_owner_key UNIQUE ("owner", "key"),
  CONSTRAINT fk_message_thread_last_message
    FOREIGN KEY ("last_message") REFERENCES messaging.message ("id")
    ON UPDATE NO ACTION ON DELETE SET NULL
);

-- Insert thread data
insert into messaging.message_thread (
  "key", "owner", "count", "count_unread", "last_message"
)
select    "thread_key", "owner", "count", "count_unread", "last_message"
from (
  select    "thread_key",
            "owner",
            count(m) as "count",
            count(m) filter (where m.read = false) as "count_unread",
            max(m.id) as "last_message"
  from      messaging.message m
  group by  "thread_key", "owner"
) messages;

-- Link threads to messages
ALTER TABLE messaging.message ADD IF NOT EXISTS "thread" integer;

UPDATE messaging.message m SET "thread" = (select "id" from messaging.message_thread where "key" = m.thread_key and "owner" = m.owner);

ALTER TABLE messaging.message ALTER COLUMN "thread" SET NOT NULL;

CREATE INDEX idx_message_owner_thread ON "messaging".message USING btree ("owner", "thread");

-- Cleanup
ALTER TABLE messaging.message DROP COLUMN IF EXISTS "thread_key";
