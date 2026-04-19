-- Migration to add customer name and current order ID to tables
ALTER TABLE restaurant_table ADD COLUMN IF NOT EXISTS customer_name VARCHAR(255);
ALTER TABLE restaurant_table ADD COLUMN IF NOT EXISTS current_order_id VARCHAR(255);
