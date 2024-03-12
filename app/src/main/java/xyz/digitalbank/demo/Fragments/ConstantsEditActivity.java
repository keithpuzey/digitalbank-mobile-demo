package xyz.digitalbank.demo.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import xyz.digitalbank.demo.Constants.ConstantsManager;
import xyz.digitalbank.demo.R;
import android.content.Intent;
import xyz.digitalbank.demo.Activity.MainActivity;
import android.app.PendingIntent;
import android.app.AlarmManager;
import xyz.digitalbank.demo.Constants.Constant;

public class ConstantsEditActivity extends AppCompatActivity implements View.OnClickListener {

    private Button cancelBtn,  saveBtn, restartBtn, resetBtn;

    private EditText editTextBaseUrl, editTextMockUrl;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constants_edit);


        // Initialize UI elements
        editTextBaseUrl = findViewById(R.id.editTextBaseUrl);
        editTextMockUrl = findViewById(R.id.editTextMockUrl);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
        restartBtn = findViewById(R.id.restartBtn);
        resetBtn = findViewById(R.id.resetBtn);

        // Set click listeners for buttons
        cancelBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        restartBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
        // Set current constant values in EditText fields
        loadCurrentConstants();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.cancelBtn) {
            // Handle cancel button click
            navigateToLoginFragment();
        } else if (view.getId() == R.id.saveBtn) {
            // Handle save button click
            updateConstants();
         } else if (view.getId() == R.id.restartBtn) {
            // Handle restart button click
            restartApp();
        } else if (view.getId() == R.id.resetBtn) {
            // Handle reset button click
            resetConstants();
        }
    }

    private void loadCurrentConstants() {
        // Load the current values from SharedPreferences and set them to EditText fields
        String currentBaseUrl = ConstantsManager.getBaseUrl(this);
        String currentMockUrl = ConstantsManager.getMockUrl(this);

        editTextBaseUrl.setText(currentBaseUrl);
        editTextMockUrl.setText(currentMockUrl);
    }

    private void resetConstants() {
        // Reset the values to the original ones
        ConstantsManager.setBaseUrl(this, "http://dbankdemo.com/bank/");
        ConstantsManager.setMockUrl(this, "http://dbmobile322871.mock.blazemeter.com/");

        // Reload the EditText fields with original values
        loadCurrentConstants();

        // Show a toast message to indicate reset success
        Toast.makeText(this, "Constants reset successfully", Toast.LENGTH_SHORT).show();

    }
    private void updateConstants() {
        String newBaseUrl = editTextBaseUrl.getText().toString();
        String newMockUrl = editTextMockUrl.getText().toString();

        // Validate the input (optional)
        if (isValidUrl(newBaseUrl) && isValidUrl(newMockUrl)) {
            ConstantsManager.setBaseUrl(this, newBaseUrl);
            ConstantsManager.setMockUrl(this, newMockUrl);

            // Show a toast message to indicate success
            Toast.makeText(this, "Constants updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Show an error message if input is invalid
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToLoginFragment() {

        finish();
    }

    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE); // Add FLAG_IMMUTABLE
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private boolean isValidUrl(String url) {
        // Add your URL validation logic here
        // For example, you can use regular expressions to validate the URL format
        // This is just a placeholder method; replace it with your actual validation logic
        return url != null && !url.isEmpty(); // Example: Validates if the URL is not empty
    }

}
