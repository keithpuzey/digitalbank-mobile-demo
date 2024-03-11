package xyz.digitalbank.demo.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
public class ConstantsManager {

    private static final String SHARED_PREF_NAME = "app_constants";

    // Constants keys
    private static final String BASE_URL = "http://dbankdemo.com/";
    private static final String MOCK_URL = "http://dbmobile322871.mock.blazemeter.com/";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getBaseUrl(Context context) {
        return getSharedPreferences(context).getString(BASE_URL, "http://dbankdemo.com/");
    }

    public static void setBaseUrl(Context context, String baseUrl) {
        getSharedPreferences(context).edit().putString(BASE_URL, baseUrl).apply();
    }

    public static String getMockUrl(Context context) {
        return getSharedPreferences(context).getString(MOCK_URL, "http://dbmobile322871.mock.blazemeter.com/");
    }

    public static void setMockUrl(Context context, String mockUrl) {
        getSharedPreferences(context).edit().putString(MOCK_URL, mockUrl).apply();
      }
}
