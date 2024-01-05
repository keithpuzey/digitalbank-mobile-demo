package xyz.digitalbank.demo.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import xyz.digitalbank.demo.Constants.Constant;
import xyz.digitalbank.demo.Constants.ConstantsManager;
import xyz.digitalbank.demo.R;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.widget.ImageButton;




public class AtmSearchFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private View view;

    private Context context;  // Declare a context variable

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.atm_search, container, false);
        context = getContext();
        String MOCK_URL = ConstantsManager.getMockUrl(requireContext());


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


        // Find buttons
        ImageButton atmSearchButton = view.findViewById(R.id.action_atm_search);
        ImageButton getIpButton = view.findViewById(R.id.action_get_ip);
        ImageButton customRequestButton = view.findViewById(R.id.action_custom_request);

        // Set click listeners
        atmSearchButton.setOnClickListener(v -> handleAtmSearchButtonClick());
        getIpButton.setOnClickListener(v -> getIpAddress());
        customRequestButton.setOnClickListener(v -> showCustomRequestDialog());

        return view;
    }


    private void handleAtmSearchButtonClick() {
        Log.d("ClickAction", "ATM Search Button Clicked!");
        Log.d("ClickAction", "ATM Search Button Clicked at " + System.currentTimeMillis());
        getLocationAndMakeRequest();
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
                                String apiUrl = ConstantsManager.getMockUrl(requireContext()) + "gps?type=atm&lat=" + latitude + "&lon=" + longitude;
                                Log.d("GPS", "URL =  " + apiUrl );
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
                                            String formattedInfo = "Road: " + road + "\n" +

                                                    "County: " + county + "\n" +
                                                    "State: " + state + "\n" +
                                                    "Postcode: " + postcode + "\n" +
                                                    "Country: " + country ;

                                            Log.d("FormattedInfo", formattedInfo);

                                            // Display the formatted information on the screen
                                            requireActivity().runOnUiThread(() -> {
                                                TextView responseTextView = view.findViewById(R.id.responseTextView);
                                                responseTextView.setText("Location Based on GPS :" + "\n" + "\n" + "\n"  + formattedInfo);
                                                responseTextView.setTextColor(Color.rgb(24,29,47));
                                                responseTextView.setGravity(Gravity.CENTER);
                                                responseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

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
    //    String ipApiUrl = "https://api.ipify.org/?format=json";
        String ipApiUrl = " https://api.seeip.org/jsonip?";
        // Perform network request on a separate thread
        new Thread(() -> {
            try {
                URL url = new URL(ipApiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                Log.d("IpResponse", "Connection String : " + connection);

                // Read the response
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Log.d("IpResponse", "Process Response : " + bufferedReader.readLine());
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
                    e.printStackTrace(); // Log the error for debugging
                    requireActivity().runOnUiThread(() -> {
                        // Handle the exception in an appropriate way (e.g., show a Toast)
                        Toast.makeText(requireContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                    });
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
        String secondApiUrl = ConstantsManager.getMockUrl(requireContext()) + "ip?ip=" + ipAddress;

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
                    String formattedInfo =
                            "Country: " + country + "\n" +
                            "Postcode: " + postcode + "\n" +
                            "State: " + state + "\n" +
                            "lat: " + lat + "\n" +
                            "lon: " + lon + "\n";

                    Log.d("FormattedInfo", formattedInfo);

                    // Log the second API response
                    Log.d("SecondApiResponse", "Response: " + formattedInfo);


                    // Close connections
                    bufferedReader.close();
                    inputStream.close();
                    connection.disconnect();

                    // Third request to another API using the latitude and longitude
                    String gpsApiUrl = ConstantsManager.getMockUrl(requireContext()) + "gps?type=atm&lat=" + lat + "&lon=" + lon;
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
                                String gpsformattedInfo =

                                        "Road: " + gpsroad + "\n" +
                                        "County: " + gpscounty + "\n" +
                                        "State: " + gpsstate + "\n" +
                                        "Postcode: " + gpspostcode + "\n" +
                                        "Country: " + gpscountry + "\n";

                                Log.d("FormattedInfo", gpsformattedInfo);

                                // Display the second API response including the IP address
                                requireActivity().runOnUiThread(() -> {
                                    TextView responseTextView = view.findViewById(R.id.responseTextView);
                                    responseTextView.setText("Location Based on Network Location :" + "\n" + ipAddress + "\n"+ "\n"  + gpsformattedInfo);
                                    responseTextView.setTextColor(Color.rgb(24,29,47));
                                    responseTextView.setGravity(Gravity.CENTER);
                                    responseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
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

        // Clear the Screen
        TextView responseTextView = view.findViewById(R.id.responseTextView);
        responseTextView.setText("");

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
        String apiUrl = ConstantsManager.getMockUrl(requireContext()) + "zip?zipcode=" + userInput;

        // Perform network request on a separate thread
        new Thread(() -> {
            int responseCode;  // Declare responseCode variable

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

// Get the HTTP response code
                responseCode = connection.getResponseCode();
                Log.d("ZipCode", "Before if statement : " + responseCode);

// Use getErrorStream() if the response code indicates an error
                InputStream inputStream = (responseCode >= 400) ? connection.getErrorStream() : connection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }


                // Handle the response based on the HTTP response code
                requireActivity().runOnUiThread(() -> {
                    TextView responseTextView = view.findViewById(R.id.responseTextView);

                    Log.d("ZipCode", "Before if statement : " + responseCode);
                    if (responseCode >= 200 && responseCode < 300) {
                        // Successful response
                        try {
                            JSONObject jsonResponse = new JSONObject(response.toString());
                            JSONArray atmsArray = jsonResponse.getJSONArray("atms");

                            // Assuming you want to display details for the first ATM only
                            if (atmsArray.length() > 0) {
                                JSONObject atm = atmsArray.getJSONObject(0);
                                JSONObject atmLocation = atm.getJSONObject("atmLocation");

                                // Extract details
                                String name = atmLocation.getString("name");
                                String locationDescription = atmLocation.getString("locationDescription");
                                String street = atmLocation.getJSONObject("address").getString("street");
                                String city = atmLocation.getJSONObject("address").getString("city");
                                String country = atmLocation.getJSONObject("address").getString("country");
                                String postalCode = atmLocation.getJSONObject("address").getString("postalCode");
                                String state = atmLocation.getJSONObject("address").getString("state");

                                // Build a formatted string with the extracted information
                                String zipFormattedInfo =
                                        "Name: " + name + "\n" +
                                                "Description: " + locationDescription + "\n" +
                                                "Street : " + street + "\n" +
                                                "City: " + city + "\n" +
                                                "Country: " + country + "\n" +
                                                "Zip Code: " + postalCode;

                                Log.d("FormattedInfo", zipFormattedInfo);

                                // Display the response on the screen
                                responseTextView.setText("Location Based on Zip Code :" + "\n" + "\n" + zipFormattedInfo);
                                responseTextView.setTextColor(Color.rgb(24,29,47));
                                responseTextView.setGravity(Gravity.CENTER);
                                responseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (responseCode == 404) {
                            // Handle the case when the resource is not found (ZIP code not found)
                            responseTextView.setText("ZIP code not found.");
                            responseTextView.setTextColor(Color.YELLOW);
                            responseTextView.setGravity(Gravity.CENTER);
                            responseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

                        } else if (responseCode >= 500) {
                            // Display the response on the screen
                            responseTextView.setText("Service Unavailable :" + "\n" + "\n" + " Response Code = " + responseCode + "\n");
                            // Set text color to red
                            responseTextView.setTextColor(Color.RED);
                            responseTextView.setGravity(Gravity.CENTER);
                            responseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        } else {
                            // Handle other error responses
                            responseTextView.setText("Service Error: " + responseCode);
                            responseTextView.setGravity(Gravity.CENTER);
                             responseTextView.setTextColor(Color.BLACK);  // Change color as needed
                            responseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        }
                    }
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