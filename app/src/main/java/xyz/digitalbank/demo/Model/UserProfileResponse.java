package xyz.digitalbank.demo.Model;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("title")
    private String title;

    @SerializedName("gender")
    private String gender;

    @SerializedName("ssn")
    private String ssn;

    @SerializedName("dob")
    private String dob;

    @SerializedName("dom")
    private String dom;

    @SerializedName("emailAddress")
    private String emailAddress;

    @SerializedName("homePhone")
    private String homePhone;

    @SerializedName("mobilePhone")
    private String mobilePhone;

    @SerializedName("workPhone")
    private String workPhone;

    @SerializedName("address")
    private String address;

    @SerializedName("locality")
    private String locality;

    @SerializedName("region")
    private String region;

    @SerializedName("postalCode")
    private String postalCode;

    @SerializedName("country")
    private String country;

    // Add getters for all fields

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTitle() {
        return title;
    }

    public String getGender() {
        return gender;
    }

    public String getSsn() {
        return ssn;
    }

    public String getDob() {
        return dob;
    }

    public String getDom() {
        return dom;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public String getAddress() {
        return address;
    }

    public String getLocality() {
        return locality;
    }

    public String getRegion() {
        return region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }
}

