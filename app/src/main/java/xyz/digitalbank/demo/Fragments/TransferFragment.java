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
import xyz.digitalbank.demo.Model.DepositRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.digitalbank.demo.Model.AccountInfo;
import java.util.ArrayList;
import java.util.List;
import android.text.TextUtils;
import android.widget.Toast;
import android.widget.CheckBox;
import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import com.google.gson.annotations.SerializedName;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.Manifest;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import android.app.Activity;

public class TransferFragment extends Fragment {

    private List<UserAccountResponse> userAccounts = new ArrayList<>();

    private ArrayAdapter<AccountInfo> adapter;
    private List<AccountInfo> accountInfoList = new ArrayList<>();

    private CheckBox creditDebitCheckBox;

    private String authToken;
    private int loggedinuserId;

    private Spinner accountSpinner;

    private int selectedAccountId;

    private RadioGroup transactionTypeRadioGroup;
    private RadioButton creditRadioButton;
    private RadioButton debitRadioButton;
    private Button submitButton;


    private EditText descriptionEditText;



    public TransferFragment() {
        // Required empty public constructor
    }

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2; // Use any integer value you prefer




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);

        FrameLayout transferContainer = view.findViewById(R.id.transferContainer);

        View transferScreen = inflater.inflate(R.layout.your_transfer_screen_layout, transferContainer, false);

        // Get references to UI elements in your transfer screen layout
        accountSpinner = transferScreen.findViewById(R.id.accountSpinner);
        EditText amountEditText = transferScreen.findViewById(R.id.amountEditText);
        descriptionEditText = transferScreen.findViewById(R.id.descriptionEditText);
        transactionTypeRadioGroup = transferScreen.findViewById(R.id.transactionTypeRadioGroup);
        creditRadioButton = transferScreen.findViewById(R.id.creditRadioButton);
        debitRadioButton = transferScreen.findViewById(R.id.debitRadioButton);
        submitButton = transferScreen.findViewById(R.id.submitButton);



        // Set up the account spinner with account data
        setupAccountSpinner(accountSpinner);

        // Set up click listener for the submit button
        submitButton.setOnClickListener(v -> {
            // Get selected account, entered amount, and credit/debit selection
            AccountInfo selectedAccount = (AccountInfo) accountSpinner.getSelectedItem();
            selectedAccountId = selectedAccount.getId();
            loggedinuserId = ((MainActivity) requireActivity()).getLoggedinuserId();
            authToken = MainActivity.appPreference.getauthToken();
            String amountStr = amountEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            // Get the selected transaction type (credit/debit)
            boolean isCredit = creditRadioButton.isChecked();

            // Validate and parse the amount
            if (!TextUtils.isEmpty(amountStr)) {
                double amount = Double.parseDouble(amountStr);
                Log.d("TransferFragment", "Submit Button Clicked - Logged in user  " + loggedinuserId + " Amount = " + amount + " Select Account ID = " + selectedAccount.getId());
                // Call the transferFunds API
                transferFunds(loggedinuserId, authToken, Double.parseDouble(amountStr), selectedAccountId, isCredit, description);




            } else {
                // Handle case where amount is empty
                // Example: Display an error message
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView cameraIcon = transferScreen.findViewById(R.id.cameraIcon);
        cameraIcon.setOnClickListener(v -> {
            // Check if the camera permission is granted
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // Open the camera
                dispatchTakePictureIntent();
            } else {
                // Request camera permission
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        });


        transferContainer.addView(transferScreen);

        return view;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // TODO: Process the captured image and extract information
            // For OCR (Optical Character Recognition), you can use a library like Tesseract.
            String extractedText = performOCR(imageBitmap);

            // Populate description field with extracted information
            descriptionEditText.setText(extractedText);
        }
    }

    // Example OCR method (requires Tesseract library)
    private String performOCR(Bitmap bitmap) {
        // TODO: Implement OCR logic here
        return "Extracted Text";
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
            int accountId = account.getId();
            String accountName = account.getName();
            String currentBalanceStr = String.valueOf(account.getCurrentBalance());
            double currentBalance = Double.parseDouble(currentBalanceStr);

            // Create an AccountInfo object and add it to the list
            AccountInfo accountInfo = new AccountInfo(accountId, accountName, currentBalance);
            accountInfoList.add(accountInfo);
        }

        // Log the number of accounts for debugging
        Log.d("TransferFragment", "Number of accounts: " + accountInfoList.size());

        // Create an ArrayAdapter with AccountInfo objects
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, accountInfoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        accountSpinner.setAdapter(adapter);

        // Log the exit point of the method
        Log.d("TransferFragment", "Exiting setupAccountSpinner");
    }

    private void transferFunds(int userId, String authToken, double amount, int toAccountId, boolean isCredit, String description) {
        // Create a DepositRequest object
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setAmount(amount);
        depositRequest.setToAccountId(toAccountId);
        depositRequest.setTransactionTypeCode(isCredit ? "RFD" : "DBT");
        depositRequest.setDescription(description);

        // Convert DepositRequest to JSON string manually
        Gson gson = new Gson();
        String jsonBody = gson.toJson(depositRequest);

        // Log the contents of the JSON request body
        Log.d("TransferFragment", "JSON Request Body: " + jsonBody);


        // Log the contents of the depositRequest
        Log.d("TransferFragment", "transfer Funds Initiated - userId = " + userId + " AuthToken " + authToken + " Body = " + depositRequest.toString());

        RetrofitClient.transferFunds(toAccountId, authToken, "application/json", depositRequest, isCredit, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("TransferFragment", "Submitted Transfer Request - Amount: " + amount + ", To Account ID: " + toAccountId + ", Is Credit? " + isCredit + ", Description: " + description);
                    // API call successful, handle the response as needed
                    // Example: Display a toast message
                    Toast.makeText(requireContext(), "Update successful", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle the case where the API call was not successful
                    MainActivity.appPreference.showToast("Update failed");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle failure of transferFunds API
                // Example: Display an error message
                // MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
            }
        });
    }
    
    // Call this method to update the user accounts in the TransferFragment
    public void updateUserAccounts(List<UserAccountResponse> accounts) {
        userAccounts = accounts;
    }
}
