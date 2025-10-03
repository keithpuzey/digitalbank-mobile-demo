package xyz.digitalbank.demo.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.Model.AccountInfo;
import xyz.digitalbank.demo.Model.DepositRequest;
import xyz.digitalbank.demo.Model.UserAccountResponse;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.RetrofitClient;



public class TransferFragment extends Fragment {

    private ArrayAdapter<AccountInfo> adapter;

    private List<UserAccountResponse> userAccounts = new ArrayList<>();

    private List<AccountInfo> accountInfoList = new ArrayList<>();
    private Spinner accountSpinner;
    private EditText descriptionEditText;
    private String authToken;
    private int loggedinuserId;
    private TessBaseAPI tessBaseAPI;
    private TesseractOCRAsyncTask ocrAsyncTask;
    private Context context;
    private boolean isOCRInitialized = false;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;

    private EditText amountEditText;
    private RadioGroup transactionTypeRadioGroup;
    private RadioButton creditRadioButton;
    private RadioButton debitRadioButton;
    private static final String TAG = TransferFragment.class.getSimpleName();



    // Declare PopupWindow and its components
    private PopupWindow popupWindow;
    private TextView userNameTextView;
    private TextView logoutLinkTextView;



    private boolean isValidAmount(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            return amount >= 0; // Assuming negative amounts are not allowed
        } catch (NumberFormatException e) {
            return false; // Parsing failed, so it's not a valid amount
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();

        // Set Tesseract debug level and redirect logging output
        setTesseractDebugLevel(3); // Set the desired debug level
        redirectTesseractLogging();
        // Initialize OCR asynchronously
        new InitOCRAsyncTask().execute();
    }

    // Method to set Tesseract debug level
    private void setTesseractDebugLevel(int level) {
        try {
            // Set the environment variable
            Process process = Runtime.getRuntime().exec("setprop TESSDATA_DEBUG " + level);
            process.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Error setting Tesseract debug level: " + e.getMessage());
        }
    }

    // Method to redirect Tesseract logging output to a file
    private void redirectTesseractLogging() {
        try {
            // Define the log file path
            File logFile = new File(getContext().getFilesDir(), "tesseract.log");

            // Redirect System.out and System.err to the log file
            OutputStream outputStream = new FileOutputStream(logFile);
            System.setOut(new PrintStream(outputStream));
            System.setErr(new PrintStream(outputStream));
        } catch (IOException e) {
            Log.e(TAG, "Error redirecting Tesseract logging: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.your_transfer_screen_layout, container, false);
        amountEditText = view.findViewById(R.id.amountEditText);
        accountSpinner = view.findViewById(R.id.accountSpinner);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        Button submitButton = view.findViewById(R.id.submitButton);
        ImageView cameraIcon = view.findViewById(R.id.cameraIcon);

        // Get references to UI elements in your transfer screen layout


        transactionTypeRadioGroup = view.findViewById(R.id.transactionTypeRadioGroup);
        creditRadioButton = view.findViewById(R.id.creditRadioButton);
        debitRadioButton = view.findViewById(R.id.debitRadioButton);


        ImageView toolbarImage = view.findViewById(R.id.toolbar_image);
        toolbarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(toolbarImage);
            }
        });

        View rootLayout = view.findViewById(R.id.transfer_root_layout);
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

        setupAccountSpinner(accountSpinner);

        // Add a TextWatcher to validate input in amountEditText
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Validate input after text is changed
                String amountStr = s.toString();
                if (!isValidAmount(amountStr)) {
                    // Clear the text and show a message
                    Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        submitButton.setOnClickListener(v -> {
            // Get selected account, entered amount, and credit/debit selection
            AccountInfo selectedAccount = (AccountInfo) accountSpinner.getSelectedItem();
            int selectedAccountId = selectedAccount.getId();
            loggedinuserId = ((MainActivity) requireActivity()).getLoggedinuserId();
            authToken = MainActivity.appPreference.getauthToken();
            String amountStr = amountEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            boolean isCredit = creditRadioButton.isChecked();

            // Validate and parse the amount
            if (!TextUtils.isEmpty(amountStr)) {
                double amount = Double.parseDouble(amountStr);
                transferFunds(loggedinuserId, authToken, amount, selectedAccountId, isCredit, description);
            } else {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });

        cameraIcon.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        });

