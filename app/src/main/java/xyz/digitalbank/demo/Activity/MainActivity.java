package xyz.digitalbank.demo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import xyz.digitalbank.demo.Extras.AppPreference;
import xyz.digitalbank.demo.Fragments.DashboardFragment;
import xyz.digitalbank.demo.Fragments.LoginFragment;
import xyz.digitalbank.demo.Fragments.ProfileFragment;
import xyz.digitalbank.demo.Fragments.RegistrationFragment;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import xyz.digitalbank.demo.Services.ServiceApi;
import xyz.digitalbank.demo.Services.RetrofitClient;
import xyz.digitalbank.demo.Fragments.AtmSearchFragment;
import xyz.digitalbank.demo.Fragments.TransferFragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements MyInterface {

    private BottomNavigationView bottomNavigationView;
    public static AppPreference appPreference;
    private ServiceApi serviceApi;

    private String email;
    private int loggedinuserId;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        appPreference = new AppPreference(this);
        appPreference.setLoginStatus(false);

        serviceApi = RetrofitClient.getRetrofitInstance(this).create(ServiceApi.class);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setBottomNavGravity();

        // Bottom navigation listener
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            handleMenuItem(item.getItemId());
            return true;
        });

        // Show initial fragment
        if (savedInstanceState == null) {
            if (appPreference.getLoginStatus()) {
                switchFragmentWithAnimation(new ProfileFragment());
            } else {
                switchFragmentWithAnimation(new LoginFragment());
            }
        }
    }

    private void setBottomNavGravity() {
        ViewGroup.LayoutParams layoutParams = bottomNavigationView.getLayoutParams();
        if (layoutParams instanceof CoordinatorLayout.LayoutParams) {
            ((CoordinatorLayout.LayoutParams) layoutParams).gravity = Gravity.BOTTOM;
        } else if (layoutParams instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) layoutParams).gravity = Gravity.BOTTOM;
        }
    }

    // Shared handler for bottom nav items
    private void handleMenuItem(int itemId) {
        if (itemId == R.id.action_check_accounts) {
            if (!(getCurrentFragment() instanceof ProfileFragment)) {
                switchFragmentWithAnimation(new ProfileFragment());
            }
        } else if (itemId == R.id.action_dashboard) {
            switchToDashboardFragment();
        } else if (itemId == R.id.action_transfer) {
            switchToTransferFragment();
        } else if (itemId == R.id.action_atm_search) {
            switchFragmentWithAnimation(new AtmSearchFragment());
        } else if (itemId == R.id.action_logout) {
            logout();
        }
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void switchToTransferFragment() {
        switchFragmentWithAnimation(new TransferFragment());
    }

    private void switchToDashboardFragment() {
        switchFragmentWithAnimation(new DashboardFragment());
    }

    // Fragment transaction with simple slide animation
    private void switchFragmentWithAnimation(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void register() {
        switchFragmentWithAnimation(new RegistrationFragment());
    }

    @Override
    public void login(String authToken, String Email) {
        appPreference.setauthToken(authToken);
        email = Email;

        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("email", Email);
        profileFragment.setArguments(bundle);

        switchFragmentWithAnimation(profileFragment);
        profileFragment.updateProfileDetails();

        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void showLoginFragment() {
        switchFragmentWithAnimation(new LoginFragment());
        bottomNavigationView.setVisibility(View.GONE);
    }

    public int getLoggedinuserId() {
        return loggedinuserId;
    }

    public void setEmailInProfileFragment(String email) {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof ProfileFragment) {
            ((ProfileFragment) currentFragment).setEmail(email);
        }
    }

    public String getEmail() {
        return email;
    }

    public void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Perform actual logout
                    appPreference.setLoginStatus(false);
                    appPreference.setDisplayName("Name");
                    appPreference.setDisplayEmail("Email");
                    appPreference.setCreDate("DATE");

                    // Clear the back stack
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    // Switch to LoginFragment with no animation
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new LoginFragment())
                            .commit();

                    bottomNavigationView.setVisibility(View.GONE);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public ServiceApi getServiceApi() {
        return serviceApi;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLoggedinuserId(int userId) {
        loggedinuserId = userId;
    }
}