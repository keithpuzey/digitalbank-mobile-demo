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
import android.widget.TableLayout;
import android.widget.TableRow;
import xyz.digitalbank.demo.Model.TransactionResponse;
import xyz.digitalbank.demo.Model.AccountInfo;

import com.google.gson.Gson;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.graphics.Color;
import xyz.digitalbank.demo.Extras.AppPreference;


public class ProfileFragment extends Fragment {
    public TextView name, email, title;
    private MyInterface logoutListener;
    public int loggedinuserId;

    private List<AccountInfo> accountInfoList = new ArrayList<>();

    private AppPreference appPreference;

    private List<UserAccountResponse> userAccounts;

    public String Email ;
    private String authToken;
    public int accountId ;
    private Spinner accountSpinner;
    public  AccountInfo selectedAccountInfo;
    private Context context; // Declare the context variable

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
        Log.d("ProfileFragment", "onCreateView");
        appPreference = new AppPreference(requireContext());

        authToken = MainActivity.appPreference.getauthToken();


        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (name != null) {
                // Get Account Details
                updateProfileDetails();
                getAndDisplayAccountTransactions(authToken, accountId);
                String displayName = MainActivity.appPreference.getDisplayName();
                String greetingMessage = "Loading ";
                name.setText(greetingMessage);
            } else {
                Log.e("ProfileFragment", "TextView 'name' is null");
            }
            if (itemId == R.id.action_check_accounts) {
                // Switch to the ProfileFragment
                switchToProfileFragment();
                return true;
            } else if (itemId == R.id.action_dashboard) {
                // Handle the TransferFragment click
                // Add your logic here
                return true;
            } else if (itemId == R.id.action_transfer) {
                // Handle the TransferFragment click
                // Add your logic here
                return true;
            } else if (itemId == R.id.action_atm_search) {
                // Start the atm_search activity
                startActivity(new Intent(getActivity(), atm_search.class));
                return true;
            } else if (itemId == R.id.action_logout) {
                // Handle the logout click
                if (logoutListener != null) {
                    logoutListener.logout();
                }
                return true;
            } else {
                return false;
            }
        });

        // Check if the view is not null
        if (view != null) {
            accountSpinner = view.findViewById(R.id.accountSpinner);
            name = view.findViewById(R.id.name);
            TableLayout tableLayout = view.findViewById(R.id.tableLayout);
            LinearLayout tableHeader = view.findViewById(R.id.tableHeader);

            // Check if the tableHeader is not null before calling clearAndDisplayAccountTransactions
            if (tableHeader != null) {
                // Clear existing rows in the TableLayout
                tableLayout.removeAllViews();

                ViewGroup parent = (ViewGroup) tableHeader.getParent();
                if (parent != null) {
                    parent.removeView(tableHeader);
                }

                // Add Table Header
                tableLayout.addView(tableHeader);

                // ... Other code ...

                // Call the method to get and display account transactions for the selected account
                authToken = MainActivity.appPreference.getauthToken();
                getAndDisplayAccountTransactions(authToken, accountId);
            } else {
                Log.e("ProfileFragment", "tableHeader is null");
            }
        } else {
            Log.e("ProfileFragment", "View is null");
        }
        // Set a listener to handle item selection if needed
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle item selection
                UserAccountResponse selectedAccount = userAccounts.get(position);
                int selectedAccountId = selectedAccount.getId();
                Log.d("API", "Selected Account ID from Drop Down = : " + selectedAccountId );

                // Call the method to get and display account transactions for the selected account
                getAndDisplayAccountTransactions(authToken , selectedAccountId);
                // Update selected account details
                updateSelectedAccountDetails(accountInfoList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle nothing selected if needed
            }
        });


        // Get Account Details
        updateProfileDetails(  );
     //   int accountId = 398885 ;


        getAndDisplayAccountTransactions( authToken , accountId);

        String displayName = MainActivity.appPreference.getDisplayName();
        String greetingMessage = "Loading ";
        name.setText(greetingMessage);

        email = view.findViewById(R.id.email);

     //   email.setText(authTokenMessage);

        return view;
    }

    public void updateProfileDetails() {
        if (!isAdded()) {
            // Fragment is not attached, handle accordingly
            return;
        }
        // Get the username and password for the initial authentication API call
        String username = "admin@demo.io";
        String adminpassword = "Demo123!";

        // Make the initial authentication API call to get the authToken
        RetrofitClient.getServiceApi(requireContext()).authenticateUser(username, adminpassword)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            // Authentication successful, get the authToken
                            String authToken = "Bearer " +  response.body().get("authToken").getAsString();
                            Log.d("API", "Auth Token is = : " + authToken );
                            // Save the authToken to your app preferences or wherever you need it
                            MainActivity.appPreference.setauthToken(authToken);



                            // Call findUserId API to get the user ID
                            String email = ((MainActivity) requireActivity()).getEmail();


                            RetrofitClient.getServiceApi(requireContext()).findUserId(authToken, email)

                                    .enqueue(new Callback<UserResponse>() {


                                        @Override
                                        public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {

                                            Log.d("API", "Email address is = : " + email );
                                            if (response.isSuccessful()) {
                                                UserResponse userResponse = response.body();

                                                // Directly assign the id to the int variable
                                                loggedinuserId = userResponse.getId();
                                                // Save the authToken to your app preferences or wherever you need it

                                                Log.d("API", "Logged in user ID = : " + loggedinuserId );

                                                ((MainActivity) requireActivity()).setLoggedinuserId(loggedinuserId);
                                                appPreference.setLoggedinuserId(loggedinuserId);

                                                // Call the API to get user profile details using the obtained user ID
                                                getUserProfile(authToken, loggedinuserId);
                                            } else {
                                                // Handle the case where findUserId API failed
                                                MainActivity.appPreference.showToast("Failed to retrieve user ID");
                                                Log.d("API", "Email address not found  = : " + email );
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
      //                      MainActivity.appPreference.showToast("Authentication failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        // Handle failure of authentication API
      //                  MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
                    }
                });
    }

    private void getUserProfile(String authToken, int loggedinuserId) {
        // Call the API to get user profile details using the obtained user ID

        RetrofitClient.getServiceApi(requireContext()).getUserProfile(loggedinuserId , authToken )
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
         //                   MainActivity.appPreference.showToast("Failed to retrieve user profile");
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        // Handle failure of getUserProfile API
      //                  MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
                    }
                });
    }


    private void getUserAccounts(String authToken, int loggedinuserId) {
        // Call the API to get user accounts using the obtained user ID


        RetrofitClient.getServiceApi(requireContext()).getUserAccounts(loggedinuserId, authToken)
                .enqueue(new Callback<List<UserAccountResponse>>() {
                    @Override
                    public void onResponse(Call<List<UserAccountResponse>> call, Response<List<UserAccountResponse>> response) {
                        if (response.isSuccessful()) {
                            // Process the list of UserAccountResponse
                            List<UserAccountResponse> userAccounts = response.body();
                            displayUserAccounts(userAccounts);

                        } else {
                            // Handle the case where the API call was not successful
                            // MainActivity.appPreference.showToast("Failed to retrieve user accounts");
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

        accountInfoList.clear(); // Clear existing data

        // Assuming you have a reference to the Spinner in your fragment
        Spinner accountSpinner = getView().findViewById(R.id.accountSpinner);

        // Create a list of AccountInfo to hold account names, numbers, IDs, and current balances
        List<AccountInfo> accountInfoList = new ArrayList<>();

        for (UserAccountResponse account : userAccounts) {
            // Extract information from UserAccountResponse
            int accountId = account.getId();
            String accountName = account.getName();
            String currentBalanceStr = String.valueOf(account.getCurrentBalance());

            // Convert currentBalance to double
            double currentBalance = Double.parseDouble(currentBalanceStr);

            String accountTypeName = "";
            if (account.getAccountType() != null) {
                accountTypeName = account.getAccountType().getName();
            }

            // Create an AccountInfo object and add it to the list
            AccountInfo accountInfo = new AccountInfo(accountId, accountName, currentBalance, accountTypeName);
            accountInfoList.add(accountInfo);

            // Display other account details as needed
            Log.d("UserAccount", "Account Name: " + accountName);
            Log.d("UserAccount", "Account ID: " + accountId);
            Log.d("UserAccount", "Current Balance: " + currentBalance);
            Log.d("User Account", "Account Number: " + account.getAccountNumber());
            Log.d("User Account", "Current Balance: " + account.getCurrentBalance());
            Log.d("User Account", "Opening Balance: " + account.getOpeningBalance());
            Log.d("User Account", "Interest Rate: " + account.getInterestRate());
            if (account.getAccountType() != null) {
                Log.d("User Account", "Account Type Name: " + account.getAccountType().getName());
            }



        }

        // Create an ArrayAdapter with AccountInfo objects
        ArrayAdapter<AccountInfo> adapter = new ArrayAdapter<AccountInfo>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                accountInfoList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        accountSpinner.setAdapter(adapter);

        // Set a listener to handle item selection
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle item selection
                AccountInfo selectedAccount = accountInfoList.get(position);
                int selectedAccountId = selectedAccount.getId();

                Log.d("API", "Selected Account ID from Drop Down = : " + selectedAccountId);
                // Call the method to get and display account transactions for the selected account
                authToken = MainActivity.appPreference.getauthToken();
                updateSelectedAccountDetails(accountInfoList.get(position));

                getAndDisplayAccountTransactions( authToken , selectedAccountId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle nothing selected if needed
            }
        });
    }

    private void updateSelectedAccountDetails(AccountInfo accountInfo) {
        // Check if 'accountName' TextView is null before setting text
        TextView accountNameTextView = getView().findViewById(R.id.accountName);
        if (accountNameTextView != null) {
            accountNameTextView.setText(accountInfo.getAccountTypeName());
        } else {
            Log.e("ProfileFragment", "TextView 'accountName' is null");
        }

        // Check if 'balance' TextView is null before setting text
        TextView balanceTextView = getView().findViewById(R.id.balance);
        if (balanceTextView != null) {
            balanceTextView.setText("Balance: " + accountInfo.getCurrentBalance());
        } else {
            Log.e("ProfileFragment", "TextView 'balance' is null");
        }
    }

    private void displayUserProfile(UserProfileResponse userProfileResponse) {
        // Update UI to display user profile details
        // For example, you can set text in TextViews or update UI components
        // with the information obtained from userProfileResponse
        // userProfileResponse.getFirstName(), userProfileResponse.getLastName(), etc.
        // Check if 'name' TextView is null before setting text
        if (name != null) {
            String fullName = userProfileResponse.getTitle() + " " + userProfileResponse.getFirstName() + " " + userProfileResponse.getLastName();
            name.setText(fullName);
        } else {
            Log.e("ProfileFragment", "TextView 'name' is null in displayUserProfile");
        }

        // Check if 'email' TextView is null before setting text
        if (email != null) {
            email.setText("Email: " + userProfileResponse.getEmailAddress());
        } else {
            Log.e("ProfileFragment", "TextView 'email' is null in displayUserProfile");
        }
    }
    private void getAndDisplayAccountTransactions(String authToken, int accountId) {
        RetrofitClient.getServiceApi(requireContext()).getAccountTransactions(accountId, authToken)
                .enqueue(new Callback<List<TransactionResponse>>() {
                    @Override
                    public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                        if (response.isSuccessful()) {
                            List<TransactionResponse> accountTransactions = response.body();
                            displayAccountTransactions(accountTransactions);
                        } else {
                            Log.e("API", "Failed to retrieve account transactions. Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                        Log.e("API", "API call failed: " + t.getMessage());
                    }
                });
    }

    private void displayAccountTransactions(List<TransactionResponse> accountTransactions) {
        // Assuming you have a reference to the TableLayout in your fragment
        TableLayout tableLayout = getView().findViewById(R.id.tableLayout);

        // Clear existing rows in the TableLayout
        tableLayout.removeAllViews();

        // Loop through the transactions and add new rows to the TableLayout
        int count = Math.min(accountTransactions.size(), 40);
        for (int i = accountTransactions.size() - count; i < accountTransactions.size(); i++) {
            TransactionResponse transaction = accountTransactions.get(i);

            // Create a new TableRow
            TableRow row = new TableRow(requireContext());

            // Create TextViews to display the transaction details
            TextView descriptionTextView = new TextView(requireContext());
            descriptionTextView.setText(transaction.getDescription());
            setTextViewAttributes(descriptionTextView);

            TextView amountTextView = new TextView(requireContext());
            String amountValue = String.valueOf(transaction.getAmount());
            amountTextView.setText(amountValue);
            // Set text color to red if the amount is negative
            if (transaction.getAmount() < 0) {
                amountTextView.setTextColor(Color.RED);
            }
            setTextViewAttributes(amountTextView);

            TextView runningBalanceTextView = new TextView(requireContext());
            runningBalanceTextView.setText(String.valueOf(transaction.getRunningBalance()));
            // Set text color to red if the running balance is negative
            if (transaction.getRunningBalance() < 0) {
                runningBalanceTextView.setTextColor(Color.RED);
            }
            setTextViewAttributes(runningBalanceTextView);

            // Add TextViews to the TableRow
            row.addView(descriptionTextView);
            row.addView(amountTextView);
            row.addView(runningBalanceTextView);

            // Add TableRow to the TableLayout
            tableLayout.addView(row);
        }
    }

    private void setTextViewAttributes(TextView textView) {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER);
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ProfileFragment", "onCreate");
    }
    public void onResume() {
        super.onResume();
        Log.d("ProfileFragment", "onResume");
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
