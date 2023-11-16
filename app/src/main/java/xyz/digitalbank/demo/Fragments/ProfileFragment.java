package xyz.digitalbank.demo.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import androidx.annotation.NonNull;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private TextView name, email;
    private Button logoutBtn;

    MyInterface logoutListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle clicks on the icons here
                switch (item.getItemId()) {
                    case R.id.action_check_accounts:
                        // Handle check accounts icon click
                        return true;
                    case R.id.action_transfer:
                        // Handle transfer icon click
                        return true;
                    case R.id.action_atm_search:
                        // Handle ATM search icon click
                        return true;
                    case R.id.action_contact:
                        // Handle contact icon click
                        return true;
                }
                return false;
            }
        });

        name = view.findViewById(R.id.name);
        // Keep only the greeting message
        String Name = "Logged into Digital Bank as user " + MainActivity.appPreference.getDisplayName();
        name.setText(Name);

        email = view.findViewById(R.id.email);

        // Display only the authentication token
        String authToken = MainActivity.appPreference.getauthToken();
        email.setText("Authentication Token: " + authToken);

        logoutBtn = view.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutListener.logout();
            }
        });

        return view;
    }

 // ending onCreateView

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        logoutListener = (MyInterface) activity;

    }
}
