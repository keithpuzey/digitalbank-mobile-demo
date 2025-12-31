import requests
import time
import os
import sys
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

POLL_INTERVAL = 10

# -----------------------------
# Start test execution
# -----------------------------
def start_test():
    url = f"https://{PERFECTO_CLOUD}/scriptless/api/executions"

    payload = {
        "testKey": SCRIPT_KEY,
        "params": {
            "ServerURL": SERVER_URL
        }
    }

    print("🚀 Starting Perfecto Scriptless test")
    print(f"🔗 ServerURL: {SERVER_URL}")

    response = requests.post(url, headers=HEADERS, json=payload)

    if response.status_code != 201:
        print("❌ Failed to start test")
        print(response.text)
        return None

    data = response.json()
    execution_id = data.get("executionId")

    print("🧪 Execution ID:", execution_id)
    return execution_id

# -----------------------------
# Get execution status
# -----------------------------
def get_status(execution_id):
    url = f"https://{PERFECTO_CLOUD}/scriptless/api/executions/{execution_id}"
    response = requests.get(url, headers=HEADERS)
    response.raise_for_status()
    return response.json()

# -----------------------------
# Write JUnit
# -----------------------------
def write_junit(end_code, report_key, device_summary):
    os.makedirs(RESULT_DIR, exist_ok=True)

    failures = "0" if end_code == "SUCCESS" else "1"

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

    if end_code != "SUCCESS":
        failure = ET.SubElement(case, "failure", message="Test failed")
        failure.text = f"Report: {report_key}"

    system_out = ET.SubElement(case, "system-out")
    system_out.text = (
        f"End Code: {end_code}\n"
        f"Device: {device_summary}\n"
        f"Report: {report_key}"
    )

    ET.ElementTree(suite).write(RESULT_FILE, encoding="utf-8", xml_declaration=True)
    print(f"📄 JUnit written to {RESULT_FILE}")

# -----------------------------
# Main
# -----------------------------
def main():
    execution_id = start_test()

    if not execution_id:
        print("❌ No execution ID returned")
        sys.exit(1)

    print("🕒 Polling execution status...")

    while True:
        status_response = get_status(execution_id)

        # 🔍 DEBUG — keep this
        print("🔍 Full status response:", status_response)

        status = (status_response.get("status") or "").upper()
        end_code = status_response.get("endCode", "")
        report_key = status_response.get("reportKey")

        devices = status_response.get("devices") or []
        device_summary = "None"

        if devices:
            d = devices[0]
            device_summary = (
                f"{d.get('platformName')} "
                f"{d.get('platformVersion')} "
                f"(Device ID: {d.get('deviceName')})"
            )

        print(
            f"{datetime.utcnow().isoformat()} | "
            f"Status: {status} | End Code: {end_code} | Device: {device_summary}"
        )

        if status == "COMPLETED":
            print("\n📊 FINAL RESULT")
            print("--------------------")
            print(f"Report URL: {report_key}")
            print(f"Device: {device_summary}")
            print(f"End Code: {end_code}")

            write_junit(end_code, report_key, device_summary)

            if end_code == "SUCCESS":
                print("✅ Test PASSED")
                sys.exit(0)
            else:
                print("❌ Test FAILED")
                sys.exit(1)

        print(f"⏳ Test still running, waiting {POLL_INTERVAL}s...\n")
        time.sleep(POLL_INTERVAL)

# -----------------------------
if __name__ == "__main__":
    main()