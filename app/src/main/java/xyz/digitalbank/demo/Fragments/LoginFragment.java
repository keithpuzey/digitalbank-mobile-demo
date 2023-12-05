package xyz.digitalbank.demo.Fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.Model.User;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;
import xyz.digitalbank.demo.Services.ServiceApi;
import xyz.digitalbank.demo.Services.RetrofitClient;
import xyz.digitalbank.demo.Extras.AppPreference;
import android.widget.ImageView;
import android.content.Intent;


public class LoginFragment extends Fragment {

    private MyInterface loginFromActivityListener;
    private TextView registerTV;

    public EditText emailInput, passwordInput;
    private Button loginBtn;
    private ServiceApi serviceApi;

    public String email ;
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);


        // Initialize the ServiceApi instance
        serviceApi = RetrofitClient.getRetrofitInstance().create(ServiceApi.class);

        // for login
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginBtn = view.findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        registerTV = view.findViewById(R.id.registerTV);
        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginFromActivityListener.register();
            }
        });

        ImageView cogIcon = view.findViewById(R.id.cogIcon);
        cogIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            loginFromActivityListener = (MyInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MyInterface");
        }
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
            MainActivity.appPreference.showToast("Password may be at least 8 characters long.");
        } else {
            Call<User> userCall = serviceApi.doLogin(Email, Password);
            userCall.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.body() != null) {
                        Log.d("Response", "Response: " + response.body());

                        ((MainActivity) requireActivity()).setEmail(Email);

                        MainActivity.appPreference.setLoginStatus(true);
                        loginFromActivityListener.login(response.body().getAuthToken(), Email );


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
                }
            });
        }
    }
}