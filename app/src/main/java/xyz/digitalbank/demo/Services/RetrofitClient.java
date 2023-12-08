package xyz.digitalbank.demo.Services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.digitalbank.demo.Constants.Constant;
import xyz.digitalbank.demo.Model.DepositRequest;
import retrofit2.Callback;


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

 //   public static ServiceApi getServiceApi() {
 //       if (serviceApi == null) {
 //           Retrofit retrofit = new Retrofit.Builder()
 //                   .baseUrl(BASE_URL)
 //                   .addConverterFactory(GsonConverterFactory.create())
 //                   .build();

  //          serviceApi = retrofit.create(ServiceApi.class);
  //      }
  //      return serviceApi;
  //  }

    public static void transferFunds(int toAccountId, String authToken, String contentType, DepositRequest depositRequest, boolean isCredit, Callback<Void> callback) {
        String action = isCredit ? "CREDIT" : "DEBIT";
        depositRequest.setTransactionTypeCode(isCredit ? "RFD" : "DBT");

        getServiceApi().transferFunds( toAccountId, action, authToken, contentType, depositRequest).enqueue(callback);
    }

}
