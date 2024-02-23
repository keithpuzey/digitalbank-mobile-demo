import requests
import time
from config import PerfectoKey

perfecto_cloud = 'demo.perfectomobile.com'
script_key = 'PUBLIC:DBankMobileRegistration.xml'

# Function to start the test execution
# Function to start the test execution
def start_test():
    url = f'https://{perfecto_cloud}/services/executions?operation=execute&scriptKey={script_key}&securityToken={PerfectoKey}&output.visibility=public'
    headers = {
        'Content-Type': 'application/json'
    }

    response = requests.post(url, headers=headers)
    if response.status_code == 200:
        try:
            return response.json()['executionId']
        except KeyError:
            print("Error: 'executionId' key not found in response. Response content:")
            print(response.content)
            return None
    else:
        print("Error starting test execution:", response.text)
        return None

# Function to check the status of the test execution
def check_test_status(execution_id):
    url = f'https://{perfecto_cloud}/services/executions/{execution_id}?operation=status&securityToken={PerfectoKey}'
    headers = {}

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # Raise exception if status code is not 200
        json_data = response.json()
        status = json_data.get('status')
        flow_end_code = json_data.get('flowEndCode')
        return status, flow_end_code  # Correct placement of return statement
    except requests.exceptions.RequestException as e:
        print("Error checking test status:", e)
        return None

# Main function to start the test and continuously check its status until completion
def main():
    execution_id = start_test()
    if execution_id is None:
        return

    print("Test execution started with ID:", execution_id)

    while True:
        status, flow_end_code = check_test_status(execution_id)  # Get status and flow_end_code from the function
        if status is None:
            break

        print("Current status:", status)
        print("Flow End Code:", flow_end_code )

        if status.lower() in ['completed', 'failed', 'stopped']:
            print("Test execution completed with status:", status)
            if flow_end_code == 'Failed':
                print("Test execution failed.")
                exit(1)  # Exit with status 1 indicating failure
            else:
                print("Test execution OK.")
                exit(0)  # Exit with status 0 indicating success
            break

        time.sleep(30)  # Check status every 30 seconds

if __name__ == "__main__":
    main()
