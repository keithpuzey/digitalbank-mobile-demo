import requests
import time
import json
import sys
import logging
from config import BMCredentials, test_result_url, account

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def run_blazemeter_test(test_id):
    try:
        # Start Blazemeter Performance Test
        logging.info(f"Starting Blazemeter Performance Test with Test ID {test_id}")
        response = requests.post(url=f"https://a.blazemeter.com/api/v4/multi-tests/{test_id}/run", auth=BMCredentials)
        response.raise_for_status()  # Raise an error for bad status codes

        # Extract values from the response
        json_response = response.json()
        logging.info(f"Response: {json_response}")
        result_id = json_response['result']['id']
        workspace_id = json_response['result']['workspaceId']
        project_id = json_response['result']['projectId']

        # Print extracted values
        logging.info(f"Result ID: {result_id}")
        logging.info(f"Workspace ID: {workspace_id}")
        logging.info(f"Project ID: {project_id}")
        logging.info(f"Test URL: https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary")

        testsessionid = json_response['result']['sessionsId'][0]
        logging.info(f"Test Session ID: {testsessionid}")

        with open(test_result_url, 'w') as file:
            try:
                # Write the Test URL to the file
                file.write(f"https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary")
                logging.info(f"Test URL written to: {test_result_url}")
            except Exception as e:
                logging.error(f"Error writing to file: {e}")

        while True:
            time.sleep(60)

            # Check Status of Test
            response = requests.get(url=f"https://a.blazemeter.com:443/api/latest/sessions/{testsessionid}", auth=BMCredentials)
            response.raise_for_status()  # Raise an error for bad status codes
            json_response = response.json()
            teststat = json_response['result']['status']
            logging.info(f"Test Status: {teststat}")

            if teststat == 'ENDED':
                while True:
                    # Get Test Results
                    testresponse = requests.get(url=f"https://a.blazemeter.com/api/v4/masters/{result_id}", auth=BMCredentials)
                    testresponse.raise_for_status()  # Raise an error for bad status codes
                    testjson_response = testresponse.json()

                    # Identify the correct key for the reportStatus
                    report_status = testjson_response['result'].get('reportStatus')

                    # Take different actions based on reportStatus
                    if report_status == 'pass':
                        logging.info(f"Final Test Result Status: Passed")
                        logging.info(f"Test URL: https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary")
                        sys.exit(0)
                    elif report_status == 'fail':
                        logging.info(f"Final Test Result Status: Failed")
                        logging.info(f"Test URL: https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary")
                        sys.exit(1)
                    elif report_status == 'unset':
                        logging.info(f"Final Test Result Status: Not Set")
                        logging.info(f"Test URL: https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary")
                        sys.exit(0)
                    else:
                        logging.info(f"Final Test Result Status: Pending")

                    time.sleep(30)  # Add a delay before the next attempt

                    # Break the inner loop if a valid result is obtained
                    if report_status in ['pass', 'fail', 'unset']:
                        break

                # Break the outer loop if the test passes
                if report_status == "pass":
                    logging.info(f"Test URL: https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary")
                    break

    except requests.RequestException as e:
        logging.error(f"HTTP request failed: {e}")
        sys.exit(1)
    except KeyError as e:
        logging.error(f"Missing key in JSON response: {e}")
        sys.exit(1)
    except Exception as e:
        logging.error(f"An unexpected error occurred: {e}")
        sys.exit(1)

if __name__ == "__main__":
    # Check if the test ID is provided as a command-line argument
    if len(sys.argv) != 2:
        logging.error("Usage: python script.py <test_id>")
        sys.exit(1)

    # Retrieve the test ID from the command-line argument
    test_id = sys.argv[1]
    run_blazemeter_test(test_id)
