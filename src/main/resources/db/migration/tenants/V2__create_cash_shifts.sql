CREATE TABLE cash_shifts (
    id UUID PRIMARY KEY,
    opened_by UUID NOT NULL,
    opened_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP,
    starting_cash NUMERIC(10, 2) NOT NULL,
    expected_cash NUMERIC(10, 2),
    actual_cash NUMERIC(10, 2),
    difference NUMERIC(10, 2),
    status VARCHAR(20) NOT NULL
);
