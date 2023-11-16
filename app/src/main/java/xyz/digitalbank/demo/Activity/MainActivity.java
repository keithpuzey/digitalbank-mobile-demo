package xyz.digitalbank.demo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import xyz.digitalbank.demo.Constants.Constant;
import xyz.digitalbank.demo.Extras.AppPreference;
import xyz.digitalbank.demo.Fragments.LoginFragment;
import xyz.digitalbank.demo.Fragments.ProfileFragment;
import xyz.digitalbank.demo.Fragments.RegistrationFragment;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.MyInterface;
import xyz.digitalbank.demo.Services.RetrofitClient;
import xyz.digitalbank.demo.Services.ServiceApi;

public class MainActivity extends AppCompatActivity implements MyInterface {

    public static AppPreference appPreference;
    public static String c_date;

    FrameLayout container_layout;

    public static ServiceApi serviceApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container_layout = findViewById(R.id.fragment_container);
        appPreference = new AppPreference(this);

        //Log.e("created_at: ", c_date);

        serviceApi = RetrofitClient.getApiClient(Constant.baseUrl.BASE_URL).create(ServiceApi.class);

        if (container_layout != null){
            if (savedInstanceState != null){
                return;
            }

            //check login status from sharedPreference
            if (appPreference.getLoginStatus()){
                //when true
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragment_container, new ProfileFragment())
                        .commit();
            } else {
                // when get false
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragment_container, new LoginFragment())
                        .commit();
            }
        }

    } // ending onCreate


    // overridden from MyInterface
    @Override
    public void register() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new RegistrationFragment())
                .addToBackStack(null)
                .commit();
    }
    @Override
    //public void login(String name, String email, String created_at, String authToken ) {
    public void login(String authToken ) {
        //appPreference.setDisplayName(name);
        //appPreference.setDisplayEmail(email);
        //appPreference.setCreDate(created_at);
        appPreference.setauthToken(authToken);


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .commit();
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
    }
}

