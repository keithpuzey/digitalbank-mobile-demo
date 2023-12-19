package xyz.digitalbank.demo.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import xyz.digitalbank.demo.Constants.Constant;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;

public class ConstantsEditActivity extends AppCompatActivity implements View.OnClickListener, MyInterface {

    private Button cancelBtn,  saveBtn;
    private EditText editTextBaseUrl, editTextMockUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constants_edit);

        // Initialize UI elements
        editTextBaseUrl = findViewById(R.id.editTextBaseUrl);
        editTextMockUrl = findViewById(R.id.editTextMockUrl);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn); // Initialize the save button


        // Set current constant values in EditText fields
        editTextBaseUrl.setText(Constant.baseUrl.BASE_URL);
        editTextMockUrl.setText(Constant.baseUrl.MOCK_URL);


        // Set click listeners for buttons
        cancelBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this); // Set click listener for the save button


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


    private void updateConstants() {
        // Get the new values from EditText fields
        String newBaseUrl = editTextBaseUrl.getText().toString();
        String newMockUrl = editTextMockUrl.getText().toString();

        // Update the constant values
        Constant.baseUrl.BASE_URL = newBaseUrl;
        Constant.baseUrl.MOCK_URL = newMockUrl;

        // Save new values to SharedPreferences
 //       saveConstantsToSharedPreferences(newBaseUrl, newMockUrl);

        // Finish the activity when save button is clicked
        finish();
    }

 //   private void saveConstantsToSharedPreferences(String baseUrl, String mockUrl) {
        // Use SharedPreferences to store the constants persistently
        // You can get the SharedPreferences instance and save the values here
        // Example:
 //       SharedPreferences.Editor editor = getSharedPreferences("Constants", MODE_PRIVATE).edit();
 //       editor.putString("BASE_URL", baseUrl);
 //       editor.putString("MOCK_URL", mockUrl);
 //       editor.apply();
 //   }

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
