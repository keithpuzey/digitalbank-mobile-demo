import requests
import time
from config import PerfectoKey

perfecto_cloud = 'demo.perfectomobile.com'
script_key = 'PUBLIC:DBankMobileRegistration.xml'

# Function to start the test execution
def start_test():
    url = f'https://{perfecto_cloud}/services/executions?operation=execute&scriptKey={script_key}&securityToken={PerfectoKey}&output.visibility=public'
    headers = {
        'Content-Type': 'application/json'
    }

    response = requests.post(url, headers=headers)
    if response.status_code == 200:
        try:
            response_json = response.json()
            execution_id = response_json.get('executionId')
            report_key = response_json.get('reportKey')
            test_grid_report_url = response_json.get('testGridReportUrl')
            single_test_report_url = response_json.get('singleTestReportUrl')
            print("Single Test Report", single_test_report_url)

            return execution_id, report_key, test_grid_report_url, single_test_report_url
        except KeyError:
            print("Error: 'executionId' key not found in response. Response content:")
            print(response.content)
            return None, None, None, None
    else:
        print("Error starting test execution:", response.text)
        return None, None, None, None

# Function to check the status of the test execution
def check_test_status(execution_id):
    url = f'https://{perfecto_cloud}/services/executions/{execution_id}?operation=status&securityToken={PerfectoKey}'
    headers = {}

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        json_data = response.json()
        report_key = json_data.get('reportKey')
        reason = json_data.get('reason')
        status = json_data.get('status')
        devices = json_data.get('devices')
        flow_end_code = json_data.get('flowEndCode')
        return status, flow_end_code, report_key, reason, devices
    except requests.exceptions.RequestException as e:
        print("Error checking test status:", e)
        return None, None, None, None

# Main function to start the test and continuously check its status until completion
def main():
    execution_id, report_key, test_grid_report_url, single_test_report_url = start_test()
    if execution_id is None:
        return

    print("Test execution started with ID:", execution_id)

    while True:
        status, flow_end_code, report_key, reason, devices = check_test_status(execution_id)  # Remove devices from unpacking
        if status is None:
            break

        print("Current status:", status)
        print("Flow End Code:", flow_end_code)

        if status.lower() in ['completed', 'failed', 'stopped']:
            print("Test execution completed with status:", status)
            if flow_end_code == 'Failed':

                # print("Execution ID:", execution_id)
                print("Test Grid Report:", test_grid_report_url)
                # print("Report Key:", report_key)
                print("Reason:", reason)
                print("Devices:", devices)
                # print("Single Test Report:", single_test_report_url)
                print("Test execution failed.")
                exit(1)  # Exit with status 1 indicating failure
            else:

                # print("Execution ID:", execution_id)
                print("Test Grid Report:", test_grid_report_url)
                # print("Report Key:", report_key)
                print("Reason:", reason)
                print("Devices:", devices)
                # print("Single Test Report:", single_test_report_url)
                print("Test execution OK.")
                exit(0)  # Exit with status 0 indicating success
            break

        time.sleep(10)  # Check status every 10 seconds

if __name__ == "__main__":
    main()
