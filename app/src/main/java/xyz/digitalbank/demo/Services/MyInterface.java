package xyz.digitalbank.demo.Services;

public interface MyInterface {

    // Get User Data
    // void getUserData();

    // for login
    void register();
    void login( String authToken );
    void logout();
}
