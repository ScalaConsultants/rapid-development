CREATE TABLE IF NOT EXISTS merchants (
  id UUID PRIMARY KEY,
  merchant_no INTEGER NOT NULL,
  name VARCHAR(200) NOT NULL,
  city VARCHAR(100) NOT NULL,
  country VARCHAR(50) NOT NULL,
  active BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  version INTEGER NOT NULL
);


CREATE TYPE cycle_types AS ENUM ('EndOfMonth', 'ExactDate');
CREATE TYPE net_days AS ENUM ('PerCover', 'PerBooking');
CREATE TYPE commission_types AS ENUM ('Fixed', 'Vary');
CREATE TYPE payment_types AS ENUM ('Prepaid', 'Postpaid', 'Pending');

CREATE TABLE IF NOT EXISTS merchant_billing_settings (
  id UUID PRIMARY KEY,
  merchantId UUID REFERENCES merchants(id),
  language VARCHAR(50) NOT NULL,
  paymentType: payment_types NOT NULL,
  commissionType commission_types NOT NULL,
  netDays net_days NOT NULL,
  defaultCommission DECIMAL NOT NULL,
  tags VARCHAR(100)[] DEFAULT '{}' NOT NULL,
  contract_start_date TIMESTAMPTZ NOT NULL,
  contract_end_date TIMESTAMPTZ NOT NULL,
  cycleType cycle_types NOT NULL,
  cycle_start_date TIMESTAMPTZ,
  version INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS merchant_contact_information (
  id UUID PRIMARY KEY,
  merchantId UUID REFERENCES merchants(id),
  owner_name VARCHAR(100) NOT NULL,
  company_name VARCHAR(100) NOT NULL,
  phone VARCHAR(15),
  email VARCHAR(15),
  tax_id VARCHAR(15),
  notes TEXT,
  version INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS merchant_contact_information (
  id UUID PRIMARY KEY,
  merchantId UUID REFERENCES merchants(id),
  lastInvoiceDate TIMESTAMPTZ,
  unbilledBookings INTEGER NOT NULL,
  balance DECIMAL,
  version INTEGER NOT NULL
);
