package xyz.digitalbank.demo.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
    private Button loginBtn;
    private ImageButton biometricBtn;
    private ServiceApi serviceApi;
    private static final int REQUEST_BIOMETRIC_PERMISSION = 1;
    private Context context;

    // --- Lockout fields ---
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 30000; // 30 seconds
    private int failedAttempts = 0;
    private boolean isLockedOut = false;
    private Handler lockoutHandler = new Handler();

    private TextView versionTextView;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        if (context == null) {
            Log.e("YourFragment", "getContext() returned null in onCreateView()");
            return null;
        }

        String BASE_URL = ConstantsManager.getBaseUrl(context);
        Log.d("SharedPreferences", "BASE_URL: " + BASE_URL);

        String MOCK_URL = ConstantsManager.getMockUrl(context);
        Log.d("SharedPreferences", "MOCK_URL: " + MOCK_URL);

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        String buildNumber = getString(R.string.app_version);
        versionTextView = view.findViewById(R.id.versionTextView);
        versionTextView.setText("Version :" + buildNumber);

        serviceApi = RetrofitClient.getRetrofitInstance(context).create(ServiceApi.class);

        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginBtn = view.findViewById(R.id.loginBtn);
        biometricBtn = view.findViewById(R.id.biometricBtn);

        loginBtn.setOnClickListener(v -> {
            if (isLockedOut) {
                showToast("Too many failed attempts. Please wait 30 seconds.");
            } else {
                loginUser();
            }
        });

        // Prepopulate fields
        emailInput.setText("jsmith@demo.io");
        passwordInput.setText("Demo123!");

        registerTV = view.findViewById(R.id.registerTV);
        registerTV.setOnClickListener(v -> myInterface.register());

        biometricBtn.setOnClickListener(v -> {
            if (isLockedOut) {
                showToast("Account locked. Try again in 30 seconds.");
            } else if (isBiometricPromptEnabled()) {
                checkBiometricPermission();
            }
        });

        ImageView cogIcon = view.findViewById(R.id.cogIcon);
        cogIcon.setOnClickListener(v -> openConstantsPage());

        return view;
    }

    // --- Lockout handling ---
    private void startLockout() {
        isLockedOut = true;

        // Disable login button
        loginBtn.setEnabled(false);
        loginBtn.setAlpha(0.5f);

        // Disable biometric button
        biometricBtn.setEnabled(false);
        biometricBtn.setAlpha(0.5f);

        showToast("Account locked. Try again in 30 seconds.");

        lockoutHandler.postDelayed(() -> {
            failedAttempts = 0;
            isLockedOut = false;

            // Re-enable login button
            loginBtn.setEnabled(true);
            loginBtn.setAlpha(1.0f);

            // Re-enable biometric button
            biometricBtn.setEnabled(true);
            biometricBtn.setAlpha(1.0f);

            showToast("You can try logging in again.");
        }, LOCKOUT_DURATION_MS);
    }

    private void openConstantsPage() {
        Intent intent = new Intent(getActivity(), ConstantsEditActivity.class);
        startActivity(intent);
    }

    private boolean isBiometricPromptEnabled() {
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
                showBiometricPrompt();
            } else {
                showToast("Biometric authentication is not available.");
            }
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        loginUserWithBiometric();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Log.e("Biometric", "Authentication failed");
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Finger Print Login")
                .setDescription("Place your fingerprint on the sensor to log in.")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void loginUserWithBiometric() {
        emailInput.setText("nsmith@demo.io");
        passwordInput.setText("Demo123!");
        loginUser();
    }

    private void loginUser() {
        if (isLockedOut) {
            showToast("Account locked. Try again later.");
            return;
        }

        String Email = emailInput.getText().toString();
        String Password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(Email)) {
            MainActivity.appPreference.showToast("Your email is required.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            MainActivity.appPreference.showToast("Invalid email");
            return;
        }
        if (TextUtils.isEmpty(Password)) {
            MainActivity.appPreference.showToast("Password required");
            return;
        }
        if (Password.length() < 8) {
            MainActivity.appPreference.showToast("Password must be at least 8 characters long.");
            return;
        }

        Call<User> userCall = serviceApi.doLogin(Email, Password);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.body() != null) {
                    failedAttempts = 0; // reset on success
                    ((MainActivity) requireActivity()).setEmail(Email);
                    MainActivity.appPreference.setLoginStatus(true);
                    myInterface.login(response.body().getAuthToken(), Email);
                } else {
                    failedAttempts++;
                    if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {
                        startLockout();
                    } else {
                        MainActivity.appPreference.showToast("Error. Login Failed (" +
                                failedAttempts + "/" + MAX_LOGIN_ATTEMPTS + ")");
                        emailInput.setText("");
                        passwordInput.setText("");
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                failedAttempts++;
                if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {
                    startLockout();
                } else {
                    showToast("Error during login. Please try again. (" +
                            failedAttempts + "/" + MAX_LOGIN_ATTEMPTS + ")");
                }
                Log.e("Login", "Error during login API call", t);
            }
        });
    }
}