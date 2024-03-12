
package xyz.digitalbank.demo.Constants;

import android.content.Context;
import android.content.SharedPreferences;

public class ConstantsManager {

    public static final String SHARED_PREF_NAME = "app_constants";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_MOCK_URL = "mock_url";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getBaseUrl(Context context) {
        return getSharedPreferences(context).getString(KEY_BASE_URL, "http://dbankdemo.com/bank/");
    }

    public static void setBaseUrl(Context context, String baseUrl) {
        getSharedPreferences(context).edit().putString(KEY_BASE_URL, baseUrl).apply();
    }

    public static String getMockUrl(Context context) {
        return getSharedPreferences(context).getString(KEY_MOCK_URL, "http://dbmobile322871.mock.blazemeter.com/");
    }


    public static void setMockUrl(Context context, String mockUrl) {
        getSharedPreferences(context).edit().putString(KEY_MOCK_URL, mockUrl).apply();
    }
}
