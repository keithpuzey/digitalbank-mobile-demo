package xyz.digitalbank.demo.Model;

import com.google.gson.annotations.SerializedName;
import org.json.JSONException;
import org.json.JSONObject;

public class User {

    @SerializedName("response")
    private String response;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("authToken")
    private String authToken; // This is the authToken variable

    public String getAuthToken() {
        return authToken;
    }
    @SerializedName("phone")
    private String phone;

    private String passwordInput;
    private String title;
    private String dob;
    private String ssn;
    private String address;
    private String city;
    private String zipCode;
    private String Role;

    @SerializedName("created_at")
    private String created_at;

    // Constructor, getters, setters, and other methods go here...

    // toString() method goes here...

}

// Move the inner User class outside of the outer User class


