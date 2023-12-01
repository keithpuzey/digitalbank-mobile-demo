package xyz.digitalbank.demo.Fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.Model.User;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.ServiceApi;
import android.widget.ArrayAdapter;

import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import android.app.DatePickerDialog;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.JsonObject;
import xyz.digitalbank.demo.Services.RetrofitClient;
import org.json.JSONException;
import org.json.JSONObject;
import xyz.digitalbank.demo.Model.UserRequest;
import java.util.Locale;
import android.widget.RadioButton;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;
import xyz.digitalbank.demo.Model.UserResponse;
import com.google.gson.Gson;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import org.json.JSONArray;
import java.io.IOException;





public class RegistrationFragment extends Fragment {

    private EditText FnameInput, LnameInput,  emailInput,  passwordInput ;

    private RadioButton maleRadioButton, femaleRadioButton;


    private Spinner titleSpinner;
    private EditText dobInput, ssnInput, addressInput, zipCodeInput , countryInput , localityInput , regionInput , homephoneInput, mobilephoneInput, workphoneInput ;
    private Button regBtn, cancelBtn;

    private static final String ROLE = "USER";

    public RegistrationFragment() {
        // Required empty public constructor
    }


    private void switchToProfileFragment() {
        // Create an instance of the ProfileFragment
        ProfileFragment profileFragment = new ProfileFragment();

        // Get the FragmentManager
        FragmentManager fragmentManager = getParentFragmentManager();

        // Start a FragmentTransaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the current fragment with the ProfileFragment
        fragmentTransaction.replace(R.id.fragment_container, profileFragment);

        // Add the transaction to the back stack (optional, allows for back navigation)
        fragmentTransaction.addToBackStack(null);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        titleSpinner = view.findViewById(R.id.titleSpinner);
        FnameInput = view.findViewById(R.id.FnameInput);
        LnameInput = view.findViewById(R.id.LnameInput);
        maleRadioButton = view.findViewById(R.id.maleRadioButton);
        femaleRadioButton = view.findViewById(R.id.femaleRadioButton);
        dobInput = view.findViewById(R.id.dobInput);
        ssnInput = view.findViewById(R.id.ssnInput);
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        addressInput = view.findViewById(R.id.addressInput);
        localityInput = view.findViewById(R.id.localityInput);
        regionInput = view.findViewById(R.id.regionInput);
        zipCodeInput = view.findViewById(R.id.zipCodeInput);
        countryInput = view.findViewById(R.id.countryInput);
        homephoneInput = view.findViewById(R.id.homephoneInput);
        mobilephoneInput = view.findViewById(R.id.mobilephoneInput);
        workphoneInput = view.findViewById(R.id.workphoneInput);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        CheckBox agreeCheckBox = view.findViewById(R.id.agreeCheckBox);
        Button regBtn = view.findViewById(R.id.regBtn);


        // Populate title dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.title_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        titleSpinner.setAdapter(adapter);

        // Initialize the titleSpinner and dobInput
        titleSpinner = view.findViewById(R.id.titleSpinner);
        dobInput = view.findViewById(R.id.dobInput);

        // Set up the date picker when the dobInput is clicked
        dobInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });




        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement cancel functionality here
                Log.e("cancel button", "clicked");

                // Assuming that MainActivity has a method showLoginFragment() to switch to the login fragment
                ((MainActivity) requireActivity()).showLoginFragment();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d("RegistrationFragment", "Register button clicked");
                    if (agreeCheckBox.isChecked()) {
                        // The user has agreed to the Terms and Conditions, proceed with registration
                        Log.d("RegistrationFragment", "Agree checkbox is checked");
                        registerUser();
                    } else {
                        // Display a message indicating that the user needs to agree to the Terms and Conditions
                        Log.d("RegistrationFragment", "Agree checkbox is not checked");
                        MainActivity.appPreference.showToast("Please agree to the Terms and Conditions.");
                    }
                } catch (Exception e) {
                    Log.e("RegistrationFragment", "Exception in onClick: " + e.getMessage());
                }
            }
        });

        return view;
    }


        private void showDatePicker() {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireActivity(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                            // Handle the selected date
                            // Format the date as DD/MM/YYYY
                            String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                            dobInput.setText(formattedDate);
                        }
                    },
                    year, month, day);

            datePickerDialog.show();
        }

    public void registerUser() {
        String title = titleSpinner.getSelectedItem().toString();
        String Fname = FnameInput.getText().toString();
        String Lname = LnameInput.getText().toString();
        String gender;
        if (maleRadioButton.isChecked()) {
            gender = "M";
        } else if (femaleRadioButton.isChecked()) {
            gender = "F";
        } else {
            // Handle the case where neither radio button is checked (optional)
            gender = ""; // or set a default value
        }
//         String gender = reggender.getText().toString();
        String dob = dobInput.getText().toString();
        String ssn = ssnInput.getText().toString();
        String formattedSSN = formatSSN(ssn);
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String address = addressInput.getText().toString();
        String locality = localityInput.getText().toString();
        String region = regionInput.getText().toString();
        String zipCode = zipCodeInput.getText().toString();
        String country = countryInput.getText().toString();
        String homephone = homephoneInput.getText().toString();
        String mobilephone = mobilephoneInput.getText().toString();
        String workphone = workphoneInput.getText().toString();

        if (TextUtils.isEmpty(Fname)) {
            MainActivity.appPreference.showToast("Your First name is required.");
        } else if (TextUtils.isEmpty(Lname)) {
            MainActivity.appPreference.showToast("Your Last name is required.");
        } else if (TextUtils.isEmpty(email)) {
            MainActivity.appPreference.showToast("Your email is required.");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            MainActivity.appPreference.showToast("Invalid email");
        } else if (TextUtils.isEmpty(password)) {
            MainActivity.appPreference.showToast("Password required");
        } else if (password.length() < 8) {
            MainActivity.appPreference.showToast("Create a password at least 8 characters long.");
        } else if (TextUtils.isEmpty(dob)) {
            MainActivity.appPreference.showToast("Your Date Of Birth is Required");
        } else if (TextUtils.isEmpty(address)) {
            MainActivity.appPreference.showToast("Your Address is Required");
        } else if (TextUtils.isEmpty(locality)) {
            MainActivity.appPreference.showToast("Your Locality is Required");
        } else if (TextUtils.isEmpty(region)) {
            MainActivity.appPreference.showToast("Your Region is Required");
        } else if (TextUtils.isEmpty(zipCode)) {
            MainActivity.appPreference.showToast("Your Post Code is Required");
        } else if (TextUtils.isEmpty(country)) {
            MainActivity.appPreference.showToast("Your Country is Required");
        } else if (TextUtils.isEmpty(homephone)) {
            MainActivity.appPreference.showToast("Your Home Phone is Required");
        } else if (TextUtils.isEmpty(mobilephone)) {
            MainActivity.appPreference.showToast("Your Mobile Phone is Required");
        } else if (TextUtils.isEmpty(workphone) ) {
            MainActivity.appPreference.showToast("Your Work Phone is Required");
        } else {
            registerApiCall(title, Fname, Lname, gender , dob, formattedSSN, email, password, address, locality, region, zipCode, country, homephone, mobilephone, workphone );
        }
    }

    private String formatSSN(String ssn) {
        // Remove any non-digit characters from the original SSN
        String cleanedSSN = ssn.replaceAll("\\D", "");

        // Ensure the SSN is 9 digits long
        if (cleanedSSN.length() == 9) {
            // Format the SSN as "xxx-xx-xxxx"
            return String.format("%s-%s-%s",
                    cleanedSSN.substring(0, 3),
                    cleanedSSN.substring(3, 5),
                    cleanedSSN.substring(5));
        } else {
            // Return the original SSN if it doesn't have 9 digits
            return ssn;
        }
    }


    private void registerApiCall(String title, String Fname, String Lname, String gender, String dob, String ssn, String email, String password, String address, String locality, String region, String zipCode, String country, String homephone, String mobilephone, String workphone) {
        // Get the username and password for the initial authentication API call
        String username = "admin@demo.io";
        String adminpassword = "Demo123!";

        // Make the initial authentication API call to get the authToken
        RetrofitClient.getServiceApi().authenticateUser(username, adminpassword)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {

                          Log.d("API", "Request successful: " + call.request().url());
                            Log.d("API", "Response: " + response.body());

 //                           // Authentication successful, get the authToken
 //                           String authToken = "Bearer " + response.body().get("authToken").getAsString();
                            // String authToken = response.body().get("authToken").getAsString();

                            String authToken = "Bearer " +  response.body().get("authToken").getAsString();

                            // Save the authToken to your app preferences or wherever you need it
                            MainActivity.appPreference.setauthToken(authToken);

                            Log.d("API", "Token: " + authToken);

                            // Make the Registration  API call with the authToken

                            registerUserDetailsApiCall(authToken, title, Fname, Lname, gender, dob, ssn, email, password, address, locality, region, zipCode, country, homephone, mobilephone, workphone );

                        } else {
                            Log.e("API", "Authentication Request failed: " + call.request().url());
                            Log.e("API", "Error: " + response.message());
                            // Authentication failed, handle the error
                            MainActivity.appPreference.showToast("Authentication failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                        Log.e("API", "Request failed: " + call.request().url());
                        Log.e("API", "Error: " + t.getMessage());
                        // Handle the failure of the authentication API call
                        MainActivity.appPreference.showToast("Authentication failed");
                    }
                });
    }

    private void registerUserDetailsApiCall(String authToken, String title, String Fname, String Lname, String gender, String dob, String ssn, String email,
                                            String password, String address, String locality, String region, String zipCode, String country, String homephone, String mobilephone, String workphone ) {


        UserRequest userRequest = new UserRequest();
        userRequest.setTitle(title);
        userRequest.setFirstName(Fname);
        userRequest.setLastName(Lname);
        userRequest.setGender(gender);
        userRequest.setDob(dob);
        userRequest.setSsn(ssn);
        userRequest.setEmailAddress(email);
        userRequest.setPassword(password);
        userRequest.setAddress(address);
        userRequest.setLocality(locality);
        userRequest.setRegion(region);
        userRequest.setzipcode(zipCode);
        userRequest.setCountry(country);
        userRequest.setHomePhone(homephone);
        userRequest.setMobilePhone(mobilephone);
        userRequest.setWorkPhone(workphone);


         Log.e("API", "User Registration Token : " + authToken);
         Log.e("API", "User Registration JSON Body : " + userRequest.toJsonString() );

        // Make the second API call to register the user details
        RetrofitClient.getServiceApi().registerUser(authToken,  "application/json", ROLE, userRequest)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {

                            //  API call to find user by email and get user ID
                            RetrofitClient.getServiceApi().findUserId(authToken, email)
                                    .enqueue(new Callback<UserResponse>() {
                                        @Override
                                        public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                                            if (response.isSuccessful()) {


                                                // Log success information
                                                Log.d("API", "Request successful: " + call.request().url());
                                                Log.d("API", "Response Code: " + response.code());


                                                // Log response body as JSON string
                                                UserResponse userResponse = response.body();
                                                if (userResponse != null) {
                                                    int userId = userResponse.getId();
                                                    String responseBodyJson = new Gson().toJson(userResponse);
                                                    Log.d("API", "Response Body: " + responseBodyJson);
                                                    Log.d("API", "ID Value = : " + userId );

                                                    createUserData(authToken, userId);

                                                } else {
                                                    Log.d("API", "Response Body is null");
                                                }


                                            } else {
                                                // Log error information
                                                Log.e("API", "Request failed: " + call.request().url());
                                                Log.e("API", "Error Code: " + response.code());
                                                Log.e("API", "Error Body: " + response.errorBody().toString());
                                                MainActivity.appPreference.showToast("User not found");

                                                // Handle the error
                                            }
                                        }


                                        @Override
                                        public void onFailure(Call<UserResponse> call, Throwable t) {
                                            // Handle failure
                                            MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
                                        }
                                    });

                            MainActivity.appPreference.showToast("Registration successful");

                            MainActivity.appPreference.setLoginStatus(true);

                            ((MainActivity) requireActivity()).showLoginFragment();

                        } else {
                            // Registration failed
                            handleRegistrationError(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("API", "Failure: " + t.getMessage());
                        // Handle the failure of the registration API call

                        MainActivity.appPreference.showToast("Registration failed");
                    }
                });
    }

    private void showErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }
    private void handleRegistrationError(Response<?> response) {
        try {
            // Extract the error message from the response body
            String errorBodyString = response.errorBody().string();

            try {
                // Try parsing the error body as a JSON array
                JSONArray jsonArray = new JSONArray(errorBodyString);

                // Extract the first element as the error message
                String errorMessage = jsonArray.optString(0);

                // Display the error message in an AlertDialog
                showErrorDialog(errorMessage);
                return;
            } catch (JSONException e) {
                // Ignore and proceed to the next step
            }

            try {
                // Try parsing the error body as a JSON object
                JSONObject jsonObject = new JSONObject(errorBodyString);

                // Check if the JSON object contains a "message" key
                if (jsonObject.has("message")) {
                    // Extract the "message" value as the error message
                    String errorMessage = jsonObject.getString("message");

                    // Display the error message in an AlertDialog
                    showErrorDialog(errorMessage);
                } else {
                    // Display a generic error message if "message" key is not present
                    MainActivity.appPreference.showToast("Registration failed");
                }
            } catch (JSONException e) {
                // Display a generic error message if parsing as JSON object fails
                MainActivity.appPreference.showToast("Registration failed");
            }
        } catch (IOException e) {
            Log.e("API", "Error handling registration error: " + e.getMessage());
            // Display a generic error message if there's an issue reading the error body
            MainActivity.appPreference.showToast("Registration failed");
        }
    }


    private void createUserData(String authToken, int userId) {
        // API call to create user data using the obtained ID
        RetrofitClient.getServiceApi().createData(userId, authToken )
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {

                            Log.d("API", "Data Creation Code: " + response.code());
                            Log.d("API", "Data Creation Code: " + response.code());
                            Log.d("API", "Data Creation Body " + response.body());

                            MainActivity.appPreference.showToast("Data creation successful");
                            // Handle success
                        } else {
                            // Handle the case where data creation failed
                            Log.d("API", "ID Value = : " + userId );
                            Log.d("API", "Token Value = : " + authToken );
                            Log.e("API", "Request failed: " + call.request().url());

                            Log.d("API", "Data Creation Failed Code: " + response.code());
                            Log.d("API", "Data Creation Failed Body " + response.body());

                            MainActivity.appPreference.showToast("Data creation failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Handle failure
                        MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
                    }
                });
    }
}
