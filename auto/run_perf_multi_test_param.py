import requests
import time
import sys
import logging
from config import BMCredentials, test_result_url, account

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def run_blazemeter_test(test_id):
    try:
        # Start test
        logging.info(f"Starting BlazeMeter Performance Test with Test ID {test_id}")
        response = requests.post(
            f"https://a.blazemeter.com/api/v4/multi-tests/{test_id}/run",
            auth=BMCredentials
        )
        response.raise_for_status()
        result = response.json()['result']

        result_id = result['id']
        workspace_id = result['workspaceId']
        project_id = result['projectId']
        testsessionid = result['sessionsId'][0]

        test_url = f"https://a.blazemeter.com/app/#/accounts/{account}/workspaces/{workspace_id}/projects/{project_id}/masters/{result_id}/summary"

        # Write URL to file
        with open(test_result_url, 'w') as f:
            f.write(test_url)

        # Poll until test ends
        while True:
            time.sleep(60)
            status_resp = requests.get(f"https://a.blazemeter.com:443/api/latest/sessions/{testsessionid}", auth=BMCredentials)
            status_resp.raise_for_status()
            status = status_resp.json()['result']['status']
            logging.info(f"Test Status: {status}")

            if status == 'ENDED':
                master_resp = requests.get(f"https://a.blazemeter.com/api/v4/masters/{result_id}", auth=BMCredentials)
                master_resp.raise_for_status()
                report_status = master_resp.json()['result'].get('reportStatus', 'unset')

                print("\n📊 FINAL RESULT")
                print("----------------------------")
                print(f"Test URL      : {test_url}")
                print(f"End Status    : {report_status.upper()}")
                print("----------------------------")

                if report_status == 'pass':
                    print("✅ Test PASSED")
                    sys.exit(0)
                elif report_status == 'fail':
                    print("❌ Test FAILED")
                    sys.exit(1)
                else:
                    print("⚠️ Test Not Set")
                    sys.exit(0)

    except Exception as e:
        logging.error(f"Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        logging.error("Usage: python script.py <test_id>")
        sys.exit(1)
    run_blazemeter_test(sys.argv[1])