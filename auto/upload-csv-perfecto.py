import requests
import json
import csv
from config import test_data_csv , PerfectoKey


# Replace this URL with the actual API endpoint
api_url = "https://demo.app.perfectomobile.com/native-automation-webapp/rest/v1/native-automation/datatable"

csv_file_name = 'blazedata-test.csv'
csv_file_path = f"{test_data_csv}_{csv_file_name}"

# Read CSV file and prepare data
csv_data = []
with open(csv_file_path, newline='') as csvfile:
    csv_reader = csv.DictReader(csvfile)
    for row in csv_reader:
        csv_data.append(list(row.values()))

# Prepare payload
payload = {
    "dataTable": {
        "columns": [{"name": col, "type": "STRING"} for col in csv_reader.fieldnames],
        "elementsData": csv_data
    },
    "itemKey": "PUBLIC:worksoft_demo.xml",
    "overwrite": "true",
    "draftKey": ""
}
additional_data = {
    "dataTable": {
        "columns": [
            {"name": "FirstName", "type": "STRING"},
            {"name": "LastName", "type": "STRING"},
            {"name": "Email", "type": "STRING"},
            {"name": "dob", "type": "STRING"},
            {"name": "ssn", "type": "STRING"},
            {"name": "address", "type": "STRING"},
            {"name": "city", "type": "STRING"},
            {"name": "State", "type": "STRING"},
            {"name": "ZipCode", "type": "STRING"},
            {"name": "seq", "type": "STRING"},
            {"name": "title", "type": "STRING"},
            {"name": "phonenumber", "type": "STRING"},
            {"name": "newuserpassword", "type": "STRING"},
            {"name": "gender", "type": "STRING"}
        ],
        "elementsData": csv_data
    },
    "itemKey": "PUBLIC:test_data.csv",
    "overwrite": True,
    "draftKey": ""
}

# Merge the main payload and additional data
payload = {**additional_data}

# Convert payload to JSON
json_payload = json.dumps(payload)


# Make the POST request

headers = {
    "Content-Type": "application/json",
    "Perfecto-Authorization": PerfectoKey
}

response = requests.post(api_url, data=json_payload, headers=headers)

# Print the response
print(response.status_code)
print(response.text)

url = f"https://demo.perfectomobile.com/services/executions?operation=execute&scriptKey=PRIVATE:CustomerQueryInstall.xml&securityToken={PerfectoKey}"

scriptresponse = requests.post(url)

# Print the response
print(scriptresponse.status_code)
print(scriptresponse.text)
