ALTER TABLE messaging.message DROP CONSTRAINT IF EXISTS fk_message_thread;

ALTER TABLE messaging.message ADD CONSTRAINT fk_message_thread FOREIGN KEY ("thread") 
  REFERENCES messaging.message_thread ("id")
  ON UPDATE NO ACTION ON DELETE CASCADE;