        return view;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(requireContext(), "Cheque Not Found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            performOCR(imageBitmap);
        }
    }

    private void performOCR(Bitmap imageBitmap) {
        if (isOCRInitialized) {
            ocrAsyncTask = new TesseractOCRAsyncTask(requireContext());
            ocrAsyncTask.execute(imageBitmap);
        } else {
            Toast.makeText(context, "OCR engine is not initialized", Toast.LENGTH_SHORT).show();
        }
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
    private void initializeOCR() {
        tessBaseAPI = new TessBaseAPI();
        String dataPath = requireActivity().getFilesDir().getAbsolutePath();
        String lang = "eng";
        String tessDataPath = dataPath + "/tessdata/";
        File tessDataFolder = new File(tessDataPath);
        if (!tessDataFolder.exists()) {
            tessDataFolder.mkdirs();
        }
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outputStream = new FileOutputStream(new File(context.getFilesDir() + "/tessdata/", "eng.traineddata"));

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tessBaseAPI.init(dataPath, lang);
        isOCRInitialized = true;
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
        RetrofitClient.getServiceApi(context).getUserAccounts(loggedinuserId, authToken)
                .enqueue(new Callback<List<UserAccountResponse>>() {
                    @Override
                    public void onResponse(Call<List<UserAccountResponse>> call, Response<List<UserAccountResponse>> response) {
                        if (response.isSuccessful()) {
                            // Process the list of UserAccountResponse
                            List<UserAccountResponse> userAccounts = response.body();
                            displayUserAccounts(userAccounts, accountSpinner);
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

    private void displayUserAccounts(List<UserAccountResponse> userAccounts, Spinner accountSpinner) {
        // Clear existing data
        accountInfoList.clear();

        for (UserAccountResponse account : userAccounts) {
            int accountId = account.getId();
            String accountName = account.getName();
            String currentBalanceStr = String.valueOf(account.getCurrentBalance());
            double currentBalance = Double.parseDouble(currentBalanceStr);
            int accountNumber = account.getAccountNumber();

            // Instantiate AccountInfo and add it to the list
            AccountInfo accountInfo = new AccountInfo(accountId, accountName, currentBalance, "SomeDefaultValue", accountNumber );
            accountInfoList.add(accountInfo);
        }

        // Log the number of accounts for debugging
      //  Log.d("TransferFragment", "Number of accounts: " + accountInfoList.size());

        // Create an ArrayAdapter with AccountInfo objects
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, accountInfoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        accountSpinner.setAdapter(adapter);

        // Log the exit point of the method
      //  Log.d("TransferFragment", "Exiting setupAccountSpinner");
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
     //   Log.d("TransferFragment", "JSON Request Body: " + jsonBody);


        // Log the contents of the depositRequest
   //     Log.d("TransferFragment", "transfer Funds Initiated - userId = " + userId + " AuthToken " + authToken + " Body = " + depositRequest.toString());

        Context context = getContext();
        RetrofitClient.transferFunds(toAccountId, authToken, "application/json", depositRequest, isCredit, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
            //        Log.d("TransferFragment", "Submitted Transfer Request - Amount: " + amount + ", To Account ID: " + toAccountId + ", Is Credit? " + isCredit + ", Description: " + description);
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
        }, context);
    }

    // Call this method to update the user accounts in the TransferFragment
    public void updateUserAccounts(List<UserAccountResponse> accounts) {
        userAccounts = accounts;
    }

    private class InitOCRAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Copy traineddata file in background
            copyTrainedData();
            // Initialize OCR engine
            initializeOCR();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Toast.makeText(context, "Cheque Reader Ready", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyTrainedData() {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outputStream = new FileOutputStream(new File(context.getFilesDir() + "/tessdata/", "eng.traineddata"));

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TesseractOCRAsyncTask extends AsyncTask<Bitmap, Void, Pair<String, String>> {

        private Context context;

        public TesseractOCRAsyncTask(Context context) {
            this.context = context;
        }

        private void saveBitmapToStorage(Bitmap bitmap, String fileName) {
            File file = new File(context.getExternalFilesDir(null), fileName);
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected Pair<String, String> doInBackground(Bitmap... bitmaps) {
            Bitmap imageBitmap = bitmaps[0];
            // Get image dimensions
            int width = imageBitmap.getWidth();
            int height = imageBitmap.getHeight();

        //    Log.d("OCR", "Height =" + height);
        //    Log.d("OCR", "Width =" + width);
            // Define regions of interest (ROIs)
            Rect amountROI = new Rect((int) (width * 2 / 3), 0, width, height);
            Rect descriptionROI = new Rect(0, 0, (int) (width / 3), height);

            // Crop the image to extract text from ROIs
            Bitmap amountBitmap = Bitmap.createBitmap(imageBitmap, amountROI.left, amountROI.top, amountROI.width(), amountROI.height());
            Bitmap descriptionBitmap = Bitmap.createBitmap(imageBitmap, descriptionROI.left, descriptionROI.top, descriptionROI.width(), descriptionROI.height());



            // Initialize OCR for amount and description separately
            TessBaseAPI amountTessBaseAPI = new TessBaseAPI();
            TessBaseAPI descriptionTessBaseAPI = new TessBaseAPI();

            // Set language for OCR
            amountTessBaseAPI.init(context.getFilesDir().getAbsolutePath(), "eng");
            descriptionTessBaseAPI.init(context.getFilesDir().getAbsolutePath(), "eng");

            // Set images for OCR
            amountTessBaseAPI.setImage(amountBitmap);
            descriptionTessBaseAPI.setImage(descriptionBitmap);

            // Perform OCR on ROIs
            String amountText = amountTessBaseAPI.getUTF8Text();
            String descriptionText = descriptionTessBaseAPI.getUTF8Text();

            // Release resources
            amountTessBaseAPI.end();
            descriptionTessBaseAPI.end();

            return new Pair<>(amountText, descriptionText);
        }

        private boolean isValidAmount(String amountStr) {
            if (TextUtils.isEmpty(amountStr)) {
                return true; // Allow empty input
            }
            try {
                double amount = Double.parseDouble(amountStr);
                return amount >= 0; // Ensure amount is non-negative
            } catch (NumberFormatException e) {
                return false; // Not a valid numeric value
            }
        }
        @Override
        protected void onPostExecute(Pair<String, String> result) {
            super.onPostExecute(result);
            String amountText = result.first;
            String descriptionText = result.second;
            if (!TextUtils.isEmpty(amountText) && !TextUtils.isEmpty(descriptionText)) {
                // Handle extracted text
                amountEditText.setText(amountText.trim());
                descriptionEditText.setText(descriptionText.trim());
            } else {
                // Show a message indicating no data was found
                Toast.makeText(context, "Cheque Not Found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}