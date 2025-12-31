import json
import csv
import requests
from config import workspaceID, BMCredentials, test_data_csv, SharedFolderID

class CSVDataGeneration:
    def __init__(self, datamodel_path, repeat_count):
        self.datamodel_path = datamodel_path
        self.repeat_count = repeat_count

    def generate_test_data(self):
        try:
            # Open Data Model file
            with open(self.datamodel_path, 'r', encoding='utf-8') as datamodel_file:
                datamodel_def = json.load(datamodel_file)

            # Update repeat count in Data Model
            default_obj = datamodel_def['data']['attributes']['model']['entities']['default']
            default_obj['repeat'] = self.repeat_count

            # Generate Test Data
            url = f"https://tdm.blazemeter.com/api/v1/workspaces/{workspaceID}/testdata/generatefile?entity=default"
            headers = {
                'Content-Type': 'application/json',
                'Accept': 'application/json,text/javascript, */*',
            }

            response = requests.post(
                url,
                json=datamodel_def,
                headers=headers,
                auth=BMCredentials
            )
            response.raise_for_status()


            print("📦 Processing test data response")

            result_data = response.json().get("result", {})
            if not result_data:
                print("⚠️ No result data found")
                return

            csv_file_name = result_data.get("fileName", "blazedata-test.csv")
            csv_file_path = f"{test_data_csv}_{csv_file_name}"

            print(f"📄 Output file: {csv_file_path}")
            print("📊 CSV Preview")
            print("─" * 60)

            with open(csv_file_path, "w", newline="", encoding="utf-8") as csv_file:
                csv_writer = csv.writer(csv_file)

                csv_content = result_data.get("content", "")
                csv_reader = csv.reader(csv_content.splitlines())

                for row in csv_reader:
                    csv_writer.writerow(row)
                    print(" | ".join(row))

            print("─" * 60)
            print(f"✅ Data saved to CSV file")

            print(f"\n🧾  {self.repeat_count} Test Data Records generated using Data Model {self.datamodel_path}\n")
            # print(response.text)

            # Make the API request to get the signed URL of the Shared Folder
            # API endpoint URL to download the JSON data
            response = requests.get(
                'https://a.blazemeter.com/api/v4/folders/' + SharedFolderID + '/s3/sign?fileName=test_data.csv_blazedata-test.csv',
                headers=headers,
                auth=BMCredentials
            )

            # Load the JSON response
            data = response.json()

            # Extract the signed URL from the JSON response
            signed_url = data["result"]

            # Use the signed_url to upload the file to the BlazeMeter Shared folder

            with open(csv_file_path, "rb") as file:
                response = requests.put(signed_url, data=file)

                # Check if the upload was successful
                if response.status_code != 200:
                    print(f"Error: Failed to upload file. Status Code: {response.status_code}")
                else:
                    print("File successfully uploaded to BlazeMeter shared folder.")

        except Exception as e:
            print(f"An error occurred: {str(e)}")

if __name__ == "__main__":
    # Assuming command line arguments: datamodel_path repeat_count
    import sys
    datamodel_path = sys.argv[1]
    repeat_count = sys.argv[2]

    csv_data_generation = CSVDataGeneration(datamodel_path, repeat_count)
    csv_data_generation.generate_test_data()
