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
import com.google.gson.JsonObject;
import xyz.digitalbank.demo.Services.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
