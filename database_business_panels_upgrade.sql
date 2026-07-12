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

-- Reviewed supplier invoices imported through Azure Document Intelligence.
CREATE TABLE IF NOT EXISTS supplier_purchase_invoices (
    id BIGSERIAL PRIMARY KEY,
    supplier_id INTEGER NOT NULL,
    supplier_name TEXT,
    supplier_address TEXT,
    supplier_vat_registration TEXT,
    supplier_phone TEXT,
    invoice_number TEXT NOT NULL,
    document_type TEXT,
    copy_type TEXT,
    invoice_date DATE NOT NULL,
    due_date DATE,
    sales_order_number TEXT,
    order_date DATE,
    ship_date DATE,
    salesperson TEXT,
    purchase_order_number TEXT,
    supplier_tax_id TEXT,
    customer_code TEXT,
    customer_name TEXT,
    customer_address TEXT,
    customer_tax_id TEXT,
    customer_phone TEXT,
    shipping_address TEXT,
    shipping_instructions TEXT,
    fiscal_document_number TEXT,
    verification_code TEXT,
    prepared_by TEXT,
    printed_at TEXT,
    checked_by TEXT,
    received_by_name TEXT,
    received_date DATE,
    subtotal NUMERIC(16, 3),
    discount NUMERIC(16, 3),
    freight NUMERIC(16, 3),
    miscellaneous_charges NUMERIC(16, 3),
    tax NUMERIC(16, 3),
    total NUMERIC(16, 3),
    currency VARCHAR(12),
    extraction_confidence NUMERIC(6, 5),
    extraction_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by TEXT,
    original_document_path TEXT NOT NULL,
    source_sha256 CHAR(64) NOT NULL,
    corrected_fields TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_supplier_purchase_invoice_number
    ON supplier_purchase_invoices (supplier_id, lower(invoice_number));
CREATE UNIQUE INDEX IF NOT EXISTS uq_supplier_purchase_invoice_source
    ON supplier_purchase_invoices (source_sha256);

CREATE TABLE IF NOT EXISTS supplier_purchase_invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    line_number INTEGER NOT NULL,
    supplier_product_code TEXT,
    supplier_description TEXT,
    warehouse TEXT,
    quantity NUMERIC(16, 4) NOT NULL,
    unit VARCHAR(40),
    unit_price NUMERIC(16, 4) NOT NULL,
    line_tax NUMERIC(16, 4),
    line_discount NUMERIC(16, 4),
    line_amount NUMERIC(16, 4) NOT NULL,
    extraction_confidence NUMERIC(6, 5),
    internal_product_id INTEGER NOT NULL,
    matching_status VARCHAR(40) NOT NULL,
    corrected_fields TEXT
);

-- Add the full reviewed-invoice field set to databases created by an earlier upgrade.
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS supplier_address TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS supplier_vat_registration TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS supplier_phone TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS document_type TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS copy_type TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS sales_order_number TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS order_date DATE;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS ship_date DATE;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS salesperson TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS customer_code TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS customer_address TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS customer_phone TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS shipping_address TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS shipping_instructions TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS fiscal_document_number TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS verification_code TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS prepared_by TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS printed_at TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS checked_by TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS received_by_name TEXT;
ALTER TABLE supplier_purchase_invoices ADD COLUMN IF NOT EXISTS received_date DATE;
ALTER TABLE supplier_purchase_invoice_items ADD COLUMN IF NOT EXISTS warehouse TEXT;
CREATE INDEX IF NOT EXISTS idx_supplier_purchase_items_invoice
    ON supplier_purchase_invoice_items (invoice_id, line_number);

CREATE TABLE IF NOT EXISTS supplier_product_mappings (
    id BIGSERIAL PRIMARY KEY,
    supplier_id INTEGER NOT NULL,
    supplier_product_code TEXT NOT NULL,
    supplier_description TEXT,
    internal_product_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_supplier_product_mapping
    ON supplier_product_mappings (supplier_id, lower(supplier_product_code));
CREATE INDEX IF NOT EXISTS idx_supplier_product_mapping_product
    ON supplier_product_mappings (internal_product_id);

CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    quantity NUMERIC(16, 4) NOT NULL,
    unit_cost NUMERIC(16, 4) NOT NULL,
    reference_type VARCHAR(40) NOT NULL,
    reference_id BIGINT NOT NULL,
    movement_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by TEXT
);
CREATE INDEX IF NOT EXISTS idx_stock_movements_product_date
    ON stock_movements (product_id, movement_timestamp);
