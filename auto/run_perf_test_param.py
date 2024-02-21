import requests
import time
import json
import sys
from config import BMCredentials, test_result_url, account

# Log file path
log_file_path = "c:\Demo\Logs\perftest.log"

def run_blazemeter_test(test_id):

    # Start Blazemeter Performance Test
    print(f"Start Blazemeter Performance Test with Test ID {test_id}")
    response = requests.post(url=f"https://a.blazemeter.com/api/v4/tests/{test_id}/Start", auth=BMCredentials)

    # Extract values from the response
    json_response = response.json()
    result_id = json_response['result']['id']
    workspace_id = json_response['result']['workspaceId']
    project_id = json_response['result']['projectId']

    # Print extracted values
    print(f"\nResult ID: {result_id}")
    print(f"Workspace ID: {workspace_id}")
    print(f"Project ID: {project_id}")
    print(f"Test URL https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary ")

    testsessionid = json_response['result']['sessionsId'][0]
    print(f"Test Session ID = {testsessionid}")


    with open(test_result_url, 'w') as file:
        try:
            # Write the Test URL to the file
            file.write(f"https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary")
            print(f"Test URL written to: {test_result_url}")
        except Exception as e:
            print(f"Error writing to file: {e}")

    # Redirect stdout and stderr to a log file
    sys.stdout = open(log_file_path, 'a')
    sys.stderr = sys.stdout

    while True:
        time.sleep(60)

        # Check Status of Test
        response = requests.get(url=f"https://a.blazemeter.com:443/api/latest/sessions/{testsessionid}", auth=BMCredentials)
        json_response = response.json()
        teststat = json_response['result']['status']
        print(f'Test Status = {teststat}')
        # Redirect stdout and stderr to a log file
        sys.stdout = open(log_file_path, 'a')
        sys.stderr = sys.stdout

        if teststat == 'ENDED':
            while True:
                # Get Test Results
                testresponse = requests.get(url=f"https://a.blazemeter.com/api/v4/masters/{result_id}", auth=BMCredentials)
                testjson_response = testresponse.json()

                #Identify the correct key for the reportStatus
                report_status = testjson_response['result'].get('reportStatus')

                # Take different actions based on reportStatus
                if report_status == 'pass':
                    print(f'Final Test Result Status = Passed')
                    print(f"Test URL https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary ")
                    sys.exit(0)
                    break
                elif report_status == 'fail':
                    print(f'Final Test Result Status = Failed')
                    print(f"Test URL https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary ")
                    sys.exit(1)

                elif report_status == 'unset':
                    print(f'Final Test Result Status = Not Set')
                    print(f"Test URL https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary ")
                    sys.exit(0)
                    break
                else:
                    print(f'Final Test Result Status = Pending')

                time.sleep(30)  # Add a delay before the next attempt

                # Break the inner loop if a valid result is obtained
                if report_status in ['pass', 'fail', 'unset']:
                    break

            # Break the outer loop if the test passes
            if report_status == "pass":
                print(f"Test URL https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary ")
                break

    else:
        print(f"Test URL https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary ")
    sys.exit(1)

if __name__ == "__main__":
    # Check if the test ID is provided as a command-line argument
    if len(sys.argv) != 2:
        print("Usage: python script.py <test_id>")
        sys.exit(1)

    # Retrieve the test ID from the command-line argument
    test_id = sys.argv[1]
    run_blazemeter_test(test_id)
