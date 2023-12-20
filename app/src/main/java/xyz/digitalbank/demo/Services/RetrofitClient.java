package xyz.digitalbank.demo.Services;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.digitalbank.demo.Constants.Constant;
import xyz.digitalbank.demo.Model.DepositRequest;
import retrofit2.Callback;
import xyz.digitalbank.demo.Constants.ConstantsManager;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.util.Log;
public class RetrofitClient {


    private static Retrofit retrofit;

    private Context context;  // Declare a context variable

    private RetrofitClient() {
        // Private constructor to prevent instantiation
    }

    public static synchronized Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            String BASE_URL = ConstantsManager.getBaseUrl(context);
            Log.e("Login", "BASE URL before RetroFit is = " + BASE_URL);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ServiceApi getServiceApi(Context context) {
        return getRetrofitInstance(context).create(ServiceApi.class);
    }


    public static void transferFunds(int toAccountId, String authToken, String contentType, DepositRequest depositRequest, boolean isCredit, Callback<Void> callback,  Context context) {
        String action = isCredit ? "CREDIT" : "DEBIT";
        depositRequest.setTransactionTypeCode(isCredit ? "RFD" : "DBT");

        getServiceApi(context).transferFunds(toAccountId, action, authToken, contentType, depositRequest).enqueue(callback);

    }

}
