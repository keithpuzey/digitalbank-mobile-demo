package xyz.digitalbank.demo.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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

import xyz.digitalbank.demo.Constants.ConstantsManager;
import xyz.digitalbank.demo.R;


public class AtmSearchFragment extends Fragment implements View.OnClickListener  {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private CheckBox checkbox1, checkbox2, checkbox3;
    private Button getLocationButton;
    private TextView responseTextView;

    private Button updateLocationButton;
    private View view;

    private Context context;  // Declare a context variable


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.atm_search, container, false);
        if (view == null) {
            throw new NullPointerException("Failed to inflate the layout for AtmSearchFragment");
        }
        context = getContext();
        String MOCK_URL = ConstantsManager.getMockUrl(requireContext());
        responseTextView = view.findViewById(R.id.responseTextView);
        updateLocationButton = view.findViewById(R.id.updateLocationButton);
      //  updateLocationButton = getView().findViewById(R.id.updateLocationButton);




        // Check and request location permission if not granted
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // Permission is granted, proceed to get location

        }

        checkbox1 = view.findViewById(R.id.checkbox1);
        checkbox2 = view.findViewById(R.id.checkbox2);
        checkbox3 = view.findViewById(R.id.checkbox3);
        getLocationButton = view.findViewById(R.id.getLocationButton);

        // Initially hide responseTextView
        responseTextView.setVisibility(View.GONE);

        

        // Set click listeners for checkboxes
        checkbox1.setOnClickListener(this);
        checkbox2.setOnClickListener(this);
        checkbox3.setOnClickListener(this);

        // Set click listener for getLocationButton
        getLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Perform action based on selected checkbox
                if (!checkbox1.isChecked() && !checkbox2.isChecked() && !checkbox3.isChecked()) {
                    // Display a message indicating no checkbox is selected
                    Toast.makeText(requireContext(), "Please select  one option", Toast.LENGTH_SHORT).show();
                    return; // Exit the method early

                } else if (checkbox1.isChecked()) {
                    responseTextView.setVisibility(View.VISIBLE);
                    getLocationButton.setVisibility(View.GONE);
                    updateLocationButton.setVisibility(View.VISIBLE);
                    handleAtmLocationGPSClick();
                        // Example: startActivity(intentForCheckbox1);

                } else if (checkbox2.isChecked()) {
                    responseTextView.setVisibility(View.VISIBLE);
                    getLocationButton.setVisibility(View.GONE);
                    updateLocationButton.setVisibility(View.VISIBLE);
                    handleAtmLocationNetworkClick();
                    // Example: startActivity(intentForCheckbox2);
                } else if (checkbox3.isChecked()) {
                    responseTextView.setVisibility(View.GONE);
                    getLocationButton.setVisibility(View.GONE);
                    updateLocationButton.setVisibility(View.VISIBLE);
                    handleSearchByZipCodeClick();
                    // Example: startActivity(intentForCheckbox3);
                } else {
                    // No checkbox selected
                    // Example: showErrorMessage();
                }
            }
        });
        updateLocationButton.setOnClickListener(v -> {
            // Perform action based on selected checkbox
            if (!checkbox1.isChecked() && !checkbox2.isChecked() && !checkbox3.isChecked()) {
                // Display a message indicating no checkbox is selected
                Toast.makeText(requireContext(), "Please select one option", Toast.LENGTH_SHORT).show();
                return; // Exit the method early
            } else if (checkbox1.isChecked()) {
                responseTextView.setVisibility(View.VISIBLE);
                handleAtmLocationGPSClick();
                // Example: startActivity(intentForCheckbox1);
            } else if (checkbox2.isChecked()) {
                responseTextView.setVisibility(View.VISIBLE);
                handleAtmLocationNetworkClick();
                // Example: startActivity(intentForCheckbox2);
            } else if (checkbox3.isChecked()) {
                responseTextView.setVisibility(View.GONE);
                handleSearchByZipCodeClick();
                // Example: startActivity(intentForCheckbox3);
            } else {
                // No checkbox selected
                // Example: showErrorMessage();
            }
        });
        return view;
    }
    @Override
    public void onClick(View v) {
        // Uncheck all checkboxes
        checkbox1.setChecked(false);
        checkbox2.setChecked(false);
        checkbox3.setChecked(false);

        // Check the clicked checkbox
        CheckBox clickedCheckbox = (CheckBox) v;
        clickedCheckbox.setChecked(true);

        // Handle clicks on checkboxes
        if (v.getId() == R.id.checkbox1) {
            responseTextView.setVisibility(View.GONE);
            updateLocationButton.setVisibility(View.GONE);
            getLocationButton.setVisibility(View.VISIBLE);
            responseTextView.setText("");
        } else if (v.getId() == R.id.checkbox2) {
            responseTextView.setVisibility(View.GONE);
            updateLocationButton.setVisibility(View.GONE);
            getLocationButton.setVisibility(View.VISIBLE);
            responseTextView.setText("");
        } else if (v.getId() == R.id.checkbox3) {
            responseTextView.setVisibility(View.GONE);
            updateLocationButton.setVisibility(View.GONE);
            getLocationButton.setVisibility(View.VISIBLE);
            responseTextView.setText("");
        } else {
            responseTextView.setVisibility(View.GONE);
        }
    }



    private void handleAtmSearchButtonClick() {
        getLocationAndMakeRequest();
    }
    private void handleAtmLocationGPSClick() {
        // Your implementation here
        // This method will be called when the ATM Location - GPS layout is clicked
        getLocationAndMakeRequest();
    }

    private void handleSearchByZipCodeClick() {
        // Your implementation here
        showCustomRequestDialog();
    }


    private void handleAtmLocationNetworkClick() {
        // Your implementation here
        getIpAddress();
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
                             //   Log.d("GPS", "URL =  " + apiUrl );
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
                                    //    Log.d("NetworkResponse", "Response: " + response.toString());

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
                                            //String formattedInfo =
                                            //        "ATM Location - GPS :" + "\n" + "\n" +
                                            //        "Road:" + road + "\n" +
                                            //                "County:" + county + "\n" +
                                            //                "State:" + state + "\n" +
                                            //                "Postcode:" + postcode + "\n" +
                                            //                "Country:" + country + "\n";

//                                            Log.d("FormattedInfo", formattedInfo);


                                            requireActivity().runOnUiThread(() -> {
                                                TextView responseTextView = view.findViewById(R.id.responseTextView);

                                                // Load the cell tower icon drawable
                                                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.outline_gps_fixed_24, null);
                                                drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

                                                // Check if the drawable is loaded successfully
                                                if (drawable != null) {
                                                    // Set bounds for the drawable
                                                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                                                    // Create a SpannableStringBuilder to combine text and image
                                                    SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

                                                    String firstLine = "    ATM Location - GPS";
                                                    spannableBuilder.append(firstLine, new StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                    int maxColumnNameLength = "Postcode:   ".length(); // Adjust based on your actual column names

                                                    String restOfInfo =
                                                            "\n\n" +
                                                                    boldenColumn("Road:           ", road, maxColumnNameLength) + "\n" +
                                                                    boldenColumn("County:       ", county, maxColumnNameLength) + "\n" +
                                                                    boldenColumn("State:           ", state, maxColumnNameLength) + "\n" +
                                                                    boldenColumn("Postcode: ", postcode, maxColumnNameLength) + "\n" +
                                                                    boldenColumn("Country:      ", country, maxColumnNameLength);

                                                    spannableBuilder.append(restOfInfo);


                                                    // Append the cell tower icon to the SpannableStringBuilder
                                                    spannableBuilder.append("  ");
                                                    ImageSpan imageSpan = new ImageSpan(drawable);
                                                    spannableBuilder.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                    // Set the combined text and image to the TextView
                                                    responseTextView.setText(spannableBuilder);
                                                    responseTextView.setTextColor(Color.rgb(24,29,47));
                                                    //  responseTextView.setGravity(Gravity.LEFT);
                                                    responseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                                    responseTextView.setBackgroundColor(Color.rgb(217,217,214));

                                                } else {

                                                }

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
    private CharSequence boldenColumn(String columnName, String columnValue, int maxColumnNameLength) {
        SpannableStringBuilder columnBuilder = new SpannableStringBuilder();

        // Append spaces to align the second column
        int spacesToAdd = maxColumnNameLength - columnName.length();
        String spaces = "";
        for (int i = 0; i < spacesToAdd; i++) {
            spaces += " ";
        }

        // Append the first column name with added spaces
        columnBuilder.append(columnName + spaces);

        // Get the length of the appended column name
        int columnNameLength = columnName.length() + spacesToAdd;

        // Bolden the appended column name
        columnBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, columnNameLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Append the second column value
        columnBuilder.append(columnValue + "\n");

        return columnBuilder;
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
               // Log.d("IpResponse", "Connection String : " + connection);

                // Read the response
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                 //   Log.d("IpResponse", "Process Response : " + bufferedReader.readLine());
                    response.append(line);
                }

                // Log the IP address response
            //    Log.d("IpResponse", "Response: " + response.toString());

                try {
                    // Extract IP address from the JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String ipAddress = jsonResponse.optString("ip", "");




                    // Display the IP address on the screen
                    requireActivity().runOnUiThread(() -> {
                        TextView ipTextView = view.findViewById(R.id.responseTextView);
                       // ipTextView.setText("IP Address: " + ipAddress);
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

                  //  Log.d("FormattedInfo", formattedInfo);

                    // Log the second API response
               //     Log.d("SecondApiResponse", "Response: " + formattedInfo);


                    // Close connections
                    bufferedReader.close();
                    inputStream.close();
                    connection.disconnect();

                    // Third request to another API using the latitude and longitude
                    String gpsApiUrl = ConstantsManager.getMockUrl(requireContext()) + "gps?type=atm&lat=" + lat + "&lon=" + lon;
                  //  Log.d("Coordinates", "Debug: " + gpsApiUrl);
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



                                requireActivity().runOnUiThread(() -> {
                                    TextView responseTextView = view.findViewById(R.id.responseTextView);

                                    // Load the cell tower icon drawable
                                    Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.outline_cell_tower_24, null);
                                    drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);



                                    // Check if the drawable is loaded successfully
                                    if (drawable != null) {
                                        // Set bounds for the drawable
                                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                                        // Create a SpannableStringBuilder to combine text and image
                                        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

                                        String firstLine = "    ATM Location - Network";
                                        spannableBuilder.append(firstLine, new StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                        int maxColumnNameLength = "Postcode:   ".length(); // Adjust based on your actual column names

                                        String restOfInfo =
                                                "\n\n" +
                                                        boldenColumn("Road:               ", gpsroad, maxColumnNameLength) + "\n" +
                                                        boldenColumn("County:            ", gpscounty, maxColumnNameLength) +"\n" +
                                                        boldenColumn("State:               ", gpsstate, maxColumnNameLength) +"\n" +
                                                        boldenColumn("Postcode:       ", gpspostcode, maxColumnNameLength) +"\n" +
                                                        boldenColumn("Country:          ", gpscountry, maxColumnNameLength) +"\n" +
                                                        boldenColumn("IP Address:    ", ipAddress, maxColumnNameLength) ;                                                ;

                                        spannableBuilder.append(restOfInfo);


                                        // Append the cell tower icon to the SpannableStringBuilder
                                        spannableBuilder.append("  ");
                                        ImageSpan imageSpan = new ImageSpan(drawable);
                                        spannableBuilder.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                        // Set the combined text and image to the TextView
                                        responseTextView.setText(spannableBuilder);
                                        responseTextView.setTextColor(Color.rgb(24,29,47));
                                        //  responseTextView.setGravity(Gravity.LEFT);
                                        responseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                        responseTextView.setBackgroundColor(Color.rgb(217,217,214));

                                    } else {

                                    }

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
        // Set text color for the EditText

        input.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));


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

                responseCode = connection.getResponseCode();
             //   Log.d("ZipCode", "Before if statement : " + responseCode);

                InputStream inputStream = (responseCode >= 400) ? connection.getErrorStream() : connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                requireActivity().runOnUiThread(() -> {
                    if (responseCode >= 500) {
                        // Display error message in the initial dialog
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Mock Service Response Code")
                                .setMessage("Service Unavailable :\n\nResponse Code = " + responseCode + "\n")
                                .setPositiveButton("OK", null)
                                .show();
                    }else if (responseCode >= 400) {
                        // Display error message in the initial dialog
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Mock Service Response Code")
                                .setMessage("Zip Code Not found :\n\nResponse Code = " + responseCode + "\n")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    else {
                        // Successful response, process as before
                        handleSuccessfulResponse(response.toString());
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

    private void handleSuccessfulResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
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
                        "ATM Location - Zip Code:" + "\n" +
                                "Name:             " + name + "\n" +
                                "Description:   " + locationDescription + "\n" +
                                "Street :             " + street + "\n" +
                                "City:                  " + city + "\n" +
                                "Country:          " + country + "\n" +
                                "Zip Code:        " + postalCode +"\n";

         //       Log.d("FormattedInfo", zipFormattedInfo);

                TextView responseTextView = view.findViewById(R.id.responseTextView);
                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.outline_search_24, null);
                drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);


                // Check if the drawable is loaded successfully
                if (drawable != null) {
                    // Set bounds for the drawable
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                    // Create a SpannableStringBuilder to combine text and image
                    SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

                    String firstLine = "     ATM Location - GPS";
                    spannableBuilder.append(firstLine, new StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    int maxColumnNameLength = "Postcode:   ".length(); // Adjust based on your actual column names

                    String restOfInfo =
                            "\n\n" +
                                    boldenColumn("Name:           ", name, maxColumnNameLength) + "\n" +
                                    boldenColumn("Street:           ", street, maxColumnNameLength) + "\n" +
                                    boldenColumn("City:               ", city, maxColumnNameLength) + "\n" +
                                    boldenColumn("Postcode:    ", postalCode, maxColumnNameLength) + "\n" +
                                    boldenColumn("Country:        ", country, maxColumnNameLength);

                    spannableBuilder.append(restOfInfo);


                    // Append the cell tower icon to the SpannableStringBuilder
                    spannableBuilder.append("  ");
                    ImageSpan imageSpan = new ImageSpan(drawable);
                    spannableBuilder.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Set the combined text and image to the TextView
                    responseTextView.setText(spannableBuilder);
                    responseTextView.setTextColor(Color.rgb(24,29,47));
                    //  responseTextView.setGravity(Gravity.LEFT);
                    responseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    responseTextView.setBackgroundColor(Color.rgb(217,217,214));
                    responseTextView.setVisibility(View.VISIBLE);

                    // Handle the rest of the information if necessary
                } else {
                    // Handle case when drawable is not loaded
                }
            } else {
                // Handle case when atmsArray is empty
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON exception
        } catch (Exception e) {
            e.printStackTrace();
            // Handle other exceptions
        }
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