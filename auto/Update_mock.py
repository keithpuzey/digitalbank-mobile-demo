import requests
import time
from config import workspaceID, BMCredentials , RegistrationMockID


# Update the Mock Service with the new CSV file

update = requests.get(
    f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{RegistrationMockID}/configure?keepBlazeData=false",
    headers={'Content-Type': 'application/json'},
    auth=BMCredentials
)

# Check if the Mock Service Update was successful
if update.status_code != 200:
    print(f"Error: Failed to update mock service. Status Code: {update.status_code}")
else:
    print("Successfully updated mock service.")
