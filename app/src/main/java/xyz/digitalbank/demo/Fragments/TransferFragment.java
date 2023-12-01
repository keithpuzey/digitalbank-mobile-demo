package xyz.digitalbank.demo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import android.widget.FrameLayout;
import xyz.digitalbank.demo.R;

public class TransferFragment extends Fragment {

    public TransferFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);

        // Replace the contents of transferContainer with your transfer screen UI
        FrameLayout transferContainer = view.findViewById(R.id.transferContainer);
        // Inflate your transfer screen layout and add it to transferContainer

        return view;
    }
}
