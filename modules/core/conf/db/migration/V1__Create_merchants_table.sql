CREATE TYPE DefaultBillingLanguage_Enum AS ENUM
  ('Thai', 'Bahasa', 'English', 'Chinese');

CREATE TYPE PaymentType_Enum AS ENUM
  ('Prepaid', 'PostPaid');

CREATE TYPE CommissionType_Enum AS ENUM
  ('Fixed', 'Vary');

CREATE TYPE NetDays_Enum AS ENUM
  ('PerCover', 'PerBooking');

CREATE TABLE merchants (
  id UUID PRIMARY KEY,
  contract_from               TIMESTAMPTZ                   NOT NULL,
  contract_until              TIMESTAMPTZ                   NOT NULL,
  default_billing_language    DefaultBillingLanguage_Enum   NOT NULL,
  payment_type                PaymentType_Enum              NOT NULL,
  commission_type             CommissionType_Enum           NOT NULL,
  net_days                    NetDays_Enum                  NOT NULL,
  owner_name                  TEXT,
  company_name                TEXT,
  virtual_bank_account        TEXT,
  phone                       TEXT,
  email                       TEXT,
  tax_id                      TEXT,
  additional_info             TEXT,
  version                     INTEGER                       NOT NULL
);

CREATE INDEX idx_merchants_id_u ON merchants(id);

-- Initial data
INSERT INTO merchants(id, contract_from, contract_until, default_billing_language, payment_type,
  commission_type, net_days, owner_name, company_name, virtual_bank_account, phone, email, tax_id,
  additional_info, version) VALUES
  ('af0b3c12-205b-4142-b4a0-b0edba88abfb', now(), now(), 'Thai', 'Prepaid',
   'Fixed', 'PerCover', 'owner1', 'company1', 'bankaccount1', 'phone1', 'email1@example.com', 'taxId1',
   'some additional Info1', 1
  ),
  ('02fad0ce-f88b-4aa1-a865-5053f70ff0b7', now(), now(), 'Bahasa', 'Prepaid',
   'Fixed', 'PerCover', 'owner2', 'company2', 'bankaccount2', 'phone2', 'email2@example.com', 'taxId2',
   'some additional Info2', 1
  ),
  ('b0975abd-ac59-40c6-b605-4bbc0f219a7a', now(), now(), 'English', 'Prepaid',
   'Fixed', 'PerCover', 'owner3', 'company3', 'bankaccount3', 'phone3', 'email3@example.com', 'taxId3',
   'some additional Info3', 1
  ),
  ('fab3bf30-991d-4180-80b2-bb2804d42ac9', now(), now(), 'Chinese', 'PostPaid',
   'Fixed', 'PerCover', 'owner4', 'company4', 'bankaccount4', 'phone4', 'email4@example.com', 'taxId4',
   'some additional Info4', 1
  ),
  ('6c40baf1-0fd9-40aa-879f-268ae3c50fc7', now(), now(), 'Thai', 'PostPaid',
   'Fixed', 'PerCover', 'owner5', 'company5', 'bankaccount5', 'phone5', 'email5@example.com', 'taxId5',
   'some additional Info5', 1
  ),
  ('04c3c470-a781-478a-8bf9-95cdfb1dbbb1', now(), now(), 'Thai', 'Prepaid',
   'Fixed', 'PerBooking', 'owner6', 'company6', 'bankaccount6', 'phone6', 'email6@example.com', 'taxId6',
   'some additional Info6', 1
  );
