package xyz.digitalbank.demo.Services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Body;
import retrofit2.http.Header;
import xyz.digitalbank.demo.Model.User;
import com.google.gson.JsonObject;
import xyz.digitalbank.demo.Model.UserRequest;

public interface ServiceApi {

    @GET("register.php")
    Call<User> doRegistration(@Query("name") String name, @Query("email") String email, @Query("phone") String phone, @Query("password") String password);

    @POST("bank/api/v1/auth")
    Call<User> doLogin(@Query("username") String email, @Query("password") String password);

    @POST("bank/api/v1/auth")
    Call<JsonObject> authenticateUser(@Query("username") String username, @Query("password") String adminpassword);


    @POST("/bank/api/v1/user")
    Call<Void> registerUser(
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType,
            @Query("role") String ROLE,
            @Body UserRequest userRequest
    );
}