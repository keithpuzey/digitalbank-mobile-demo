package xyz.digitalbank.demo.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.Constants.ConstantsManager;
import xyz.digitalbank.demo.Model.User;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;
import xyz.digitalbank.demo.Services.RetrofitClient;
import xyz.digitalbank.demo.Services.ServiceApi;


@RequiresApi(api = Build.VERSION_CODES.P)

public class LoginFragment extends Fragment {
    private MyInterface myInterface;
    private TextView registerTV;
    private EditText emailInput, passwordInput;
    private Button loginBtn ;
    private ImageButton biometricBtn ;
    private ServiceApi serviceApi;
    private static final int REQUEST_BIOMETRIC_PERMISSION = 1;
    private Context context; // Declare the context variable
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Check if the hosting activity implements MyInterface
        if (context instanceof MyInterface) {
            myInterface = (MyInterface) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MyInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        String BASE_URL = ConstantsManager.getBaseUrl(context);
        Log.e("Login", "BASE URL is = " + BASE_URL);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize the ServiceApi instance
        serviceApi = RetrofitClient.getRetrofitInstance(context).create(ServiceApi.class);

        // for login
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginBtn = view.findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LoginFragment", "Login button clicked");
                loginUser();
            }
        });
        biometricBtn = view.findViewById(R.id.biometricBtn);


        registerTV = view.findViewById(R.id.registerTV);
        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myInterface.register(); // Corrected method name
            }
        });



        biometricBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle biometric authentication
                if (isBiometricPromptEnabled()) {
                    checkBiometricPermission();
                }
            }
        });


        ImageView cogIcon = view.findViewById(R.id.cogIcon);
        cogIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dataPath = getActivity().getFilesDir().getAbsolutePath();

                Log.d("TesseractOCR", "Data Path: " + dataPath);
                // Open the new page for editing constants
                openConstantsPage();
            }
        });

        return view;
    }
    private void openConstantsPage() {
        // Here, you should navigate to the new page for editing constants.
        // You can use Intent to start a new activity or FragmentTransaction to replace the current fragment.

        // For example, if using Intent:
        Intent intent = new Intent(getActivity(), ConstantsEditActivity.class);
        startActivity(intent);
    }

    private boolean isBiometricPromptEnabled() {
        // Check if biometric authentication is available on the device
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED);
    }

    private void checkBiometricPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.USE_BIOMETRIC},
                    REQUEST_BIOMETRIC_PERMISSION);
        } else {
            showBiometricPrompt();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BIOMETRIC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, show biometric prompt
                showBiometricPrompt();
            } else {
                // Permission not granted, show a Toast
                showToast("Biometric authentication is not available.");
            }
        }
    }

    private void showToast(String message) {
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);

                        if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                            showToast("No fingerprints enrolled. Please enroll a fingerprint in your device settings.");
                        } else {
                            Log.e("Biometric", "Authentication error: " + errString);
                        }
                    }
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Log.d("Biometric", "Authentication succeeded");

                        try {
                            // Handle successful authentication
                            loginUserWithBiometric();
                        } catch (Exception e) {
                            // Log any exceptions that might occur during authentication
                            Log.e("Biometric", "Exception during authentication: " + e.getMessage(), e);
                        }
                    }


                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Log.e("Biometric", "Authentication failed");
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Finger Print  Login")
                .setNegativeButtonText("Cancel")
                .setDescription("Place your fingerprint on the sensor to log in.")
                .setNegativeButtonText("Biometric authentication failed. Try again.")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void loginUserWithBiometric() {

        emailInput.setText("nsmith@demo.io");
        passwordInput.setText("Demo123!");

        // Perform login with the predefined user credentials
        loginUser();

    }



    private void loginUser() {
        String Email = emailInput.getText().toString();
        String Password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(Email)) {
            MainActivity.appPreference.showToast("Your email is required.");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            MainActivity.appPreference.showToast("Invalid email");
        } else if (TextUtils.isEmpty(Password)) {
            MainActivity.appPreference.showToast("Password required");
        } else if (Password.length() < 8) {
            MainActivity.appPreference.showToast("Password must be at least 8 characters long.");
        } else {
            Call<User> userCall = serviceApi.doLogin(Email, Password);
            userCall.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.body() != null) {
                        Log.d("Response", "Response: " + response.body());

                        ((MainActivity) requireActivity()).setEmail(Email);

                        MainActivity.appPreference.setLoginStatus(true);
                        Log.d("Login", " Login API Being Called");

                        myInterface.login(response.body().getAuthToken(), Email);

                    } else {
                        // Login failed
                        MainActivity.appPreference.showToast("Error. Login Failed");
                        emailInput.setText("");
                        passwordInput.setText("");
                    }
                }


                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // Handle failure
                    Log.e("Login", "Error during login API call", t);

                    if (t instanceof IOException) {
                        // This exception is thrown for network-related issues
                        showToast("Server is not available. Please check your internet connection.");
                    } else if (t instanceof HttpException) {
                        // This exception is thrown for HTTP error responses
                        HttpException httpException = (HttpException) t;
                        Response<?> response = httpException.response();
                        if (response != null && response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("Login", "Error response: " + errorBody);
                                // Handle the errorBody as needed
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            showToast("Error during login. Please try again.");
                        }
                    } else {
                        showToast("Error during login. Please try again.");
                    }
                }


            });
        }
    }
}
