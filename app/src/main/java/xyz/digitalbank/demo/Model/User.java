package xyz.digitalbank.demo.Model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("response")
    private String response;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("authToken")
    private String authToken;

    @SerializedName("phone")
    private String phone;

    @SerializedName("created_at")
    private String created_at;

    public String getResponse() {
        return response;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getauthToken() { return authToken;  }

    public String getCreated_at() {
        return created_at;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "authToken='" + authToken + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}
