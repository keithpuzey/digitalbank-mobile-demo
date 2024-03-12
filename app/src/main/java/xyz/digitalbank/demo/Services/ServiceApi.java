package xyz.digitalbank.demo.Services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Body;
import retrofit2.http.Header;
import xyz.digitalbank.demo.Model.User;
import com.google.gson.JsonObject;

import xyz.digitalbank.demo.Model.UserProfileResponse;
import xyz.digitalbank.demo.Model.UserRequest;
import xyz.digitalbank.demo.Model.UserResponse;
import xyz.digitalbank.demo.Model.UserAccountResponse;
import retrofit2.http.Path;
import java.util.List;
import xyz.digitalbank.demo.Model.TransactionResponse;
import xyz.digitalbank.demo.Model.DepositRequest;

public interface ServiceApi {

    @GET("register.php")
    Call<User> doRegistration(@Query("name") String name, @Query("email") String email, @Query("phone") String phone, @Query("password") String password);

    @POST("api/v1/auth")
    Call<User> doLogin(@Query("username") String email, @Query("password") String password);

    @POST("api/v1/auth")
    Call<JsonObject> authenticateUser(@Query("username") String username, @Query("password") String adminpassword);

    @POST("api/v1/user")
    Call<Void> registerUser(
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType,
            @Query("role") String ROLE,
            @Body UserRequest userRequest
    );

    @POST("api/v1/account/{toAccountId}/transaction")
    Call<Void> transferFunds(
            @Path("toAccountId") int toAccountId,
            @Query("action") String action,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType,
            @Body DepositRequest depositRequest
    );


    @GET("api/v1/user/find")
    Call<UserResponse> findUserId(
            @Header("Authorization") String authToken,
            @Query("username") String email
    );

    @POST("api/v1/user/{id}/data/create")
    Call<Void> createData(
            @Path("id") int userId,
            @Header("Authorization") String authToken
    );

    @GET("api/v1/user/{id}/profile")
    Call<UserProfileResponse> getUserProfile(
            @Path("id") int loggedinuserId,
            @Header("Authorization") String authToken
        );

    @GET("api/v1/user/{id}/account")
    Call<List<UserAccountResponse>> getUserAccounts(
            @Path("id") int loggedinuserId,
            @Header("Authorization") String authToken
    );

    @GET("api/v1/account/{accountid}/transaction")
    Call<List<TransactionResponse>> getAccountTransactions(
            @Path("accountid") int accountId,
            @Header("Authorization") String authToken
    );
}