package xyz.digitalbank.demo.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class UserRequest {

    private String title;
    private String firstName;
    private String lastName;
    private String gender;
    private String dob;
    private String ssn;
    private String emailAddress;
    private String password;
    private String address;
    private String locality;
    private String region;
    private String postalCode;
    private String country;
    private String homePhone;
    private String mobilePhone;
    private String workPhone;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setGender(String gender) {  this.gender = gender;  }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setzipcode(String postalCode) {   this.postalCode = postalCode;   }

    public void setCountry(String country) {
        this.country = country;
    }


    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }





    // Convert UserRequest object to JSON string
    public String toJsonString() {
        JSONObject json = new JSONObject();
        try {
            json.put("title", title);
            json.put("firstName", firstName);
            json.put("lastName", lastName);
            json.put("gender", gender);
            json.put("dob", dob);
            json.put("ssn", ssn);
            json.put("emailAddress", emailAddress);
            json.put("password", password);
            json.put("address", address);
            json.put("locality", locality);
            json.put("region", region);
            json.put("postalCode", postalCode);
            json.put("country", country);
            json.put("homePhone", homePhone);
            json.put("mobilePhone", mobilePhone);
            json.put("workPhone", workPhone);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
