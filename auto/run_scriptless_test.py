import requests
import time
import os
import xml.etree.ElementTree as ET
from config import PerfectoKey

# -----------------------------
# Config
# -----------------------------
PERFECTO_CLOUD = "demo.perfectomobile.com"
SCRIPT_KEY = "PUBLIC:DBankMobileRegistrationDelphix.xml"

RESULT_DIR = "test-results"
RESULT_FILE = os.path.join(RESULT_DIR, "perfecto-result.xml")
TEST_NAME = "DBankMobileRegistration"

SERVER_URL = os.environ.get("DemoServerURL", "http://dbankdemo.com/bank")

HEADERS = {
    "Perfecto-Authorization": PerfectoKey,
    "Content-Type": "application/json"
}

# -----------------------------
# Start test execution
# -----------------------------
def start_test():
    url = f"https://{PERFECTO_CLOUD}/scriptless/api/executions"

    payload = {
        "testKey": SCRIPT_KEY,
        "params": {
            "ServerURL": SERVER_URL
            # IMPORTANT:
            # If Device is not hard-coded in Scriptless UI,
            # you MUST add it here (e.g. "DUT": "device-id")
        }
    }

    print("🚀 Starting Perfecto Scriptless test")
    print(f"🔗 ServerURL: {SERVER_URL}")

    response = requests.post(url, headers=HEADERS, json=payload)

    if response.status_code != 201:
        print("❌ Failed to start test")
        print(response.text)
        return None, None

    data = response.json()

    execution_id = data.get("executionId")
    report_url = data.get("testGridReportUrl")

    print("🧪 Execution ID:", execution_id)
    print("📊 Report URL:", report_url)

    return execution_id, report_url


# -----------------------------
# Get execution status
# -----------------------------
def get_status(execution_id):
    url = f"https://{PERFECTO_CLOUD}/scriptless/api/executions/{execution_id}"

    response = requests.get(url, headers=HEADERS)
    response.raise_for_status()

    data = response.json()

    return {
        "status": data.get("status"),
        "endCode": data.get("endCode"),
        "completionDescription": data.get("completionDescription"),
        "devices": data.get("devices"),
        "reportKey": data.get("reportKey")
    }


# -----------------------------
# Generate JUnit XML
# -----------------------------
def write_junit(result, report_url, reason=None):
    os.makedirs(RESULT_DIR, exist_ok=True)

    failures = "0" if result == "passed" else "1"

    suite = ET.Element(
        "testsuite",
        name="Perfecto Scriptless",
        tests="1",
        failures=failures
    )

    case = ET.SubElement(
        suite,
        "testcase",
        classname="Perfecto",
        name=TEST_NAME
    )

    if result != "passed":
        ET.SubElement(
            case,
            "failure",
            message=reason or "Test failed"
        )

    ET.SubElement(case, "system-out").text = f"Report: {report_url}"

    tree = ET.ElementTree(suite)
    tree.write(RESULT_FILE, encoding="utf-8", xml_declaration=True)

    print(f"📄 JUnit written to {RESULT_FILE}")


# -----------------------------
# Main
# -----------------------------
# -----------------------------
# Main
# -----------------------------
# -----------------------------
# Main
# -----------------------------
def main():
    execution_id, report_url = start_test()

    if not execution_id:
        write_junit("failed", "N/A", "Failed to start execution")
        print("❌ Failed to start execution, exiting 1")
        exit(1)

    print("🕒 Polling execution status...")

    max_retries = 3  # retries if status fetch fails
    retry_count = 0

    while True:
        try:
            status_info = get_status(execution_id)
        except Exception as e:
            print(f"❌ Error fetching status: {e}")
            retry_count += 1
            if retry_count > max_retries:
                write_junit("failed", report_url, f"Failed to fetch status after {max_retries} attempts")
                print("❌ Exceeded max retries, exiting 1")
                exit(1)
            print(f"⏳ Retrying in 10s... (attempt {retry_count}/{max_retries})")
            time.sleep(10)
            continue

        if not status_info:
            print("❌ Received empty status info, retrying in 10s...")
            time.sleep(10)
            continue

        # Safe extraction and normalization
        status = (status_info.get("status") or "").upper()
        end_code = (status_info.get("endCode") or "").upper()
        completion_description = status_info.get("completionDescription", "")
        devices = status_info.get("devices", [])

        print(f"Status: {status}, End code: {end_code}")
        print(f"Devices: {devices}")

        if status == "COMPLETED":
            if end_code == "SUCCESS":
                write_junit("passed", report_url)
                print("✅ Test passed, exiting 0")
                exit(0)
            else:
                write_junit("failed", report_url, completion_description or "Test failed")
                print(f"❌ Test failed: {completion_description}, exiting 1")
                exit(1)

        elif status in ["FAILED", "STOPPED"]:
            write_junit("failed", report_url, completion_description or "Test did not complete successfully")
            print(f"❌ Test ended prematurely: {completion_description}, exiting 1")
            exit(1)

        # Still running
        print("⏳ Test still running, waiting 10s...")
        time.sleep(10)


if __name__ == "__main__":
    main()