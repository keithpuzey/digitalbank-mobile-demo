
import requests
import time
import json
from config import BMCredentials

# Blazemeter Tests - Performance Test ID  BMTestID = 11873026  - SWAT Demo  - Digital Bank Workspace

# Start Blazemeter Performance Test
print("Start Blazemeter Performance Test")
response = requests.post(url=f"https://a.blazemeter.com/api/v4/tests/11873026/Start", auth=BMCredentials)
json_response = response.json()
testsessionid = json_response['result']['sessionsId'][0]
print(f"Test Session ID = {testsessionid}")

while True:

    time.sleep(120)

    # Check Status of Test
    response = requests.get(
        url=f"https://a.blazemeter.com:443/api/latest/sessions/{testsessionid}",
        auth=BMCredentials )
    json_response = response.json()
    testthreshold = json_response['result']['failedThresholds']
    teststat = json_response['result']['status']
    if teststat == 'ENDED':
        final_response = json_response
        # Get Test Results
        response = requests.post(url=f"https://a.blazemeter.com/api/v4/masters/", auth=BMCredentials)
        json_response = response.json()
        testsessionid = json_response['result']['sessionsId'][0]
        print(f"Test Session ID = {testsessionid}")
        break

if testthreshold == 0:
    print('Test Passed')

    testresult = "Blazemeter Performance Test Passed"
else:
    print('Test Failed')
    testresult = "Blazemeter Performance Test Failed"

# Print the final response in JSON format
print("\nFinal Response (JSON):")
print(json.dumps(final_response))

# Print the final response with indentation for better readability
print("\nFinal Response (Formatted):")
print(json.dumps(final_response, indent=2))

