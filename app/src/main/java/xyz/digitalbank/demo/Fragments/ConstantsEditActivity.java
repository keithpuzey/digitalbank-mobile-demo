package xyz.digitalbank.demo.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import xyz.digitalbank.demo.Constants.Constant;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;

public class ConstantsEditActivity extends AppCompatActivity implements View.OnClickListener, MyInterface {

    private Button cancelBtn;
    private EditText editTextBaseUrl, editTextMockUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constants_edit);

        // Initialize UI elements
        editTextBaseUrl = findViewById(R.id.editTextBaseUrl);
        editTextMockUrl = findViewById(R.id.editTextMockUrl);
        cancelBtn = findViewById(R.id.cancelBtn);


        // Set current constant values in EditText fields
        editTextBaseUrl.setText(Constant.baseUrl.BASE_URL);
        editTextMockUrl.setText(Constant.baseUrl.MOCK_URL);

        // Set click listeners for buttons
        cancelBtn.setOnClickListener(this);


        // Implement the layout and functionality for editing constants here
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.cancelBtn) {
            // Handle cancel button click
            navigateToLoginFragment();
        }
    }

    private void updateConstants() {
        // Get the new values from EditText fields
        String newBaseUrl = editTextBaseUrl.getText().toString();
        String newMockUrl = editTextMockUrl.getText().toString();

        // Update the constant values
        Constant.baseUrl.BASE_URL = newBaseUrl;
        Constant.baseUrl.MOCK_URL = newMockUrl;

        // Optionally, you may want to save these new values to preferences or elsewhere

        // Now, you can navigate back to the login fragment
        navigateToLoginFragment();
    }

    private void navigateToLoginFragment() {
        // Create a new instance of the LoginFragment
        LoginFragment loginFragment = new LoginFragment();

        // Get the FragmentManager and start a transaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace the current fragment with the LoginFragment
        transaction.replace(R.id.fragment_container, loginFragment);

        // Add the transaction to the back stack so the user can navigate back
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
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
