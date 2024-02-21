import csv
import requests
import subprocess
from config import workspaceID, BMCredentials , mock_output_id

# Read mockid from CSV file (assuming the first column contains mockid)
with open(f'{mock_output_id}', 'r') as csvfile:
    reader = csv.reader(csvfile)
    mockid = next(reader)[0]

# Delete Mock Service
response = requests.delete(
    f"https://mock.blazemeter.com/api/v1/workspaces/{workspaceID}/service-mocks/{mockid}",
    auth=BMCredentials
)
print(f"Deleting Mock Service ")
