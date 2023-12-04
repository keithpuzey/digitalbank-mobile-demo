package xyz.digitalbank.demo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import xyz.digitalbank.demo.R;
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
import android.widget.TextView;
import xyz.digitalbank.demo.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.json.JSONException;
import org.json.JSONObject;
import xyz.digitalbank.demo.R;
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
import androidx.core.content.ContextCompat;


public class AtmSearchFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.atm_search, container, false);

        // Check and request location permission if not granted
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // Permission is granted, proceed to get location
            getLocationAndMakeRequest();
        }

        // Find the "ATM Search" button by its ID
        Button atmSearchButton = view.findViewById(R.id.action_atm_search);
        // Find the "Get IP Address" button by its ID
        Button getIpButton = view.findViewById(R.id.action_get_ip);
        // Find the "Custom Request" button by its ID
        Button customRequestButton = view.findViewById(R.id.action_custom_request);

        // Set an OnClickListener for the ATM Search button
        atmSearchButton.setOnClickListener(v -> getLocationAndMakeRequest());

        // Set an OnClickListener for the Get IP Address button
        getIpButton.setOnClickListener(v -> getIpAddress());

        // Set an OnClickListener for the Custom Request button
        customRequestButton.setOnClickListener(v -> showCustomRequestDialog());

        return view;
    }

    private void getLocationAndMakeRequest() {
        // Get the location manager
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        // Check if permissions are granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Check if GPS and network location providers are enabled
            if (locationManager != null &&
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

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
                                String apiUrl = "http://digitalbank322871.mock-eu.blazemeter.com/gps?type=atm&lat=" + latitude + "&lon=" + longitude;

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
                                            requireActivity().runOnUiThread(() -> {
                                                TextView responseTextView = view.findViewById(R.id.responseTextView);
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
                // Handle case when providers are not enabled
            }
        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
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
                    requireActivity().runOnUiThread(() -> {
                        TextView ipTextView = view.findViewById(R.id.responseTextView);
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
        String secondApiUrl = "http://digitalbank322871.mock-eu.blazemeter.com/ip?ip=" + ipAddress;

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
                    requireActivity().runOnUiThread(() -> {
                        TextView responseTextView = view.findViewById(R.id.responseTextView);
                        responseTextView.setText("Response for IP Address " + ipAddress + ":\n" + formattedInfo);
                    });

                    // Close connections
                    bufferedReader.close();
                    inputStream.close();
                    connection.disconnect();

                    // Third request to another API using the latitude and longitude
                    String gpsApiUrl = "http://digitalbank322871.mock-eu.blazemeter.com/gps?type=atm&lat=" + lat + "&lon=" + lon;
                    Log.d("Coordinates", "Debug: " + gpsApiUrl);
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
                                requireActivity().runOnUiThread(() -> {
                                    TextView responseTextView = view.findViewById(R.id.responseTextView);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Zip Code");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String userInput = input.getText().toString().trim();
            if (!TextUtils.isEmpty(userInput)) {
                // Perform the custom request with the entered text
                performCustomRequest(userInput);
            } else {
                // Handle empty input (e.g., show a message to the user)
                Toast.makeText(requireContext(), "Enter Zip code", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void performCustomRequest(String userInput) {
        // Construct the URL with the user input
        String apiUrl = "http://digitalbank322871.mock-eu.blazemeter.com/zip?zipcode=" + userInput;

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
                requireActivity().runOnUiThread(() -> {
                    TextView responseTextView = view.findViewById(R.id.responseTextView);
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
                // Permission granted, call your location-related method again
                getLocationAndMakeRequest();
            } else {
                // Permission denied, handle accordingly
                // You may want to inform the user or provide an alternative flow
            }
        }
    }
}