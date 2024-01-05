package xyz.digitalbank.demo.Activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import xyz.digitalbank.demo.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.widget.EditText;
import android.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Toast;
import xyz.digitalbank.demo.Constants.Constant;
import xyz.digitalbank.demo.Constants.ConstantsManager;





public class atm_search extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atm_search);

        // Check and request location permission if not granted
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // Permission is granted, proceed to get location
            getLocationAndMakeRequest();
        }

        // Find the "ATM Search" button by its ID
        ImageButton atmSearchButton = findViewById(R.id.action_atm_search);
        // Find the "Get IP Address" button by its ID
        ImageButton getIpButton = findViewById(R.id.action_get_ip);
        // Find the "Custom Request" button by its ID
        ImageButton customRequestButton = findViewById(R.id.action_custom_request);



        // Set an OnClickListener for the ATM Search button
        atmSearchButton.setOnClickListener(v -> getLocationAndMakeRequest());

        // Set an OnClickListener for the Get IP Address button
        getIpButton.setOnClickListener(v -> getIpAddress());

        // Set an OnClickListener for the Custom Request button
        customRequestButton.setOnClickListener(v -> showCustomRequestDialog());


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_atm_search) {
            // Handle ATM search icon click
            getLocationAndMakeRequest();
            return true;
        } else {
            // Handle other menu items if needed
            return super.onOptionsItemSelected(item);
        }
    }

    private void getLocationAndMakeRequest() {
        // Get the location manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check if GPS and network location providers are enabled
           if (locationManager != null &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Check if location permission is granted
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Request location updates
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0,
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            // Location obtained, make network request
                            // Log the location information
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String apiUrl = ConstantsManager.getMockUrl(getApplicationContext()) +"gps?type=atm&lat=" + latitude + "&lon=" + longitude;


                            // Perform network request on a separate thread
                            new Thread(() -> {
                                try {
                                    URL url = new URL(apiUrl);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");

                                    // Read the response
                                    InputStream inputStream = connection.getInputStream();
                                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                                    StringBuilder response = new StringBuilder();
                                    String line;
                                    while ((line = bufferedReader.readLine()) != null) {
                                        response.append(line);
                                    }
                                    Log.d("NetworkResponse", "Response: " + response.toString());

                                    try {
                                        JSONObject jsonResponse = new JSONObject(response.toString());

                                        // Extract the "address" object from the JSON response
                                        JSONObject addressObject = jsonResponse.optJSONObject("address");

                                        // Extract relevant information
                                        String country = addressObject.optString("country", "");
                                        String postcode = addressObject.optString("postcode", "");
                                        String state = addressObject.optString("state", "");
                                        String county = addressObject.optString("county", "");
                                        String address = addressObject.optString("address", "");
                                        String road = addressObject.optString("road", "");

                                        // Build a formatted string with the extracted information
                                        String formattedInfo = "Country: " + country + "\n" +
                                                "Postcode: " + postcode + "\n" +
                                                "State: " + state + "\n" +
                                                "County: " + county + "\n" +
                                                "Address: " + address + "\n" +
                                                "Road: " + road;

                                        Log.d("FormattedInfo", formattedInfo);

                                        // Display the formatted information on the screen
                                        runOnUiThread(() -> {
                                            TextView responseTextView = findViewById(R.id.responseTextView);
                                            responseTextView.setText(formattedInfo);
                                        });

                                    } catch (JSONException e) {
                                        e.printStackTrace(); // Handle the exception in an appropriate way
                                    }

                                    // Close connections
                                    bufferedReader.close();
                                    inputStream.close();
                                    connection.disconnect();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).start();

                            // Remove location updates to conserve battery
                            locationManager.removeUpdates(this);
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                    });
            } else {
                // Location permission not granted, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
           } else {
               // GPS or network provider not enabled, handle accordingly
               // ... (handle the case where providers are not enabled)
           }
    }




    private void getIpAddress() {
        // URL for the IP address API
        String ipApiUrl = "https://api.ipify.org/?format=json";

        // Perform network request on a separate thread
        new Thread(() -> {
            try {
                URL url = new URL(ipApiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                // Log the IP address response
                Log.d("IpResponse", "Response: " + response.toString());

                try {
                    // Extract IP address from the JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String ipAddress = jsonResponse.optString("ip", "");

                    // Display the IP address on the screen
                    runOnUiThread(() -> {
                        TextView ipTextView = findViewById(R.id.responseTextView);
                        ipTextView.setText("IP Address: " + ipAddress);
                    });

                    // Make a second network request with the obtained IP address
                    getDetailsForIpAddress(ipAddress);

                } catch (JSONException e) {
                    e.printStackTrace(); // Handle the exception in an appropriate way
                }

                // Close connections
                bufferedReader.close();
                inputStream.close();
                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getDetailsForIpAddress(String ipAddress) {
        // URL for the second API
        String secondApiUrl = ConstantsManager.getMockUrl(getApplicationContext()) + "ip?ip=" + ipAddress;

        // Perform the second network request on a separate thread
        new Thread(() -> {
            try {
                URL url = new URL(secondApiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    // Extract relevant information
                    String country = jsonResponse.optString("country", "");
                    String postcode = jsonResponse.optString("zip", "");
                    String state = jsonResponse.optString("region", "");
                    String lat = jsonResponse.optString("lat", "");
                    String lon = jsonResponse.optString("lon", "");

                    // Build a formatted string with the extracted information
                    String formattedInfo = "Country: " + country + "\n" +
                            "Postcode: " + postcode + "\n" +
                            "State: " + state + "\n" +
                            "lat: " + lat + "\n" +
                            "lon: " + lon + "\n";

                    Log.d("FormattedInfo", formattedInfo);

                    // Log the second API response
                    Log.d("SecondApiResponse", "Response: " + formattedInfo);


                    // Display the second API response including the IP address
                    runOnUiThread(() -> {
                        TextView responseTextView = findViewById(R.id.responseTextView);
                        responseTextView.setText("Response for IP Address " + ipAddress + ":\n" + formattedInfo);
                    });

                    // Close connections
                    bufferedReader.close();
                    inputStream.close();
                    connection.disconnect();

                    // Third request to another API using the latitude and longitude
                    String gpsApiUrl = ConstantsManager.getMockUrl(getApplicationContext()) + "gps?type=atm&lat="+lat+"&lon="+ lon;
                    Log.d("Coordinates", "Debug: " + gpsApiUrl );
                    new Thread(() -> {
                        try {
                            URL urlgps = new URL(gpsApiUrl);
                            HttpURLConnection gpsconnection = (HttpURLConnection) urlgps.openConnection();
                            gpsconnection.setRequestMethod("GET");

                            // Read the response
                            InputStream gpsinputStream = gpsconnection.getInputStream();
                            BufferedReader gpsbufferedReader = new BufferedReader(new InputStreamReader(gpsinputStream));
                            StringBuilder gpsresponse = new StringBuilder();
                            String gpsline;
                            while ((gpsline = gpsbufferedReader.readLine()) != null) {
                                gpsresponse.append(gpsline);
                            }
                            // Handle the response as needed
                            Log.d("GPSApiResponse", "Response: " + gpsresponse.toString());
                            try {
                                JSONObject gpsjsonResponse = new JSONObject(gpsresponse.toString());

                                // Extract the "address" object from the JSON response
                                JSONObject gpsaddressObject = gpsjsonResponse.optJSONObject("address");

                                // Extract relevant information
                                String gpscountry = gpsaddressObject.optString("country", "");
                                String gpspostcode = gpsaddressObject.optString("postcode", "");
                                String gpsstate = gpsaddressObject.optString("state", "");
                                String gpscounty = gpsaddressObject.optString("county", "");
                                String gpsaddress = gpsaddressObject.optString("address", "");
                                String gpsroad = gpsaddressObject.optString("road", "");

                                // Build a formatted string with the extracted information
                                String gpsformattedInfo = "Country: " + gpscountry + "\n" +
                                        "Postcode: " + gpspostcode + "\n" +
                                        "State: " + gpsstate + "\n" +
                                        "County: " + gpscounty + "\n" +
                                        "Address: " + gpsaddress + "\n" +
                                        "Road: " + gpsroad;

                                Log.d("FormattedInfo", gpsformattedInfo);

                                // Display the second API response including the IP address
                                runOnUiThread(() -> {
                                    TextView responseTextView = findViewById(R.id.responseTextView);
                                    responseTextView.setText("Response for IP / GPS Location " + ipAddress + ":\n" + gpsformattedInfo);
                                });

                            } catch (JSONException e) {
                                e.printStackTrace(); // Handle the exception in an appropriate way
                            }





                            // Close connections
                            bufferedReader.close();
                            inputStream.close();
                            connection.disconnect();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();


                } catch (JSONException e) {
                    e.printStackTrace(); // Handle the exception in an appropriate way
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }



         private void showCustomRequestDialog() {
            // Create an AlertDialog with an EditText for user input
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Zip Code");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String userInput = input.getText().toString().trim();
                if (!TextUtils.isEmpty(userInput)) {
                    // Perform the custom request with the entered text
                    performCustomRequest(userInput);
                } else {
                    // Handle empty input (e.g., show a message to the user)
                    Toast.makeText(this, "Enter Zip code", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        }

        private void performCustomRequest(String userInput) {
            // Construct the URL with the user input
            String apiUrl = ConstantsManager.getMockUrl(getApplicationContext()) + "zip?zipcode=" + userInput;

            // Perform network request on a separate thread
            new Thread(() -> {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Read the response
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }

                    // Handle the response as needed
                    Log.d("CustomRequest", "Response: " + response.toString());

                    // Display the custom request response
                    runOnUiThread(() -> {
                        TextView responseTextView = findViewById(R.id.responseTextView);
                        responseTextView.setText("Nearest ATM:\n" + response.toString());
                    });

                    // Close connections
                    bufferedReader.close();
                    inputStream.close();
                    connection.disconnect();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to get location
                getLocationAndMakeRequest();
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
        }
    }
}
