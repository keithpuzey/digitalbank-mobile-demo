import os

# Get tokens from Jenkins environment
BMCredentials = os.getenv("BMCredentials")
if not BMCredentials:
    raise RuntimeError("❌ Environment variable 'BMCredentials' is not set.")

PerfectoKey = os.getenv("PerfectoToken")
if not PerfectoKey:
    raise RuntimeError("❌ Environment variable 'PerfectoToken' is not set.")

# Blazemeter Environment - Workspace / Account
workspaceID = 2014117
account = 352831

# Blazemeter Mock Service Details
ServiceID = "338990"
MockThinkTime = "60"
TemplateID = "6183"
SharedFolderID = "65d71b4e8b2044fe570baf89"
RegistrationMockID = "144333"

# Result paths
base_dir = '/var/lib/jenkins/workspace/DBank Mobile Pipeline/auto/results/'
mock_output = f'{base_dir}mockurl.txt'
mock_output_id = f'{base_dir}mockid.txt'
test_result_url = f'{base_dir}test_result.txt'
test_data_csv = f'{base_dir}test_data.csv'