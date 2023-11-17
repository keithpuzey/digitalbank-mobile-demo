package xyz.digitalbank.demo.Services;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://dbankdemo.com/";
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
