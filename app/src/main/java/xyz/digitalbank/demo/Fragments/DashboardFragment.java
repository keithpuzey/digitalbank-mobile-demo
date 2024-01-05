package xyz.digitalbank.demo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.Constants.ConstantsManager;
import xyz.digitalbank.demo.Model.AccountInfo;
import xyz.digitalbank.demo.Model.UserAccountResponse;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.RetrofitClient;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.core.cartesian.series.Bar;
import com.anychart.AnyChart;
import android.widget.ProgressBar;



public class DashboardFragment extends Fragment {

    private List<UserAccountResponse> userAccounts = new ArrayList<>();
    private ArrayAdapter<AccountInfo> adapter;
    private List<AccountInfo> accountInfoList = new ArrayList<>();

    private String authToken;
    private int loggedinuserId;

    private ProgressBar progressBar;

    private AnyChartView anyChartView;

    private Spinner accountSpinner;
    private int selectedAccountId;

    private Context context;  // Declare a context variable

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.your_dashboard_layout, container, false);

        anyChartView = view.findViewById(R.id.Any_chart_view);
        progressBar = view.findViewById(R.id.progressBar);
        context = getContext();

        // Retrieve authToken from SharedPreferences
        authToken = MainActivity.appPreference.getauthToken();
        Log.e("DashBoard", "authToken is  = " + authToken);
        // Check if the user is logged in
        if (MainActivity.appPreference.getLoginStatus()) {
            // User is logged in, retrieve other information
            loggedinuserId = MainActivity.appPreference.getLoggedinuserId();
            Log.e("DashBoard", "Logged in User ID   = " + loggedinuserId);

            String email = MainActivity.appPreference.getDisplayEmail();

            String BASE_URL = ConstantsManager.getBaseUrl(requireContext());
            Log.e("Login", "BASE URL is = " + BASE_URL);
            //    FrameLayout transferContainer = view.findViewById(R.id.transferContainer);



            //    View transferScreen = inflater.inflate(R.layout.your_dashboard_layout, transferContainer, false);

            // Call the method to get user accounts and update the chart
            getUserAccounts(authToken, loggedinuserId);

        } else {
            // User is not logged in, handle accordingly
            // For example, redirect to the login screen
            // You might want to show a login screen or handle the scenario appropriately
            Log.d("DashboardFragment", "User is not logged in. Redirect to login screen.");
        }

        return view;
    }



    private void getUserAccounts(String authToken, int loggedinuserId) {
        // Call the API to get user accounts using the obtained user ID
        RetrofitClient.getServiceApi(context).getUserAccounts(loggedinuserId, authToken)
                .enqueue(new Callback<List<UserAccountResponse>>() {
                    @Override
                    public void onResponse(Call<List<UserAccountResponse>> call, Response<List<UserAccountResponse>> response) {
                        if (response.isSuccessful()) {
                            // Process the list of UserAccountResponse
                            List<UserAccountResponse> userAccounts = response.body();
                            displayUserAccounts(userAccounts, accountSpinner);
                        } else {
                            // Handle the case where the API call was not successful
                            // MainActivity.appPreference.showToast("Failed to retrieve user accounts");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<UserAccountResponse>> call, Throwable t) {
                        // Handle failure of getUserAccounts API
                        MainActivity.appPreference.showToast("API call failed: " + t.getMessage());
                    }
                });
    }

    private void displayUserAccounts(List<UserAccountResponse> userAccounts, Spinner accountSpinner) {
        // Clear existing data
        accountInfoList.clear();

        for (UserAccountResponse account : userAccounts) {
            int accountId = account.getId();
            String accountName = account.getName();
            String currentBalanceStr = String.valueOf(account.getCurrentBalance());
            double currentBalance = Double.parseDouble(currentBalanceStr);

            // Instantiate AccountInfo and add it to the list
            AccountInfo accountInfo = new AccountInfo(accountId, accountName, currentBalance, "SomeDefaultValue");
            accountInfoList.add(accountInfo);
        }

        // Log the number of accounts for debugging
        Log.d("TransferFragment", "Number of accounts: " + accountInfoList.size());

        // Create an ArrayAdapter with AccountInfo objects
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, accountInfoList);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateChart();
            }
        });

        // Log the exit point of the method
        Log.d("DashBoard", "Exiting DashBoard Graph");
    }

    private void updateChart() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Create a Cartesian chart
                Cartesian cartesian = AnyChart.bar();

                // Show the ProgressBar
                progressBar.setVisibility(View.VISIBLE);

                // Create data entries for the chart
                List<DataEntry> data = new ArrayList<>();
                for (AccountInfo accountInfo : accountInfoList) {
                    data.add(new ValueDataEntry(accountInfo.getAccountName(), accountInfo.getCurrentBalance()));
                }
                // Hide the ProgressBar
                progressBar.setVisibility(View.GONE);

                // Check if anyChartView is not null before setting the chart
                if (anyChartView != null) {
                    // Add the data entries to the chart
                    Bar bar = cartesian.bar(data);
                    // Customize the tooltip to show the values on top of each column
                    bar.tooltip().format("{%Value}");
                    anyChartView.setChart(cartesian);
                    anyChartView.setVisibility(View.VISIBLE);
                    Log.d("DashboardFragment", "AnyChartView visibility after setting chart: " + anyChartView.getVisibility());
                } else {
                    Log.e("DashboardFragment", "anyChartView is null. Cannot set the chart.");
                }

            }
        });
    }


}