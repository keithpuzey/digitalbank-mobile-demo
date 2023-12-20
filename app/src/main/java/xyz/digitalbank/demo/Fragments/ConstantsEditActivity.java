package xyz.digitalbank.demo.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import xyz.digitalbank.demo.Constants.Constant;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;
import xyz.digitalbank.demo.Constants.ConstantsManager;
import android.util.Log;

public class ConstantsEditActivity extends AppCompatActivity implements View.OnClickListener, MyInterface {

    private Button cancelBtn,  saveBtn;
    private EditText editTextBaseUrl, editTextMockUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constants_edit);

        // Log statement to check if onCreate is called
        Log.d("ConstantsEditActivity", "onCreate called");


        // Initialize UI elements
        editTextBaseUrl = findViewById(R.id.editTextBaseUrl);
        editTextMockUrl = findViewById(R.id.editTextMockUrl);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn); // Initialize the save button


        // Set current constant values in EditText fields
      //  editTextBaseUrl.setText(Constant.baseUrl.BASE_URL);
      //  editTextMockUrl.setText(Constant.baseUrl.MOCK_URL);


        // Set click listeners for buttons
        cancelBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this); // Set click listener for the save button

        // Set current constant values in EditText fields
        loadCurrentConstants(); // Load the values when the activity is created


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.cancelBtn) {
            // Handle cancel button click
            navigateToLoginFragment();
        } else if (view.getId() == R.id.saveBtn) {
            // Handle save button click
            updateConstants();
        }
    }


    private void loadCurrentConstants() {
        // Load the current values from SharedPreferences and set them to EditText fields
        String currentBaseUrl = ConstantsManager.getBaseUrl(this);
        String currentMockUrl = ConstantsManager.getMockUrl(this);

        // Log statements to check values
        Log.d("ConstantsEditActivity", "Loaded BaseUrl: " + currentBaseUrl);
        Log.d("ConstantsEditActivity", "Loaded MockUrl: " + currentMockUrl);

        editTextBaseUrl.setText(currentBaseUrl);
        editTextMockUrl.setText(currentMockUrl);
    }

    private void updateConstants() {
        // Get the new values from EditText fields
        String newBaseUrl = editTextBaseUrl.getText().toString();
        String newMockUrl = editTextMockUrl.getText().toString();

        // Update the constant values using ConstantsManager
        ConstantsManager.setBaseUrl(this, newBaseUrl);
        ConstantsManager.setMockUrl(this, newMockUrl);

        // Log statements to check values
        Log.d("ConstantsEditActivity", "Updated BaseUrl: " + newBaseUrl);
        Log.d("ConstantsEditActivity", "Updated MockUrl: " + newMockUrl);

        // Finish the activity when save button is clicked
        finish();
    }

    protected void onResume() {
        super.onResume();
        // Log statement to check if onResume is called
        Log.d("ConstantsEditActivity", "onResume called");
    }


    private void navigateToLoginFragment() {

        finish();
    }



    @Override
    public void login(String authToken, String Email) {
        // Dummy implementation or leave it empty
    }

    @Override
    public void register() {
        // Dummy implementation or leave it empty
    }

    public void logout() {
        // Add any necessary implementation or leave it empty if not needed
    }
}
