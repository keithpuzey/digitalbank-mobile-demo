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
    "description": f"WorkSoft Demo  {current_date}",
    "endpointPreference": "HTTPS",
    "harborId": "65a56d48e1ab5c98590583b8",
    "type": "TRANSACTIONAL",
    "liveSystemHost": "null",
    "liveSystemPort": "null",
    "name": f"WorkSoft Demo {service_name}",
    "serviceId": int(ServiceID),
    "shipId": "65a56d52edfe2d1cac08f507",
    "thinkTime": int(MockThinkTime),
    "appliedTemplateId": 5971,
    "mockServiceTransactions": [
        {"txnId": 6378107, "priority": 10},
        {"txnId": 6378106, "priority": 10},
        {"txnId": 6378104, "priority": 10}
    ]
}

# Create Mock Service using payload patchOrg
response = requests.post(
    f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks",
    json=payload,
    headers={'Content-Type': 'application/json'},
    auth=BMCredentials
)
json_response = response.json()
mockid = json_response['result']['id']
print(f"Mock Service IDs: {json_response['result']['id']}")

print("Prepare Environment - Start Mock Services ")

# Start Mock Service
response = requests.get(
    f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{mockid}/deploy",
    headers={'Content-Type': 'application/json'},
    auth=BMCredentials
)

while True:
    time.sleep(15)
    # Retrieve Status of Mock Service
    response = requests.get(
        f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{mockid}",
        headers={'Content-Type': 'application/json'},
        auth=BMCredentials
    )
    json_response = response.json()
    mockendpoint = json_response['result']['httpsEndpoint']
    mockstat = json_response['result']['status']
    print(f"Mock Service Status {mockstat}")

    if mockstat == 'RUNNING':
        break

print(f"Mock Service Started - Endpoint details {mockendpoint}")


# Deploy Template
response = requests.patch(
    f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{mockid}/apply-template/5971",
    headers={'Content-Type': 'application/json'},
    auth=BMCredentials
)
print(f"Mock Service Template Status -{response}")
while True:
    time.sleep(15)
    # Retrieve Status of Mock Service
    response = requests.get(
        f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{mockid}",
        headers={'Content-Type': 'application/json'},
        auth=BMCredentials
    )

    json_response = response.json()
    mockstat = json_response['result']['status']
    print(f"Mock Service Status {mockstat}")

    if mockstat == 'RUNNING':
        break

#print(f"Mock Service Template Deployed")

with open(mock_output, 'w') as file:
    try:
        # Write the Mock URL to the file
        file.write(
            f"{mockendpoint}")
     #   print(f"Mock URL written to: {mock_output}")

    except Exception as e:
        print(f"Error writing to file: {e}")

with open(mock_output_id, 'w') as file:
    try:
        # Write the Mock ID to the file
        file.write(
            f"{mockid}")
     #   print(f"Mock URL written to: {mock_output_id}")

    except Exception as e:
        print(f"Error writing to file: {e}")