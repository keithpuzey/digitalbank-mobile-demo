package xyz.digitalbank.demo.Services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.digitalbank.demo.Constants.Constant;

public class RetrofitClient {

    private static final String BASE_URL = "http://dbankdemo.com/";
    private static Retrofit retrofit;

    private RetrofitClient() {
        // Private constructor to prevent instantiation
    }

    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ServiceApi getServiceApi() {
        return getRetrofitInstance().create(ServiceApi.class);
    }
}
