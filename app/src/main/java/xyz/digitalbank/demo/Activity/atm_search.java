package xyz.digitalbank.demo.Activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import xyz.digitalbank.demo.R;
import android.view.View;
import android.widget.Button;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;
import android.widget.TextView;


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
        Button atmSearchButton = findViewById(R.id.action_atm_search);

        // Set an OnClickListener for the button
        atmSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method when the button is clicked
                getLocationAndMakeRequest();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_atm_search:
                // Handle ATM search icon click
                getLocationAndMakeRequest();
                return true;
            // Add other cases if needed
            default:
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
                            Log.d("LocationUpdates", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());

                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String apiUrl = "http://digitalbank322871.mock-eu.blazemeter.com/gps?type=atm&lat="+latitude+"&lon="+longitude;

                            // Perform network request on a separate thread
                            // Log the API URL
                            Log.d("NetworkRequest", "API URL: " + apiUrl);

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

                                    runOnUiThread(() -> {

                                        TextView responseTextView = findViewById(R.id.responseTextView);
                                        responseTextView.setText(response.toString());
                                    });

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
        }
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


