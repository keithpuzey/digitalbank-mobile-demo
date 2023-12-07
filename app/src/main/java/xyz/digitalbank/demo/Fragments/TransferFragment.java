package xyz.digitalbank.demo.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import xyz.digitalbank.demo.Model.UserAccountResponse;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.RetrofitClient;
import xyz.digitalbank.demo.Activity.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.digitalbank.demo.Model.AccountInfo;

import java.util.ArrayList;
import java.util.List;

public class TransferFragment extends Fragment {

    private List<UserAccountResponse> userAccounts = new ArrayList<>();
    private List<AccountInfo> accountInfoList = new ArrayList<>(); // Declare it at the class level

    private String authToken;
    private int loggedinuserId;

    private Spinner accountSpinner;

    public TransferFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);

        FrameLayout transferContainer = view.findViewById(R.id.transferContainer);

        View transferScreen = inflater.inflate(R.layout.your_transfer_screen_layout, transferContainer, false);

        // Get references to UI elements in your transfer screen layout
        accountSpinner = transferScreen.findViewById(R.id.accountSpinner);
        EditText amountEditText = transferScreen.findViewById(R.id.amountEditText);
        Button submitButton = transferScreen.findViewById(R.id.submitButton);

        // Set up the account spinner with account data
        setupAccountSpinner(accountSpinner);

        // Set up click listener for the submit button
        submitButton.setOnClickListener(v -> {
            // Get selected account and entered amount
            String selectedAccount = accountSpinner.getSelectedItem().toString();
            String amount = amountEditText.getText().toString();

            // Call your API with the selected account and amount
            // TODO: Add your API call logic here

            // Example: Display a toast message
            // Toast.makeText(requireContext(), "Transfer submitted: " + selectedAccount + ", Amount: " + amount, Toast.LENGTH_SHORT).show();
        });

        transferContainer.addView(transferScreen);

        return view;
    }

    private void setupAccountSpinner(Spinner accountSpinner) {
        Log.d("TransferFragment", "Entering setupAccountSpinner");

        // Clear the list before adding new items
        accountInfoList.clear();

        // Get authToken and loggedinuserId
        authToken = MainActivity.appPreference.getauthToken();
        loggedinuserId = ((MainActivity) requireActivity()).getLoggedinuserId();

        // Call the method to get user accounts
        getUserAccounts(authToken, loggedinuserId);
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
                            displayUserAccounts(userAccounts, accountSpinner);
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

    private void displayUserAccounts(List<UserAccountResponse> userAccounts, Spinner accountSpinner) {
        for (UserAccountResponse account : userAccounts) {
// Extract information from UserAccountResponse
            int accountId = account.getId();
            String accountName = account.getName();
            String currentBalanceStr = String.valueOf(account.getCurrentBalance());

// Convert currentBalance to double
            double currentBalance = Double.parseDouble(currentBalanceStr);

// Create an AccountInfo object and add it to the list
            AccountInfo accountInfo = new AccountInfo(accountId, accountName, currentBalance);

            accountInfoList.add(accountInfo);
        }

        // Log the number of accounts for debugging
        Log.d("TransferFragment", "Number of accounts: " + accountInfoList.size());

        // Create an ArrayAdapter with AccountInfo objects
        ArrayAdapter<AccountInfo> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, accountInfoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        accountSpinner.setAdapter(adapter);

        // Log the exit point of the method
        Log.d("TransferFragment", "Exiting setupAccountSpinner");
    }

    // Call this method to update the user accounts in the TransferFragment
    public void updateUserAccounts(List<UserAccountResponse> accounts) {
        userAccounts = accounts;
    }
}
