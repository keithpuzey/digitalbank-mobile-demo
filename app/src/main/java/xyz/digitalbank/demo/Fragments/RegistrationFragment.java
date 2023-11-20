package xyz.digitalbank.demo.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.digitalbank.demo.Activity.MainActivity;
import xyz.digitalbank.demo.Model.User;
import xyz.digitalbank.demo.R;
import xyz.digitalbank.demo.Services.ServiceApi;

import java.util.Calendar;

public class RegistrationFragment extends Fragment {

    private EditText nameInput, emailInput, phoneInput, passwordInput;
    private Spinner titleSpinner;
    private EditText dobInput, ssnInput, addressInput, cityInput, zipCodeInput;
    private Button regBtn, cancelBtn;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        nameInput = view.findViewById(R.id.nameInput);
        emailInput = view.findViewById(R.id.emailInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        titleSpinner = view.findViewById(R.id.titleSpinner);
        dobInput = view.findViewById(R.id.dobInput);
        ssnInput = view.findViewById(R.id.ssnInput);
        addressInput = view.findViewById(R.id.addressInput);
        cityInput = view.findViewById(R.id.cityInput);
        zipCodeInput = view.findViewById(R.id.zipCodeInput);
        regBtn = view.findViewById(R.id.regBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);

        // Populate title dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireActivity(), R.array.title_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        titleSpinner.setAdapter(adapter);

        // Set up the date picker when the dobInput is clicked
        dobInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
                Log.e("reg button", "clicked");
            }
        });

        // Set onClickListener for cancel button
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement cancel functionality here
                Log.e("cancel button", "clicked");
            }
        });

        return view;
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Handle the selected date
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        dobInput.setText(selectedDate);
                    }
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void registerUser() {
        String name = nameInput.getText().toString();
        String email = emailInput.getText().toString();
        String phone = phoneInput.getText().toString();
        String password = passwordInput.getText().toString();
        String title = titleSpinner.getSelectedItem().toString();
        String dob = dobInput.getText().toString();
        String ssn = ssnInput.getText().toString();
        String address = addressInput.getText().toString();
        String city = cityInput.getText().toString();
        String zipCode = zipCodeInput.getText().toString();

        // Rest of your registration logic...
    }
}
