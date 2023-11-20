package xyz.digitalbank.demo.Services;
import xyz.digitalbank.demo.Constants.Constant;


import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = Constant.baseUrl.BASE_URL;
    public static Retrofit getRetrofitInstance() {
        // Create and configure your Retrofit instance here
        Retrofit retrofit = new Retrofit.Builder()
                // Set base URL, converters, etc.
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
}
