package xyz.digitalbank.demo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import xyz.digitalbank.demo.Extras.AppPreference;
import xyz.digitalbank.demo.Fragments.LoginFragment;
import xyz.digitalbank.demo.Fragments.ProfileFragment;
import xyz.digitalbank.demo.Fragments.RegistrationFragment;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;

import android.util.Log;
import android.view.View;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import xyz.digitalbank.demo.Services.ServiceApi;
import xyz.digitalbank.demo.Services.RetrofitClient;
import xyz.digitalbank.demo.Fragments.AtmSearchFragment;
import xyz.digitalbank.demo.Fragments.TransferFragment;


public class MainActivity extends AppCompatActivity implements MyInterface {

    private BottomNavigationView bottomNavigationView;
    public static AppPreference appPreference;
    private ServiceApi serviceApi;

    private String email ;
    private int loggedinuserId ;

    // Add a method to set the email
    public void setEmail(String email) {
        this.email = email;
    }

    // Add this method to set the loggedinuserId
    public void setLoggedinuserId(int userId) {
        loggedinuserId = userId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use the static variable instead of defining a local variable
        appPreference = new AppPreference(this);

        appPreference.setLoginStatus(false);

        serviceApi = RetrofitClient.getRetrofitInstance().create(ServiceApi.class);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_check_accounts) {
                if (!(getCurrentFragment() instanceof ProfileFragment)) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                }
                return true;
            } else if (itemId == R.id.action_transfer) {
                Log.d("MainActivity", "Switching to TransferFragment");
                switchToTransferFragment();
                return true;
            } else if (itemId == R.id.action_atm_search) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AtmSearchFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.action_logout) {
                logout();
                return true;
            } else {
                return false;
            }
        });


        Log.d("MainActivity", "savedInstanceState before if statement : " + savedInstanceState);

        if (savedInstanceState == null) {
            Log.d("MainActivity", "Login status inside If statement: " + appPreference.getLoginStatus());

            if (appPreference.getLoginStatus()) {
                Log.d("MainActivity", "Replacing with ProfileFragment");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
            } else {

                Log.d("MainActivity", "Replacing with LoginFragment");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit();
            }
        }
    }


    public ServiceApi getServiceApi() {
        return serviceApi;
    }
    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    public void register() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new RegistrationFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void login(String authToken, String Email ) {
        appPreference.setauthToken(authToken);
        setEmail(email);
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("email", Email);
        profileFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, profileFragment) // Use the created instance
                .commit();

        profileFragment.updateProfileDetails();

        // Make bottomNavigationView visible after login
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void showLoginFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();

        // Hide bottomNavigationView after logout
        bottomNavigationView.setVisibility(View.GONE);
    }


    public int getLoggedinuserId() {
        return loggedinuserId;
    }



    // Add this method to set the email in ProfileFragment
    public void setEmailInProfileFragment(String email) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof ProfileFragment) {
            ProfileFragment profileFragment = (ProfileFragment) currentFragment;
            profileFragment.setEmail(email);
        } else {
            // Handle the case where the current fragment is not a ProfileFragment
            // You might want to log or display an error message
        }
    }


    private void switchToTransferFragment() {
        Log.d("MainActivity", "Switching to TransferFragment");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new TransferFragment())
                .addToBackStack(null)
                .commit();
    }


    // Add a method to get the email
    public String getEmail() {
        return email;
    }

    public void logout() {
        appPreference.setLoginStatus(false);
        appPreference.setDisplayName("Name");
        appPreference.setDisplayEmail("Email");
        appPreference.setCreDate("DATE");

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();

        // Hide bottomNavigationView after logout
        bottomNavigationView.setVisibility(View.GONE);
    }
}