
package xyz.digitalbank.demo.Constants;

import android.content.Context;
import android.content.SharedPreferences;

import xyz.digitalbank.demo.Fragments.DashboardFragment;

public class ConstantsManager {

    public static final String SHARED_PREF_NAME = "app_constants";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_MOCK_URL = "mock_url";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getBaseUrl(Context context) {
        String url = getSharedPreferences(context).getString(KEY_BASE_URL, "http://dbankdemo.com/bank/");
        if (url == null) url = "http://dbankdemo.com/bank/";
        // Ensure it ends with /
        if (!url.endsWith("/")) url += "/";
        return url;
    }

    public static void setBaseUrl(Context context, String baseUrl) {
        if (baseUrl == null) return;
        // Ensure trailing slash
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        getSharedPreferences(context).edit().putString(KEY_BASE_URL, baseUrl).apply();
    }

    // Derived homeUrl: remove /bank and trailing slash
    public static String getHomeUrl(Context context) {
        String url = getBaseUrl(context);
        // remove trailing slash
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        // remove /bank
        if (url.endsWith("/bank")) url = url.substring(0, url.length() - "/bank".length());
        return url;
    }


    public static String getMockUrl(Context context) {
        return getSharedPreferences(context).getString(KEY_MOCK_URL, "http://dbmobile322871.mock.blazemeter.com/");
    }


    public static void setMockUrl(Context context, String mockUrl) {
        getSharedPreferences(context).edit().putString(KEY_MOCK_URL, mockUrl).apply();
    }


}
