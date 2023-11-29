package xyz.digitalbank.demo.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.Activity.atm_search;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;
import xyz.digitalbank.demo.Model.UserProfileResponse;
import xyz.digitalbank.demo.Model.UserResponse;
import xyz.digitalbank.demo.Model.UserAccountResponse;
import com.google.gson.JsonObject;
import xyz.digitalbank.demo.Services.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import android.widget.Spinner;
import org.json.JSONArray;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;


public class ProfileFragment extends Fragment {
    public TextView name, email, title;
    private MyInterface logoutListener;
    public int loggedinuserId;

    public String Email ;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public void setEmail(String email) {
        this.Email = email;
        // Call the method to update the profile details
        updateProfileDetails();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // Handle clicks on the icons here
            switch (item.getItemId()) {
                case R.id.action_check_accounts:
                    // Switch to the ProfileFragment
                    switchToProfileFragment();
                    return true;
                case R.id.action_transfer:
                    // Handle transfer icon click
                    return true;
                case R.id.action_atm_search:
                    startActivity(new Intent(getActivity(), atm_search.class));
                    return true;
                case R.id.action_logout:
                    if (logoutListener != null) {
                        logoutListener.logout();
                    }
                    return true;
                default:
                    return false;
            }
        });


        name = view.findViewById(R.id.name);
        // Keep only the greeting message
        updateProfileDetails(  );

        String displayName = MainActivity.appPreference.getDisplayName();
        String greetingMessage = "Logged into Digital Bank as user " + displayName;
        name.setText(greetingMessage);

        email = view.findViewById(R.id.email);
        // Display only the authentication token
        String authToken = MainActivity.appPreference.getauthToken();
        String authTokenMessage = "Authentication Token: " + loggedinuserId ;
        email.setText(authTokenMessage);

        return view;
    }

    public void updateProfileDetails() {
        // Get the username and password for the initial authentication API call
        String username = "admin@demo.io";
        String adminpassword = "Demo123!";

        // Make the initial authentication API call to get the authToken
        RetrofitClient.getServiceApi().authenticateUser(username, adminpassword)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            // Authentication successful, get the authToken
                            String authToken = "Bearer " + response.body().get("authToken").getAsString();

                            // Save the authToken to your app preferences or wherever you need it
                            MainActivity.appPreference.setauthToken(authToken);

                            // Call findUserId API to get the user ID
                            String email = ((MainActivity) requireActivity()).getEmail();


                            RetrofitClient.getServiceApi().findUserId(authToken, email )
                                    .enqueue(new Callback<UserResponse>() {
                                        @Override
                                        public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                                            if (response.isSuccessful()) {
                                                UserResponse userResponse = response.body();

                                                // Directly assign the id to the int variable
                                                loggedinuserId = userResponse.getId();
                                                Log.d("API", "Logged in user ID = : " + loggedinuserId );

                                                ((MainActivity) requireActivity()).setLoggedinuserId(loggedinuserId);
                                                // Call the API to get user profile details using the obtained user ID
                                                getUserProfile(authToken, loggedinuserId);
                                            } else {
                                                // Handle the case where findUserId API failed
                                                MainActivity.appPreference.showToast("Failed to retrieve user ID");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<UserResponse> call, Throwable t) {
                                            // Handle failure of findUserId API
                                            MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
                                        }
                                    });
                        } else {
                            // Handle the case where authentication API failed
                            MainActivity.appPreference.showToast("Authentication failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        // Handle failure of authentication API
                        MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
                    }
                });
    }

    private void getUserProfile(String authToken, int loggedinuserId) {
        // Call the API to get user profile details using the obtained user ID

        RetrofitClient.getServiceApi().getUserProfile(loggedinuserId , authToken )
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                        if (response.isSuccessful()) {
                            UserProfileResponse userProfileResponse = response.body();
                            // Update UI to display user profile details
                            displayUserProfile(userProfileResponse);
                            // Set the loggedinuserId in MainActivity
                            ((MainActivity) requireActivity()).setLoggedinuserId(loggedinuserId);
                            // Call the API to get user accounts using the obtained user ID

                            getUserAccounts(authToken, loggedinuserId);

                        } else {
                            // Handle the case where getUserProfile API failed
                            MainActivity.appPreference.showToast("Failed to retrieve user profile");
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        // Handle failure of getUserProfile API
                        MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
                    }
                });
    }


    private void getUserAccounts(String authToken, int loggedinuserId) {
        // Call the API to get user accounts using the obtained user ID

        RetrofitClient.getServiceApi().getUserAccounts(loggedinuserId, authToken)
                .enqueue(new Callback<List<UserAccountResponse>>() {
                    @Override
                    public void onResponse(Call<List<UserAccountResponse>> call, Response<List<UserAccountResponse>> response) {
                        if (response.isSuccessful()) {
                            // Process the list of UserAccountResponse
                            List<UserAccountResponse> userAccounts = response.body();
                            displayUserAccounts(userAccounts);
                        } else {
                            // Handle the case where the API call was not successful
                            MainActivity.appPreference.showToast("Failed to retrieve user accounts");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<UserAccountResponse>> call, Throwable t) {
                        // Handle failure of getUserAccounts API
                        MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
                    }
                });
    }
    private void displayUserAccounts(List<UserAccountResponse> userAccounts) {
        // Assuming you have a reference to the Spinner in your fragment
        Spinner accountSpinner = getView().findViewById(R.id.accountSpinner);

        // Create a list of strings to hold account names
        List<String> accountNames = new ArrayList<>();
        for (UserAccountResponse account : userAccounts) {
            accountNames.add(account.getName());
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, accountNames);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        accountSpinner.setAdapter(adapter);

        // Set a listener to handle item selection if needed
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle item selection if needed
                String selectedAccountName = accountNames.get(position);
                // Do something with the selected account name
                // For example, you can display additional details or perform actions based on the selected account
                // You can access the corresponding UserAccountResponse using the position
                UserAccountResponse selectedAccount = userAccounts.get(position);
                // Log or display details of the selected account
                Log.d("UserAccount", "Selected Account Name: " + selectedAccount.getName());
                Log.d("UserAccount", "Selected Account Number: " + selectedAccount.getAccountNumber());
                // Add more details as needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle nothing selected if needed
            }
        });
    }

    private void displayUserProfile(UserProfileResponse userProfileResponse) {
        // Update UI to display user profile details
        // For example, you can set text in TextViews or update UI components
        // with the information obtained from userProfileResponse
        // userProfileResponse.getFirstName(), userProfileResponse.getLastName(), etc.
        String fullName = userProfileResponse.getTitle() + " " + userProfileResponse.getFirstName() + " " + userProfileResponse.getLastName();
        name.setText(fullName);
        email.setText("Email: " + userProfileResponse.getEmailAddress());

    }


    private void switchToProfileFragment() {
        // Log to check if this method is being called
        Log.d("ProfileFragment", "Switching to Check Accounts");

        // Check if the current fragment is not already MainActivity
        if (!(getActivity() instanceof MainActivity)) {
            // Switch to MainActivity
            MainActivity.appPreference.showToast("Switching to Check Accounts");
            MainActivity.appPreference.setLoginStatus(true);

            // Create a new Intent
            Intent intent = new Intent(getActivity(), MainActivity.class);

            // Clear the back stack to prevent returning to the login screen
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Start the MainActivity
            startActivity(intent);

            // Optional: finish the current activity if needed
            requireActivity().finish();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        logoutListener = (MyInterface) activity;

        // Log to check if onAttach is being called
        Log.d("ProfileFragment", "onAttach");
    }
}
