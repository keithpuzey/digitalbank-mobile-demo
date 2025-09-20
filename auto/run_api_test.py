import requests
import time
import os
import sys
import xml.etree.ElementTree as ET

# Get token from environment
api_token = os.getenv("APICredentials")

if not api_token:
    print("❌ APICredentials environment variable is not set.")
    sys.exit(1)

# Prepend "Bearer " if not already included
if not api_token.lower().startswith("bearer "):
    AUTH_TOKEN = f"Bearer {api_token}"
else:
    AUTH_TOKEN = api_token

# Configurable values
RUNSCOPE_TRIGGER_URL = "https://api.runscope.com/radar/49adfa54-2b83-4029-96a0-2285ab48ac47/trigger?runscope_environment=8816780a-0dbc-4f03-854c-10636ab3da36"

RESULT_DIR = "test-results"
RESULT_FILE = os.path.join(RESULT_DIR, "runscope-result.xml")

HEADERS = {
    "Authorization": AUTH_TOKEN
}


def trigger_test():
    print("🔄 Triggering Runscope test...")
    response = requests.post(RUNSCOPE_TRIGGER_URL)
    response.raise_for_status()
    data = response.json()

    try:
        run_info = data["data"]["runs"][0]
        api_test_run_url = run_info["api_test_run_url"]
        test_name = run_info["test_name"]
        test_id = run_info["test_id"]
        test_run_url = run_info["test_run_url"]
    except (KeyError, IndexError):
        print("❌ Failed to trigger test. Response:")
        print(data)
        sys.exit(1)

    if not api_test_run_url:
        print("❌ No test run URL returned, cannot proceed.")
        sys.exit(1)

    print(f"✅ Test triggered: {test_name} ({test_id})")
    print(f"🔁 Polling test run at: {api_test_run_url}")
    return api_test_run_url, test_name, test_run_url


def poll_until_complete(api_test_run_url):
    status = "working"
    attempts = 0
    max_attempts = 60  # ~5 minutes timeout (60*5s)

    while status in ("init", "working"):
        if attempts >= max_attempts:
            print("⏱️ Timed out waiting for test to complete.")
            sys.exit(1)

        time.sleep(5)
        resp = requests.get(api_test_run_url, headers=HEADERS)
        resp.raise_for_status()
        data = resp.json()
        status = data.get("data", {}).get("result")
        print(f"⏳ Current status: {status}")
        attempts += 1

    return data


def generate_junit_xml(test_name, final_result, test_run_url):
    if not os.path.exists(RESULT_DIR):
        os.makedirs(RESULT_DIR)

    testsuite = ET.Element("testsuite", name="Runscope Test Suite", tests="1",
                           failures="0" if final_result == "pass" else "1")
    testcase = ET.SubElement(testsuite, "testcase", classname="Runscope", name=test_name)

    if final_result != "pass":
        ET.SubElement(testcase, "failure", message=f"Runscope test result: {final_result}")

    ET.SubElement(testcase, "system-out").text = f"Test Report URL: {test_run_url}"

    tree = ET.ElementTree(testsuite)
    tree.write(RESULT_FILE, encoding="utf-8", xml_declaration=True)
    print(f"📄 JUnit result saved to {RESULT_FILE}")


def main():
    api_test_run_url, test_name, test_run_url = trigger_test()
    status_response = poll_until_complete(api_test_run_url)
    final_result = status_response.get("data", {}).get("result")
    print(f"✅ Final result: {final_result}")
    generate_junit_xml(test_name, final_result, test_run_url)


if __name__ == "__main__":
    main()