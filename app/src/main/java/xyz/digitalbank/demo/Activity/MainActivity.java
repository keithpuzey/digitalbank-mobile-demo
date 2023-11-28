package xyz.digitalbank.demo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import xyz.digitalbank.demo.Extras.AppPreference;
import xyz.digitalbank.demo.Fragments.LoginFragment;
import xyz.digitalbank.demo.Fragments.ProfileFragment;
import xyz.digitalbank.demo.Fragments.RegistrationFragment;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;
import android.view.View;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import xyz.digitalbank.demo.Services.ServiceApi;
import xyz.digitalbank.demo.Services.RetrofitClient;
import xyz.digitalbank.demo.Fragments.AtmSearchFragment;


public class MainActivity extends AppCompatActivity implements MyInterface {

    private BottomNavigationView bottomNavigationView;
    public static AppPreference appPreference;
    private ServiceApi serviceApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appPreference = new AppPreference(this);
        serviceApi = RetrofitClient.getRetrofitInstance().create(ServiceApi.class);


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_check_accounts:
                    if (!(getCurrentFragment() instanceof ProfileFragment)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ProfileFragment())
                                .commit();
                    }
                    return true;
                case R.id.action_transfer:
                    // Handle transfer icon click
                    return true;
                case R.id.action_atm_search:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new AtmSearchFragment())
                            .commit();
                    return true;

                case R.id.action_logout:
                    logout();
                    return true;
                default:
                    return false;
            }
        });

        if (appPreference.getLoginStatus()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
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
    public void login(String authToken) {
        appPreference.setauthToken(authToken);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .commit();

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

    @Override
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