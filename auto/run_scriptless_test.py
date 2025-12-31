import requests
import time
import os
import xml.etree.ElementTree as ET
from config import PerfectoKey

# -----------------------------
# Config
# -----------------------------
perfecto_cloud = "demo.perfectomobile.com"
script_key = "PUBLIC:DBankMobileRegistrationDelphix.xml"

RESULT_DIR = "test-results"
RESULT_FILE = os.path.join(RESULT_DIR, "perfecto-result.xml")
TEST_NAME = "DBankMobileRegistration"

# Jenkins parameter (fallback if not set)
JENKINS_SERVER_URL = os.environ.get("DemoServerURL", "http://dbankdemo.com/bank")

HEADERS = {
    "Content-Type": "application/json"
}

# -----------------------------
# Start the Perfecto test
# -----------------------------
def start_test():
    url = f"https://{perfecto_cloud}/scriptless/api/executions"

    params = {
        "operation": "execute",
        "testKey": script_key,
        "securityToken": PerfectoKey,
        "output.visibility": "public",
        "param.ServerURL": JENKINS_SERVER_URL
    }

    print("🚀 Starting Perfecto test")
    print(f"🔗 Using ServerURL from Jenkins: {JENKINS_SERVER_URL}")

    response = requests.post(url, headers=HEADERS, params=params)

    if response.status_code != 200:
        print("❌ Failed to start test")
        print(response.text)
        return None, None, None, None

    data = response.json()

    execution_id = data.get("executionId")
    report_key = data.get("reportKey")
    test_grid_report_url = data.get("testGridReportUrl")
    single_test_report_url = data.get("singleTestReportUrl")

    print("🧪 Execution ID:", execution_id)
    print("📊 Test Grid Report:", test_grid_report_url)
    print("📄 Single Test Report:", single_test_report_url)

    return execution_id, report_key, test_grid_report_url, single_test_report_url


# -----------------------------
# Poll test status
# -----------------------------
def check_test_status(execution_id):
    url = f"https://{perfecto_cloud}/services/executions/{execution_id}"

    params = {
        "operation": "status",
        "securityToken": PerfectoKey
    }

    response = requests.get(url, params=params)
    response.raise_for_status()

    data = response.json()

    return (
        data.get("status"),
        data.get("flowEndCode"),
        data.get("reportKey"),
        data.get("reason"),
        data.get("devices")
    )


# -----------------------------
# Generate JUnit XML
# -----------------------------
def generate_junit_xml(test_name, result, report_url, reason=None):
    os.makedirs(RESULT_DIR, exist_ok=True)

    failures = "0" if result == "passed" else "1"

    testsuite = ET.Element(
        "testsuite",
        name="Perfecto Scriptless Tests",
        tests="1",
        failures=failures
    )

    testcase = ET.SubElement(
        testsuite,
        "testcase",
        classname="Perfecto",
        name=test_name
    )

    if result != "passed":
        message = f"Test failed. Reason: {reason}" if reason else "Test failed"
        ET.SubElement(testcase, "failure", message=message)

    ET.SubElement(testcase, "system-out").text = f"Report URL: {report_url}"

    tree = ET.ElementTree(testsuite)
    tree.write(RESULT_FILE, encoding="utf-8", xml_declaration=True)

    print(f"📄 JUnit result written to {RESULT_FILE}")


# -----------------------------
# Main
# -----------------------------
def main():
    execution_id, report_key, test_grid_report_url, single_test_report_url = start_test()

    if not execution_id:
        generate_junit_xml(
            TEST_NAME,
            "failed",
            "N/A",
            reason="Failed to start Perfecto test"
        )
        exit(1)

    print("🕒 Waiting for test completion...")

    while True:
        status, flow_end_code, _, reason, devices = check_test_status(execution_id)

        print("Current status:", status)
        print("Flow End Code:", flow_end_code)

        if status and status.lower() in ["completed", "failed", "stopped"]:
            print("✅ Test execution finished")
            print("Devices:", devices)
            print("Report:", single_test_report_url)

            if flow_end_code and flow_end_code.lower() != "passed":
                generate_junit_xml(
                    TEST_NAME,
                    "failed",
                    single_test_report_url,
                    reason
                )
                exit(1)
            else:
                generate_junit_xml(
                    TEST_NAME,
                    "passed",
                    single_test_report_url
                )
                exit(0)

        time.sleep(10)


if __name__ == "__main__":
    main()