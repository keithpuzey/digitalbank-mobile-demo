import mysql.connector

# MySQL database connection details
db_config = {
    'host': '10.128.0.118',
    'user': 'root',
    'password': 'CAdemo123',
    'database': 'digitalbank',
    'port': 3309
}

try:
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    # Query to count the number of registered customers
    count_query = "SELECT COUNT(id) AS 'Number of Registered Customers' FROM users;"
    cursor.execute(count_query)
    count_result = cursor.fetchone()
    number_of_customers = count_result[0] if count_result and count_result[0] else "No data available"

    # Print the number of registered customers
    print("Number of Registered Customers: {}".format(number_of_customers))

    # Query to get the last 10 registered users
    user_query = """
        SELECT id, username AS "Username"
        FROM users
        ORDER BY id DESC
        LIMIT 10;
    """
    cursor.execute(user_query)
    user_result = cursor.fetchall()

    # Print the header
    print("\n10 Most Recently Registered Bank Customers:")
    print("ID\tUsername")  # Adjust column width as needed

    # Print the data
    for row in user_result:
        print("{}\t{}".format(row[0], row[1]))

    # Query to get the last opened account
    account_query = "SELECT MAX(transaction_date) AS last_opened_account FROM account_transaction;"
    cursor.execute(account_query)
    account_result = cursor.fetchone()
    last_opened_account = account_result[0] if account_result and account_result[0] else "No data available"

    # Print last opened account date/time
    print("\nLast Updated Transaction -", last_opened_account)

    cursor.close()
    conn.close()
except mysql.connector.Error as err:
    print("Error:", err)
