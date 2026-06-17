-- Phone Repair Service Database Tables
-- Run these SQL commands in your PostgreSQL database (luckyelectronicals)

-- Create phone_repairs table
CREATE TABLE IF NOT EXISTS phone_repairs (
    id SERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    phone_brand VARCHAR(50) NOT NULL,
    phone_model VARCHAR(100) NOT NULL,
    phone_color VARCHAR(30),
    repair_type VARCHAR(100) NOT NULL,
    agreed_amount DECIMAL(10, 2) NOT NULL,
    amount_paid DECIMAL(10, 2) DEFAULT 0.00,
    balance_due DECIMAL(10, 2) DEFAULT 0.00,
    payment_status VARCHAR(20) DEFAULT 'Not Paid',
    date_received TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_completed TIMESTAMP,
    technician_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'Pending', -- Pending, In Progress, Completed, Collected
    parts_used TEXT,
    actual_cost DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster searches
CREATE INDEX IF NOT EXISTS idx_phone_repairs_receipt ON phone_repairs(receipt_number);
CREATE INDEX IF NOT EXISTS idx_phone_repairs_customer ON phone_repairs(customer_name, customer_phone);
CREATE INDEX IF NOT EXISTS idx_phone_repairs_status ON phone_repairs(status);
CREATE INDEX IF NOT EXISTS idx_phone_repairs_date ON phone_repairs(date_received);

-- Optional: Create a view for active repairs (for reports)
CREATE OR REPLACE VIEW active_repairs AS
SELECT * FROM phone_repairs 
WHERE status IN ('Pending', 'In Progress')
ORDER BY date_received DESC;

-- Optional: Create a view for completed repairs ready for pickup
CREATE OR REPLACE VIEW completed_repairs_ready AS
SELECT * FROM phone_repairs 
WHERE status = 'Completed'
ORDER BY date_completed DESC;

-- Optional: Create a view for repair statistics
CREATE OR REPLACE VIEW repair_statistics AS
SELECT 
    DATE(date_received) as repair_date,
    COUNT(*) as total_repairs,
    COUNT(CASE WHEN status = 'Completed' THEN 1 END) as completed,
    COUNT(CASE WHEN status = 'Pending' OR status = 'In Progress' THEN 1 END) as pending,
    ROUND(AVG(agreed_amount), 2) as average_repair_cost,
    SUM(agreed_amount) as total_revenue
FROM phone_repairs
GROUP BY DATE(date_received)
ORDER BY repair_date DESC;
