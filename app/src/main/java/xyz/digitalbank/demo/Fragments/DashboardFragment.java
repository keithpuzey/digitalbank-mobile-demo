package xyz.digitalbank.demo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Constants.Constant;

public class DashboardFragment extends Fragment {

    private PopupWindow popupWindow;
    private TextView logoutLinkTextView;

    private ProgressBar progressBar;
    private Context context;
    private WebView webView;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.your_dashboard_layout, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        context = getContext();

        // ✅ Setup WebView
        webView = view.findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JS if webpage needs it
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE); // hide loading spinner
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl( DEFAULT_BASE_URL + "/financedashboard.html");

        // ✅ Setup Toolbar
        Toolbar toolbar = view.findViewById(R.id.action_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeAsUpIndicator(null);

        ImageView toolbarImage = view.findViewById(R.id.toolbar_image);
        toolbarImage.setOnClickListener(v -> showPopupMenu(toolbarImage));

        // ✅ Dismiss popup if user taps outside
        View rootLayout = view.findViewById(R.id.dashboard_root_layout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
                return true;
            }
            return false;
        });

        return view;
    }

    private void showPopupMenu(View anchorView) {
        if (popupWindow == null) {
            View popupView = getLayoutInflater().inflate(R.layout.popup_user_info, null);

            logoutLinkTextView = popupView.findViewById(R.id.link_logout);
            logoutLinkTextView.setOnClickListener(v -> {
                MainActivity mainActivity = (MainActivity) requireActivity();
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                mainActivity.logout();
            });

            popupWindow = new PopupWindow(
                    popupView,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    true
            );
        }
        popupWindow.showAsDropDown(anchorView);
    }
}