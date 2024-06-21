import mysql.connector

# MySQL database connection details
db_config = {
    'host': '',
    'user': '',
    'password': '',
    'database': 'digitalbank',
    'port': 3309
}

# Connect to the database
try:
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    # Execute SQL queries
    queries = [
        "USE digitalbank;",
        """
        SELECT COUNT(id) AS "Number of Registered Customers" 
        FROM users;
        """,
        """
        SELECT id, username AS "10 Most Recently Registered Bank Customers"
        FROM users
        ORDER BY id DESC
        LIMIT 10;
        """
    ]

    for query in queries:
        cursor.execute(query)
        if cursor.with_rows:  # If the query returns rows
            result = cursor.fetchall()
            for row in result:
                print(row)
        else:
            print("Query executed successfully")

    cursor.close()
    conn.close()
except mysql.connector.Error as err:
    print(f"Error: {err}")
