-- LuckyPOS business panel structure upgrade
-- Run this after the existing database setup when you are ready to store the extra fields.

CREATE TABLE IF NOT EXISTS technicians (
    id SERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    phone_number VARCHAR(40),
    email VARCHAR(160),
    address TEXT,
    national_id VARCHAR(80),
    specialty VARCHAR(120),
    emergency_contact VARCHAR(120),
    joining_date DATE,
    status VARCHAR(30) DEFAULT 'Active',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE technicians ADD COLUMN IF NOT EXISTS email VARCHAR(160);
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS national_id VARCHAR(80);
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS specialty VARCHAR(120);
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS emergency_contact VARCHAR(120);
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'Active';
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id INTEGER PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    tpnumber VARCHAR(40),
    email VARCHAR(160),
    address TEXT,
    contact_person VARCHAR(160),
    payment_terms VARCHAR(80),
    tax_id VARCHAR(80),
    status VARCHAR(30) DEFAULT 'Active',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS email VARCHAR(160);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS contact_person VARCHAR(160);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(80);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS tax_id VARCHAR(80);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'Active';
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS customers (
    customer_id SERIAL PRIMARY KEY,
    customer_name VARCHAR(180) NOT NULL,
    phone_number VARCHAR(40),
    invoice_number VARCHAR(80),
    email VARCHAR(160),
    address TEXT,
    customer_type VARCHAR(60) DEFAULT 'Retail',
    credit_limit NUMERIC(14,2) DEFAULT 0,
    status VARCHAR(30) DEFAULT 'Active',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE customers ADD COLUMN IF NOT EXISTS email VARCHAR(160);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS customer_type VARCHAR(60) DEFAULT 'Retail';
ALTER TABLE customers ADD COLUMN IF NOT EXISTS credit_limit NUMERIC(14,2) DEFAULT 0;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'Active';
ALTER TABLE customers ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE employeestbl ADD COLUMN IF NOT EXISTS email VARCHAR(160);
ALTER TABLE employeestbl ADD COLUMN IF NOT EXISTS national_id VARCHAR(80);
ALTER TABLE employeestbl ADD COLUMN IF NOT EXISTS emergency_contact VARCHAR(120);
ALTER TABLE employeestbl ADD COLUMN IF NOT EXISTS salary NUMERIC(14,2) DEFAULT 0;
ALTER TABLE employeestbl ADD COLUMN IF NOT EXISTS department VARCHAR(120);
ALTER TABLE employeestbl ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'Active';
ALTER TABLE employeestbl ADD COLUMN IF NOT EXISTS notes TEXT;

CREATE TABLE IF NOT EXISTS invoices (
    invoice_id SERIAL PRIMARY KEY,
    invoice_number VARCHAR(80) UNIQUE NOT NULL,
    customer_name VARCHAR(180),
    customer_phone VARCHAR(40),
    invoice_date DATE DEFAULT CURRENT_DATE,
    due_date DATE,
    status VARCHAR(30) DEFAULT 'Paid',
    subtotal NUMERIC(14,2) DEFAULT 0,
    discount NUMERIC(14,2) DEFAULT 0,
    tax NUMERIC(14,2) DEFAULT 0,
    total NUMERIC(14,2) DEFAULT 0,
    paid_amount NUMERIC(14,2) DEFAULT 0,
    balance NUMERIC(14,2) DEFAULT 0,
    payment_method VARCHAR(80),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expenditures_ledger (
    expenditure_id SERIAL PRIMARY KEY,
    category VARCHAR(80) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    reason TEXT,
    paid_to VARCHAR(180),
    payment_method VARCHAR(80),
    reference_number VARCHAR(100),
    expenditure_date DATE DEFAULT CURRENT_DATE,
    approved_by VARCHAR(160),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_technicians_search ON technicians (name, phone_number, status);
CREATE INDEX IF NOT EXISTS idx_suppliers_search ON suppliers (name, tpnumber, status);
CREATE INDEX IF NOT EXISTS idx_customers_search ON customers (customer_name, phone_number, status);
CREATE INDEX IF NOT EXISTS idx_invoices_search ON invoices (invoice_number, customer_name, status, invoice_date);
CREATE INDEX IF NOT EXISTS idx_expenditures_date ON expenditures_ledger (category, expenditure_date);
