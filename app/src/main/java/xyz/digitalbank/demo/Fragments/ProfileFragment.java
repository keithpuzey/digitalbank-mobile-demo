package xyz.digitalbank.demo.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.Activity.atm_search;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;

public class ProfileFragment extends Fragment {
    private TextView name, email;
    private MyInterface logoutListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // Handle clicks on the icons here
            switch (item.getItemId()) {
                case R.id.action_check_accounts:
                    // Switch to the ProfileFragment
                    switchToProfileFragment();
                    return true;
                case R.id.action_transfer:
                    // Handle transfer icon click
                    return true;
                case R.id.action_atm_search:
                    startActivity(new Intent(getActivity(), atm_search.class));
                    return true;
                case R.id.action_logout:
                    if (logoutListener != null) {
                        logoutListener.logout();
                    }
                    return true;
                default:
                    return false;
            }
        });

        name = view.findViewById(R.id.name);
        // Keep only the greeting message
        String displayName = MainActivity.appPreference.getDisplayName();
        String greetingMessage = "Logged into Digital Bank as user " + displayName;
        name.setText(greetingMessage);

        email = view.findViewById(R.id.email);
        // Display only the authentication token
        String authToken = MainActivity.appPreference.getauthToken();
        String authTokenMessage = "Authentication Token: " + authToken;
        email.setText(authTokenMessage);

        return view;
    }

    private void switchToProfileFragment() {
        // Log to check if this method is being called
        Log.d("ProfileFragment", "Switching to Check Accounts");

        // Check if the current fragment is not already MainActivity
        if (!(getActivity() instanceof MainActivity)) {
            // Switch to MainActivity
            MainActivity.appPreference.showToast("Switching to Check Accounts");
            MainActivity.appPreference.setLoginStatus(true);

            // Create a new Intent
            Intent intent = new Intent(getActivity(), MainActivity.class);

            // Clear the back stack to prevent returning to the login screen
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Start the MainActivity
            startActivity(intent);

            // Optional: finish the current activity if needed
            requireActivity().finish();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        logoutListener = (MyInterface) activity;

        // Log to check if onAttach is being called
        Log.d("ProfileFragment", "onAttach");
    }
}
