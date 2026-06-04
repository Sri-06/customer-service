-- Customer
INSERT INTO customer (id, name)
VALUES (1, 'Sri Raj');

-- Current Month
INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES (1, 120, CURRENT_DATE - 3);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES (1, 80, CURRENT_DATE - 2);

-- Previous Month
INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES (1, 180, CURRENT_DATE - 10);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES (1, 75, CURRENT_DATE - 20);

-- Two Months Back
INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES (1, 220, CURRENT_DATE - 40);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES (1, 60, CURRENT_DATE - 45);