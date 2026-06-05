-- Customers
INSERT INTO customer (name) VALUES ('Sri Raj');
INSERT INTO customer (name) VALUES ('Raj');

-- Current Month
INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Sri Raj'), 120, CURRENT_DATE - 3);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Sri Raj'), 80, CURRENT_DATE - 2);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Raj'), 110, CURRENT_DATE - 3);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Raj'), 70, CURRENT_DATE - 2);

-- Previous Month
INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Sri Raj'), 180, CURRENT_DATE - 10);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Sri Raj'), 75, CURRENT_DATE - 20);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Raj'), 150, CURRENT_DATE - 10);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Raj'), 65, CURRENT_DATE - 20);

-- Two Months Back
INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Sri Raj'), 220, CURRENT_DATE - 40);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Sri Raj'), 60, CURRENT_DATE - 45);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Raj'), 10, CURRENT_DATE - 40);

INSERT INTO transactions (customer_id, amount, transaction_date)
VALUES ((SELECT id FROM customer WHERE name = 'Raj'), 100, CURRENT_DATE - 45);