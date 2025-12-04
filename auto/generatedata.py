import json
import requests
import sys
import base64

# ----------------------------------------------------------
# Read credential (username:secret) passed from Jenkins
# ----------------------------------------------------------
# Example: BMCredentials="user123:abcd1234"
from credentials import BMCredentials   # <-- your Jenkins-injected file

try:
    username, secret = BMCredentials.split(":")
except ValueError:
    print("ERROR: BMCredentials is not in username:secret format")
    sys.exit(1)

# ----------------------------------------------------------
# Build headers
# ----------------------------------------------------------
def auth_header(username, secret):
    token = f"{username}:{secret}".encode("utf-8")
    b64 = base64.b64encode(token).decode("utf-8")
    return {"Authorization": f"Basic {b64}"}

# Combine with content headers
base_headers = {
    "Content-Type": "application/json",
    "Accept": "application/json,text/javascript,*/*"
}


# ----------------------------------------------------------
# CLI Inputs
# ----------------------------------------------------------
if len(sys.argv) != 3:
    print("Usage: python generatedata.py <data-model.json> <records>")
    sys.exit(1)

model_file = sys.argv[1]
count = sys.argv[2]

workspace_id = "<YOUR WORKSPACE>"
tdm_url = "https://tdm.blazemeter.com/api/v1/workspaces"


# ----------------------------------------------------------
# Load data model
# ----------------------------------------------------------
with open(model_file, "r") as f:
    datamodel = json.load(f)

datamodel["recordCount"] = int(count)


# ----------------------------------------------------------
# Step 1: Generate Data File
# ----------------------------------------------------------
generate_url = f"{tdm_url}/{workspace_id}/testdata/generatefile?entity=default"

print("➡️ Calling BlazeMeter GenerateFile...")

gen_resp = requests.post(
    generate_url,
    json=datamodel,
    headers={**base_headers, **auth_header(username, secret)}
)

if gen_resp.status_code != 200:
    print("❌ GenerateFile failed:", gen_resp.text)
    sys.exit(1)

gen_json = gen_resp.json()
object_key = gen_json.get("objectKey")

print("✔️ Generated objectKey:", object_key)


# ----------------------------------------------------------
# Step 2: Download the generated CSV file
# ----------------------------------------------------------
download_url = f"{tdm_url}/objects/download/{object_key}"

print("➡️ Downloading generated CSV...")

csv_resp = requests.get(
    download_url,
    headers=auth_header(username, secret)
)

if csv_resp.status_code != 200:
    print("❌ Download failed:", csv_resp.text)
    sys.exit(1)

with open("test_data.csv", "wb") as f:
    f.write(csv_resp.content)

print("✔️ CSV saved as test_data.csv")
