package xyz.digitalbank.demo.Extras;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import xyz.digitalbank.demo.R;

// Shared Preference METHODS

public class AppPreference {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final String KEY_AUTH_TOKEN = "auth_Token";
    private static final String DEFAULT_AUTH_TOKEN = "";



    public AppPreference(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(String.valueOf(R.string.s_pref_file), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    //Setting login status
    public void setLoginStatus(boolean status){
        // Using a default value of false if the status is not provided
        boolean defaultStatus = true;
        editor.putBoolean(String.valueOf(R.string.s_pref_login_status), status);
        editor.commit();
    }
    public boolean getLoginStatus(){
        return sharedPreferences.getBoolean(String.valueOf(R.string.s_pref_login_status), true);
    }

    // For Name
    public void setDisplayName(String name){
        editor.putString(String.valueOf(R.string.s_pref_name), name);
        editor.commit();
    }
    public String getDisplayName(){
        return sharedPreferences.getString(String.valueOf(R.string.s_pref_name), "Name");
    }

    //For email
    public void setDisplayEmail(String email){
        editor.putString(String.valueOf(R.string.s_pref_email), email);
        editor.commit();
    }
    public String getDisplayEmail(){
        return sharedPreferences.getString(String.valueOf(R.string.s_pref_email), "email");
    }

    //For email
    public void setCreDate(String date){
        editor.putString(String.valueOf(R.string.s_pref_date), date);
        editor.commit();
    }
    public void setauthToken(String authToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.apply();
    }
     public String getCreDate(){
        return sharedPreferences.getString(String.valueOf(R.string.s_pref_date), "date");
    }
    public String getauthToken() {
        // Retrieve the authToken from SharedPreferences
        return sharedPreferences.getString(KEY_AUTH_TOKEN, DEFAULT_AUTH_TOKEN);
    }

    // For TOAST Message for response
    public void showToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}