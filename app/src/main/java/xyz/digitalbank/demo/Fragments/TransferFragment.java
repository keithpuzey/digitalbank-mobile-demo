package xyz.digitalbank.demo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import androidx.fragment.app.Fragment;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Model.UserAccountResponse;
import xyz.digitalbank.demo.Model.AccountInfo;
import java.util.ArrayList;
import java.util.List;

public class TransferFragment extends Fragment {

//    private List<UserAccountResponse> userAccounts;
    private List<UserAccountResponse> userAccounts = new ArrayList<>();

    private String authToken;

    public TransferFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);

        FrameLayout transferContainer = view.findViewById(R.id.transferContainer);

        View transferScreen = inflater.inflate(R.layout.your_transfer_screen_layout, transferContainer, false);

        // Get references to UI elements in your transfer screen layout
        Spinner accountSpinner = transferScreen.findViewById(R.id.transferAccountSpinner);
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
        // Create a list of AccountInfo to hold account names, numbers, IDs, and current balances
        List<AccountInfo> accountInfoList = new ArrayList<>();

        for (UserAccountResponse account : userAccounts) {
            int accountId = account.getId();
            String accountName = account.getName();
            String currentBalance = String.valueOf(account.getCurrentBalance());

            AccountInfo accountInfo = new AccountInfo(accountId, accountName, currentBalance);
            accountInfoList.add(accountInfo);
        }

        // Create an ArrayAdapter with AccountInfo objects
        ArrayAdapter<AccountInfo> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, accountInfoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        accountSpinner.setAdapter(adapter);
    }

    // Call this method to update the user accounts in the TransferFragment
    public void updateUserAccounts(List<UserAccountResponse> accounts) {
        userAccounts = accounts;

        // If the view is already created, update the account spinner
        View view = getView();
        if (view != null) {
            Spinner accountSpinner = view.findViewById(R.id.transferAccountSpinner);
            if (accountSpinner != null) {
                setupAccountSpinner(accountSpinner);
            }
        }
    }
}
