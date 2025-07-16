import os

# Get BlazeMeter token from environment
token_BMCredentials = os.getenv("BMCredentials")
if not token_BMCredentials:
   raise RuntimeError("❌ Environment variable 'token_BMCredentials' is not set.")
# Use the token as needed
BMCredentials = token_BMCredentials

PerfectoKey = os.getenv("PerfectoToken")
if not token_BMCredentials:
   raise RuntimeError("❌ Environment variable 'token_PerfectoToken' is not set.")
# Use the token as needed
PerfectoKey = token_PerfectoToken

# Blazemeter Environment - Workspace / Account
workspaceID = 2014117
account = 352831

# Blazemeter Mock Service Details
ServiceID = "338990"
MockThinkTime = "60"
TemplateID = "6183"
SharedFolderID = "65d71b4e8b2044fe570baf89"
RegistrationMockID = "144333"


# Blazemeter Environment - Workspace / Account
workspaceID = 2014117
account = 352831

# Blazemeter Mock Service Details
ServiceID = "338990"
MockThinkTime = "60"
TemplateID = "6183"
SharedFolderID = "65d71b4e8b2044fe570baf89"
RegistrationMockID = "144333"

BMCredentials = (
    token_BMCredentials )

PerfectoKey = "token_perfectotoken"

base_dir = '/var/lib/jenkins/workspace/DBank Mobile Pipeline/auto/results/'
mock_output = f'{base_dir}mockurl.txt'
mock_output_id = f'{base_dir}mockid.txt'
test_result_url = f'{base_dir}test_result.txt'
test_data_csv = f'{base_dir}test_data.csv'

BMCredentials = (
    token_BMCredentials )

PerfectoKey = "token_perfectotoken"

base_dir = '/var/lib/jenkins/workspace/DBank Mobile Pipeline/auto/results/'
mock_output = f'{base_dir}mockurl.txt'
mock_output_id = f'{base_dir}mockid.txt'
test_result_url = f'{base_dir}test_result.txt'
test_data_csv = f'{base_dir}test_data.csv'
