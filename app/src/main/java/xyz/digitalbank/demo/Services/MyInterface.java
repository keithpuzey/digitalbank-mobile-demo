package xyz.digitalbank.demo.Services;

public interface MyInterface {


    // for login
    void register();
    void login( String authToken, String Email  );
    void logout();
}
