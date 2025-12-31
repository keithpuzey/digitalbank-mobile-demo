import requests
import time
import os
import sys
from datetime import datetime, timezone
from config import PerfectoKey

# -----------------------------
# Config
# -----------------------------
PERFECTO_CLOUD = "demo.perfectomobile.com"
SCRIPT_KEY = "PUBLIC:DBankMobileRegistrationDelphix.xml"
SERVER_URL = os.environ.get("DemoServerURL", "http://dbankdemo.com/bank")
POLL_INTERVAL = 10

HEADERS = {
    "Perfecto-Authorization": PerfectoKey,
    "Content-Type": "application/json"
}

# -----------------------------
# Start execution
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
    response.raise_for_status()

    data = response.json()
    execution_id = data.get("executionId")

    if not execution_id:
        raise RuntimeError("No executionId returned from Perfecto")

    print(f"🧪 Execution ID: {execution_id}")
    return execution_id

# -----------------------------
# Get status
# -----------------------------
def get_status(execution_id):
    url = f"https://{PERFECTO_CLOUD}/scriptless/api/executions/{execution_id}"
    response = requests.get(url, headers=HEADERS)
    response.raise_for_status()
    return response.json()

# -----------------------------
# Main
# -----------------------------
def main():
    execution_id = start_test()

    print("🕒 Polling execution status...")

    while True:
        status_response = get_status(execution_id)

        # 🔍 Debug – keep this while stabilising
        print("🔍 Full status response:", status_response)

        status = (status_response.get("status") or "").upper()
        end_code = status_response.get("endCode")
        report_key = status_response.get("reportKey")

        devices = status_response.get("devices") or []
        platform_name = None
        platform_version = None

        if devices:
            platform_name = devices[0].get("platformName")
            platform_version = devices[0].get("platformVersion")

        print(
            f"{datetime.now(timezone.utc).isoformat()} | "
            f"Status: {status} | EndCode: {end_code}"
        )

        if status == "COMPLETED":
            print("\n📊 FINAL RESULT")
            print("----------------------------")
            print(f"Report URL      : {report_key}")
            print(f"Platform        : {platform_name}")
            print(f"Platform Version: {platform_version}")
            print(f"End Code        : {end_code}")

            if end_code == "SUCCESS":
                print("✅ Test PASSED")
                sys.exit(0)
            else:
                print("❌ Test FAILED")
                sys.exit(1)

        print(f"⏳ Still running, waiting {POLL_INTERVAL}s...\n")
        time.sleep(POLL_INTERVAL)

# -----------------------------
if __name__ == "__main__":
    main()