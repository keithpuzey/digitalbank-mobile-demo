USE digitalbank;

SELECT COUNT(id) AS "Number of Registered Customers" FROM users;

SELECT id, username AS "10 Most Recently Registered Bank Customers"
FROM users
ORDER BY id DESC
LIMIT 10;