import requests
import time
import os
import xml.etree.ElementTree as ET
from datetime import datetime
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
    payload = {"testKey": SCRIPT_KEY, "params": {"ServerURL": SERVER_URL}}

    print(f"{datetime.now()} 🚀 Starting Perfecto Scriptless test")
    print(f"{datetime.now()} 🔗 ServerURL: {SERVER_URL}")

    response = requests.post(url, headers=HEADERS, json=payload)
    if response.status_code != 201:
        print(f"{datetime.now()} ❌ Failed to start test")
        print(response.text)
        return None, None

    data = response.json()
    execution_id = data.get("executionId")
    report_url = data.get("testGridReportUrl")
    print(f"{datetime.now()} 🧪 Execution ID: {execution_id}")
    print(f"{datetime.now()} 📊 Report URL: {report_url}")
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
        "flowEndCode": data.get("flowEndCode"),
        "completionDescription": data.get("completionDescription"),
        "reason": data.get("reason"),
        "devices": data.get("devices", []),
        "reportKey": data.get("reportKey")
    }

# -----------------------------
# Generate JUnit XML
# -----------------------------
def write_junit(result, report_url, reason=None, devices=None, flow_end_code=None, timestamp=None):
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
        ET.SubElement(case, "failure", message=reason or "Test failed")

    # Device info with timestamps
    device_info = ""
    if devices:
        device_info = "\n".join([f"{d.get('model')} by {d.get('manufacturer')} (ID: {d.get('id')})" for d in devices])

    ET.SubElement(case, "system-out").text = (
        f"Timestamp: {timestamp}\nReport: {report_url}\nFlowEndCode: {flow_end_code}\nReason: {reason}\nDevices:\n{device_info}"
    )

    tree = ET.ElementTree(suite)
    tree.write(RESULT_FILE, encoding="utf-8", xml_declaration=True)
    print(f"{datetime.now()} 📄 JUnit written to {RESULT_FILE}")

# -----------------------------
# Main
# -----------------------------
def main():
    execution_id, report_url = start_test()
    if not execution_id:
        write_junit("failed", "N/A", "Failed to start execution", timestamp=datetime.now().isoformat())
        print(f"{datetime.now()} ❌ Failed to start execution, exiting 1")
        exit(1)

    print(f"{datetime.now()} 🕒 Polling execution status...")

    max_retries = 3
    retry_count = 0

    while True:
        try:
            status_info = get_status(execution_id)
        except Exception as e:
            print(f"{datetime.now()} ❌ Error fetching status: {e}")
            retry_count += 1
            if retry_count > max_retries:
                write_junit("failed", report_url, f"Failed to fetch status after {max_retries} attempts", timestamp=datetime.now().isoformat())
                exit(1)
            print(f"{datetime.now()} ⏳ Retrying in 10s... (attempt {retry_count}/{max_retries})")
            time.sleep(10)
            continue

        timestamp = datetime.now().isoformat()
        status = (status_info.get("status") or "").upper()
        flow_end_code = (status_info.get("flowEndCode") or "").upper()
        reason = status_info.get("reason") or ""
        completion_desc = status_info.get("completionDescription") or ""
        devices = status_info.get("devices", [])

        # Display device info in console
        if devices:
            for d in devices:
                print(f"{timestamp} 📱 Device: {d.get('model')} by {d.get('manufacturer')} (ID: {d.get('id')})")
        else:
            print(f"{timestamp} 📱 Devices: None")

        print(f"{timestamp} Status: {status}, Flow End Code: {flow_end_code}, Reason: {reason}")

        if status == "COMPLETED":
            if flow_end_code == "SUCCESS":
                write_junit("passed", report_url, reason, devices, flow_end_code, timestamp)
                print(f"{timestamp} ✅ Test passed, exiting 0")
                exit(0)
            else:
                write_junit("failed", report_url, f"{completion_desc} | Reason: {reason}", devices, flow_end_code, timestamp)
                print(f"{timestamp} ❌ Test failed: {completion_desc} | Reason: {reason}, exiting 1")
                exit(1)
        elif status in ["FAILED", "STOPPED"]:
            write_junit("failed", report_url, f"{completion_desc} | Reason: {reason}", devices, flow_end_code, timestamp)
            print(f"{timestamp} ❌ Test ended prematurely: {completion_desc} | Reason: {reason}, exiting 1")
            exit(1)

        print(f"{timestamp} ⏳ Test still running, waiting 10s...")
        time.sleep(10)

if __name__ == "__main__":
    main()