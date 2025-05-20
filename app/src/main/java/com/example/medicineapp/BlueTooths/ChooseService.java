package com.example.medicineapp.BlueTooths;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.medicineapp.Mains.MainPageFragment;
import com.example.medicineapp.R;
import com.example.medicineapp.function.LoginFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChooseService#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChooseService extends Fragment {

    private Button btnBPM;
    private Button btnTEMP;

    public ChooseService() {
        // Required empty public constructor
    }

    public static ChooseService newInstance(String param1, String param2) {
        ChooseService fragment = new ChooseService();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_service, container, false);

        // Initialize views
        btnBPM = view.findViewById(R.id.btnBPM);
        btnTEMP = view.findViewById(R.id.btnTEMP);

        ImageView ivBackArrow = view.findViewById(R.id.ivBackArrowBT);
        ivBackArrow.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MainPageFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Set button listeners
        btnBPM.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), BTheartbeats.class);
            startActivity(intent);
        });

        btnTEMP.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), BTtemp.class);
            startActivity(intent);
        });

        return view;
    }
}