package xyz.digitalbank.demo.Services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import xyz.digitalbank.demo.Model.User;

public interface ServiceApi {

    @GET("register.php")
    Call<User> doRegistration(@Query("name") String name, @Query("email") String email, @Query("phone") String phone, @Query("password") String password);

    @POST("bank/api/v1/auth")
    Call<User> doLogin(@Query("username") String email, @Query("password") String password);

}
