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
def main():
    execution_id, report_url = start_test()

    if not execution_id:
        write_junit("failed", "N/A", "Failed to start execution")
        exit(1)

    print("🕒 Polling execution status...")

    while True:
        status_info = get_status(execution_id)

        status = status_info["status"].upper()  # Normalize case
        end_code = status_info.get("endCode", "").upper()  # Normalize case

        print(f"Status: {status}")

        if status == "COMPLETED":
            print("Devices:", status_info.get("devices", []))
            print("End code:", end_code)

            if end_code == "SUCCESS":
                write_junit("passed", report_url)
                print("✅ Test passed, exiting 0")
                exit(0)
            else:
                reason = status_info.get("completionDescription", "Test failed")
                write_junit("failed", report_url, reason)
                print(f"❌ Test failed: {reason}, exiting 1")
                exit(1)

        elif status in ["FAILED", "STOPPED"]:
            reason = status_info.get("completionDescription", "Test did not complete successfully")
            write_junit("failed", report_url, reason)
            print(f"❌ Test ended prematurely: {reason}, exiting 1")
            exit(1)

        # If still running
        print("⏳ Test still running, waiting 10s...")
        time.sleep(10)


if __name__ == "__main__":
    main()