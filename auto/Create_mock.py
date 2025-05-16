import requests
import time
import random
from datetime import datetime
from config import workspaceID, account, ServiceID, MockThinkTime, BMCredentials , mock_output, base_dir, mock_output_id , TemplateID

# Get the current day as a string (e.g., '01', '02', ..., '31')
current_day = datetime.now().strftime('%d')
current_date = datetime.now()
# Generate a random string of length 6
random_string = ''.join(random.choices('abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', k=6))
# Combine the current day and the random string
service_name = current_day + random_string

payload = {
    "description": f"E2E Demo  {current_date}",
    "endpointPreference": "HTTPS",
    "harborId": "5c544422c7dc9735767b23ce",
    "type": "TRANSACTIONAL",
    "liveSystemHost": "null",
    "liveSystemPort": "null",
    "name": f"E2E Demo {service_name}",
    "serviceId": int(ServiceID),
    "shipId": "5d3ccab3526ad28f53205574",
    "thinkTime": int(MockThinkTime),
    "appliedTemplateId": (TemplateID),
    "mockServiceTransactions": [
        {"txnId": 6458371, "priority": 10},
        {"txnId": 6458373, "priority": 10},
        {"txnId": 6458374, "priority": 10}
    ]
}

# Create Mock Service using payload patchOrg
response = requests.post(
    f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks",
    json=payload,
    headers={'Content-Type': 'application/json'},
    auth=BMCredentials
)

# Check if the response is successful and contains the expected data
if response.status_code != 200:
    print(f"Failed to create mock service. Status code: {response.status_code}, Response: {response.text}")
    exit(1)

json_response = response.json()
if 'result' not in json_response or 'id' not in json_response['result']:
    print(f"Unexpected response format: {json_response}")
    exit(1)

mockid = json_response['result']['id']
print(f"Mock Service IDs: {mockid}")

print("Prepare Environment - Start Mock Services ")

# Start Mock Service
response = requests.get(
    f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{mockid}/deploy",
    headers={'Content-Type': 'application/json'},
    auth=BMCredentials
)

if response.status_code != 200:
    print(f"Failed to start mock service. Status code: {response.status_code}, Response: {response.text}")
    exit(1)

while True:
    time.sleep(15)
    # Retrieve Status of Mock Service
    response = requests.get(
        f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{mockid}",
        headers={'Content-Type': 'application/json'},
        auth=BMCredentials
    )

    if response.status_code != 200:
        print(f"Failed to retrieve mock service status. Status code: {response.status_code}, Response: {response.text}")
        continue

    json_response = response.json()
    if 'result' not in json_response or 'status' not in json_response['result']:
        print(f"Unexpected response format: {json_response}")
        continue

    mockendpoint = json_response['result'].get('httpsEndpoint', 'Unknown')
    mockstat = json_response['result']['status']
    print(f"Mock Service Status: {mockstat}")

    if mockstat == 'RUNNING':
        break

print(f"Mock Service Started - Endpoint details: {mockendpoint}")

# Deploy Template
response = requests.patch(
    f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{mockid}/apply-template/5971",
    headers={'Content-Type': 'application/json'},
    auth=BMCredentials
)
print(f"Mock Service Template Status: {response.status_code}, Response: {response.text}")

while True:
    time.sleep(15)
    # Retrieve Status of Mock Service
    response = requests.get(
        f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{mockid}",
        headers={'Content-Type': 'application/json'},
        auth=BMCredentials
    )

    if response.status_code != 200:
        print(f"Failed to retrieve mock service status. Status code: {response.status_code}, Response: {response.text}")
        continue

    json_response = response.json()
    if 'result' not in json_response or 'status' not in json_response['result']:
        print(f"Unexpected response format: {json_response}")
        continue

    mockstat = json_response['result']['status']
    print(f"Mock Service Status: {mockstat}")

    if mockstat == 'RUNNING':
        break

with open(mock_output, 'w') as file:
    try:
        # Write the Mock URL to the file
        file.write(f"{mockendpoint}")
    except Exception as e:
        print(f"Error writing to file: {e}")

with open(mock_output_id, 'w') as file:
    try:
        # Write the Mock ID to the file
        file.write(f"{mockid}")
    except Exception as e:
        print(f"Error writing to file: {e}")
