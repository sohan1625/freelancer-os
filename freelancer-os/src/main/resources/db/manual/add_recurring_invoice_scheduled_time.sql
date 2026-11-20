ALTER TABLE recurring_invoices
    ADD COLUMN IF NOT EXISTS scheduled_time TIME;

UPDATE recurring_invoices
SET scheduled_time = TIME '09:00:00'
WHERE scheduled_time IS NULL;

ALTER TABLE recurring_invoices
    ALTER COLUMN scheduled_time SET DEFAULT TIME '09:00:00';

ALTER TABLE recurring_invoices
    ALTER COLUMN scheduled_time SET NOT NULL;
