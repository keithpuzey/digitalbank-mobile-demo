package xyz.digitalbank.demo.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.Activity.atm_search;
import xyz.digitalbank.demo.Extras.AppPreference;
import xyz.digitalbank.demo.Model.AccountInfo;
import xyz.digitalbank.demo.Model.TransactionResponse;
import xyz.digitalbank.demo.Model.UserAccountResponse;
import xyz.digitalbank.demo.Model.UserProfileResponse;
import xyz.digitalbank.demo.Model.UserResponse;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;
import xyz.digitalbank.demo.Services.RetrofitClient;



public class ProfileFragment extends Fragment {
    public TextView name, email, title;
    private MyInterface logoutListener;
    public int loggedinuserId;

    private List<AccountInfo> accountInfoList = new ArrayList<>();

    private ProgressBar progressBar;
    private AppPreference appPreference;

    private List<UserAccountResponse> userAccounts;



    // Declare PopupWindow and its components
    private PopupWindow popupWindow;
    private TextView userNameTextView;
    private TextView logoutLinkTextView;

    private TextView progressTextView;

    public String Email ;
    private String authToken;
    public int accountId ;
    private Spinner accountSpinner;

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
        // Log.d("ProfileFragment", "onCreateView");

        // Perform null check after inflating the view
        if (view != null) {
        appPreference = new AppPreference(requireContext());
        authToken = MainActivity.appPreference.getauthToken();
        progressTextView = view.findViewById(R.id.progressTextView);
        Toolbar toolbar = view.findViewById(R.id.action_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeAsUpIndicator(null);

            ImageView toolbarImage = view.findViewById(R.id.toolbar_image);
            toolbarImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(toolbarImage);
                }
            });

            View rootLayout = view.findViewById(R.id.profile_root_layout);
            rootLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                        return true; // Consume the touch event to prevent it from propagating further
                    }
                    return false; // Allow the touch event to propagate if the popup menu is not showing
                }
            });


        if (progressTextView != null) {
               progressTextView.setVisibility(View.VISIBLE);
        }
        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (name != null) {
                // Get Account Details
                updateProfileDetails();

                String displayName = MainActivity.appPreference.getDisplayName();
                String greetingMessage = "Loading ";
                getAndDisplayAccountTransactions(authToken, accountId);
                // Assuming you have retrieved the user's name and stored it in a variable named userName
                // Concatenate "Welcome" with the userName
                //  String welcomeMessage = "Hi, " + name;

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
            }  else {
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
               //  Log.d("API", "Selected Account ID from Drop Down = : " + selectedAccountId );

                // Show progress text
                if (progressTextView != null) {
                    progressTextView.setVisibility(View.VISIBLE);
                }


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
      //  name.setText(greetingMessage);

        email = view.findViewById(R.id.email);

        } else {
            Log.e("ProfileFragment", "View is null");
        }

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
                          //   Log.d("API", "Auth Token is = : " + authToken );
                            // Save the authToken to your app preferences or wherever you need it
                            MainActivity.appPreference.setauthToken(authToken);



                            // Call findUserId API to get the user ID
                            String email = ((MainActivity) requireActivity()).getEmail();


                            RetrofitClient.getServiceApi(requireContext()).findUserId(authToken, email)

                                    .enqueue(new Callback<UserResponse>() {


                                        @Override
                                        public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {

                                     //      Log.d("API", "Email address is = : " + email );
                                            if (response.isSuccessful()) {
                                                UserResponse userResponse = response.body();

                                                // Directly assign the id to the int variable
                                                loggedinuserId = userResponse.getId();
                                                // Save the authToken to your app preferences or wherever you need it

                                            //    Log.d("API", "Logged in user ID = : " + loggedinuserId );

                                                ((MainActivity) requireActivity()).setLoggedinuserId(loggedinuserId);
                                                appPreference.setLoggedinuserId(loggedinuserId);

                                                // Call the API to get user profile details using the obtained user ID
                                                getUserProfile(authToken, loggedinuserId);
                                            } else {
                                                // Handle the case where findUserId API failed
                                                MainActivity.appPreference.showToast("Failed to retrieve user ID");
                                             //   Log.d("API", "Email address not found  = : " + email );
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

    private void showPopupMenu(View anchorView) {
        if (popupWindow == null) {
            View popupView = getLayoutInflater().inflate(R.layout.popup_user_info, null);

            logoutLinkTextView = popupView.findViewById(R.id.link_logout);
            logoutLinkTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                    mainActivity.logout();
                }
            });

            popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        }

        popupWindow.showAsDropDown(anchorView);
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
            int accountNumber = account.getAccountNumber();


            // Create an AccountInfo object and add it to the list
            AccountInfo accountInfo = new AccountInfo(accountId, accountName, currentBalance, accountTypeName, accountNumber);
            accountInfoList.add(accountInfo);


            // Display other account details as needed
         //   Log.d("UserAccount", "Account Name: " + accountName);
         //   Log.d("UserAccount", "Account Type Name: " + accountTypeName);
         //   Log.d("UserAccount", "Account ID: " + accountId);
            //   Log.d("UserAccount", "Current Balance: " + currentBalance);
            // Log.d("User Account", "Account Number: " + account.getAccountNumber());
            // Log.d("User Account", "Current Balance: " + account.getCurrentBalance());
            // Log.d("User Account", "Opening Balance: " + account.getOpeningBalance());
            // Log.d("User Account", "Interest Rate: " + account.getInterestRate());
            if (account.getAccountType() != null) {
            //    Log.d("User Account", "Account Type Name: " + account.getAccountType().getName());
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

             //   Log.d("API", "Selected Account ID from Drop Down = : " + selectedAccountId);
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
        TextView accountTypeNameTextView = getView().findViewById(R.id.accountTypeName);
        if (accountTypeNameTextView != null) {
            accountTypeNameTextView.setText(" " + accountInfo.getAccountName());
        } else {
            Log.e("ProfileFragment", "TextView 'accountName' is null");
        }

        TextView accountNumberTextView = getView().findViewById(R.id.accountNumber);
        if (accountNumberTextView != null) {

            accountNumberTextView.setText(" " + accountInfo.getAccountNumber());
        } else {
            Log.e("ProfileFragment", "TextView 'accountNumber' is null");
        }

        // Check if 'balance' TextView is null before setting text
        TextView balanceTextView = getView().findViewById(R.id.balance);
        if (balanceTextView != null) {
            balanceTextView.setText(" " + accountInfo.getCurrentBalance());
        } else {
            Log.e("ProfileFragment", "TextView 'balance' is null");
        }
    }

    private void displayUserProfile(UserProfileResponse userProfileResponse) {

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
                        if (progressTextView != null) {
                            progressTextView.setVisibility(View.VISIBLE);
                        }
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

        if (tableLayout == null) {
            Log.e("ProfileFragment", "TableLayout is null");
            return; // Exit the method if TableLayout is null
        }

        // Clear existing rows in the TableLayout except the header row
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        // Loop through the transactions and add new rows to the TableLayout
        for (TransactionResponse transaction : accountTransactions) {
            // Create a new TableRow
            TableRow row = new TableRow(requireContext());
            if (progressTextView != null) {
                progressTextView.setVisibility(View.VISIBLE);
            }

            // Create TextViews to display the transaction details
            TextView descriptionTextView = new TextView(requireContext());
            descriptionTextView.setText(transaction.getDescription());
            setTextViewAttributes(descriptionTextView, 6, Gravity.START, 16); // Adjust weight to occupy 40% of the width

            TextView amountTextView = new TextView(requireContext());
            String amountValue = String.valueOf(transaction.getAmount());
            amountTextView.setText(amountValue);
            // Set text color to green if the amount is negative
            if (transaction.getAmount() < 0) {
                amountTextView.setTextColor(Color.parseColor("#006400"));
            }
            setTextViewAttributes(amountTextView, 2, Gravity.CENTER, 16); // Adjust weight to occupy 30% of the width

            TextView runningBalanceTextView = new TextView(requireContext());
            runningBalanceTextView.setText(String.valueOf(transaction.getRunningBalance()));
            // Set text color to green if the running balance is negative
            if (transaction.getRunningBalance() < 0) {
                runningBalanceTextView.setTextColor(Color.parseColor("#006400"));
            }
            setTextViewAttributes(runningBalanceTextView, 2, Gravity.CENTER, 16); // Adjust weight to occupy 30% of the width

            // Add TextViews to the TableRow
            row.addView(descriptionTextView);
            row.addView(amountTextView);
            row.addView(runningBalanceTextView);

            // Call this method for each TextView
            setFixedHeight(descriptionTextView);
            setFixedHeight(amountTextView);
            setFixedHeight(runningBalanceTextView);

            // Apply the border/background to TextViews
            descriptionTextView.setBackgroundResource(R.drawable.border_background); // Apply the border
            amountTextView.setBackgroundResource(R.drawable.border_background); // Apply the border
            runningBalanceTextView.setBackgroundResource(R.drawable.border_background); // Apply the border

            // Add the TableRow to the TableLayout
            tableLayout.addView(row);
            progressTextView.setVisibility(View.INVISIBLE);

        }
    }


    private void setTextViewAttributes(TextView textView, int weight, int gravity, int padding) {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                weight
        );
        layoutParams.gravity = gravity;
        textView.setLayoutParams(layoutParams);
        textView.setPadding(padding, 0, padding, 0); // Add horizontal padding
    }



    private void switchToProfileFragment() {
        // Log to check if this method is being called
        // Log.d("ProfileFragment", "Switching to Check Accounts");

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

    }
    public void onResume() {
        super.onResume();

    }

    // Add this method to set fixed height for TextViews
    private void setFixedHeight(TextView textView) {
        // Set a fixed height for TextView
        textView.setHeight(getResources().getDimensionPixelSize(R.dimen.row_height));
    }

    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
        progressBar.setVisibility(View.GONE); // Hide progressBar after receiving response
        // Rest of your code...
    }


    public void onFailure(Call<JsonObject> call, Throwable t) {
        progressBar.setVisibility(View.GONE); // Hide progressBar in case of API call failure
        // Handle failure...
    }


    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        logoutListener = (MyInterface) activity;

        // Log to check if onAttach is being called
        Log.d("ProfileFragment", "onAttach");
    }
}
