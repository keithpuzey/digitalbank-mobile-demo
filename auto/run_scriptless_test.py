import requests
import time
import sys
import os
import xml.etree.ElementTree as ET
from datetime import datetime

# =========================
# CONFIGURATION
# =========================
PERFECTO_CLOUD = "demo.app.perfectomobile.com"
PERFECTO_TOKEN = os.environ.get("PERFECTO_TOKEN")

SCRIPTLESS_EXECUTION_ID = os.environ.get("PERFECTO_EXECUTION_ID")
POLL_INTERVAL = 10
JUNIT_PATH = "test-results/perfecto-result.xml"

HEADERS = {
    "Authorization": f"Bearer {PERFECTO_TOKEN}",
    "Content-Type": "application/json"
}

# =========================
# HELPERS
# =========================
def get_execution_status(execution_id):
    url = f"https://{PERFECTO_CLOUD}/scriptless/api/executions/{execution_id}"
    response = requests.get(url, headers=HEADERS)
    response.raise_for_status()
    return response.json()


def write_junit(report_key, platform_name, platform_version, end_code):
    os.makedirs(os.path.dirname(JUNIT_PATH), exist_ok=True)

    testsuite = ET.Element(
        "testsuite",
        name="Perfecto Scriptless Test",
        tests="1",
        failures="1" if end_code != "SUCCESS" else "0",
        time="0"
    )

    testcase = ET.SubElement(
        testsuite,
        "testcase",
        classname="perfecto.scriptless",
        name=f"{platform_name} {platform_version}"
    )

    if end_code != "SUCCESS":
        failure = ET.SubElement(
            testcase,
            "failure",
            message="Perfecto test failed"
        )
        failure.text = f"Report: {report_key}"

    system_out = ET.SubElement(testcase, "system-out")
    system_out.text = (
        f"Report URL: {report_key}\n"
        f"Platform: {platform_name} {platform_version}\n"
        f"End Code: {end_code}"
    )

    tree = ET.ElementTree(testsuite)
    tree.write(JUNIT_PATH, encoding="utf-8", xml_declaration=True)

    print(f"📄 JUnit written to {JUNIT_PATH}")


# =========================
# MAIN POLLING LOOP
# =========================
print(f"🚀 Monitoring Perfecto execution: {SCRIPTLESS_EXECUTION_ID}")

while True:
    status_response = get_execution_status(SCRIPTLESS_EXECUTION_ID)

    print("🔍 Full status response:", status_response)

    status = (status_response.get("status") or "").upper()
    end_code = status_response.get("endCode", "")
    report_key = status_response.get("reportKey")

    devices = status_response.get("devices") or []
    platform_name = "Unknown"
    platform_version = "Unknown"

    if devices:
        platform_name = devices[0].get("platformName", "Unknown")
        platform_version = devices[0].get("platformVersion", "Unknown")

    print(
        f"{datetime.utcnow().isoformat()} "
        f"📱 Platform: {platform_name} {platform_version} | "
        f"Status: {status} | End Code: {end_code}"
    )

    if status == "COMPLETED":
        print("\n📊 FINAL TEST SUMMARY")
        print("--------------------")
        print(f"Report URL: {report_key}")
        print(f"Platform: {platform_name} {platform_version}")
        print(f"End Code: {end_code}")

        write_junit(report_key, platform_name, platform_version, end_code)

        if end_code.upper() == "SUCCESS":
            print("✅ Test passed — exiting 0")
            sys.exit(0)
        else:
            print("❌ Test failed — exiting 1")
            sys.exit(1)

    print(f"⏳ Test still running, waiting {POLL_INTERVAL}s...\n")
    time.sleep(POLL_INTERVAL)