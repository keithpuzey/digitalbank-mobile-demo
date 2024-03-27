package xyz.digitalbank.demo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Bar;

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

public class DashboardFragment extends Fragment {

    private List<UserAccountResponse> userAccounts = new ArrayList<>();
    private ArrayAdapter<AccountInfo> adapter;
    private List<AccountInfo> accountInfoList = new ArrayList<>();

    private String authToken;
    private int loggedinuserId;

    // Declare PopupWindow and its components
    private PopupWindow popupWindow;
    private TextView userNameTextView;
    private TextView logoutLinkTextView;

    private ProgressBar progressBar;
    private Context context;

    private AnyChartView anyChartView;
    private Spinner accountSpinner;
    private int selectedAccountId;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.your_dashboard_layout, container, false);

        anyChartView = view.findViewById(R.id.Any_chart_view);
        progressBar = view.findViewById(R.id.progressBar);
        context = getContext();

        Toolbar toolbar = view.findViewById(R.id.action_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeAsUpIndicator(null);

        ImageView toolbarImage = view.findViewById(R.id.toolbar_image);
        toolbarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(toolbarImage);
            }
        });

        View rootLayout = view.findViewById(R.id.dashboard_root_layout);
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    return true; // Consume the touch event to prevent it from propagating further
                }
                return false; // Allow the touch event to propagate if the popup menu is not showing
            }
        });

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
            Log.e("DashBoard", "BASE URL is = " + BASE_URL);
            // Call the method to get user accounts and update the chart
            getUserAccounts(authToken, loggedinuserId);
        } else {
            // User is not logged in, handle accordingly
            // Log.d("DashboardFragment", "User is not logged in. Redirect to login screen.");
        }

        return view;
    }

    private void showPopupMenu(View anchorView) {
        if (popupWindow == null) {
            View popupView = getLayoutInflater().inflate(R.layout.popup_user_info, null);

            logoutLinkTextView = popupView.findViewById(R.id.link_logout);
            logoutLinkTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                    mainActivity.logout();
                }
            });

            popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        }

        popupWindow.showAsDropDown(anchorView);
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
            int accountNumber = account.getAccountNumber();

            // Instantiate AccountInfo and add it to the list
            AccountInfo accountInfo = new AccountInfo(accountId, accountName, currentBalance, "SomeDefaultValue", accountNumber);
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
                } else {
                }
            }
        });
    }
}
