package com.example.medicineapp.function;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicineapp.Java.FirebaseServices;
import com.example.medicineapp.Mains.MainFragment;
import com.example.medicineapp.Mains.MainPageFragment;
import com.example.medicineapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnSignup;
    private FirebaseServices fbs;
    private TextView tvLoginFromSignup;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       /* return inflater.inflate(R.layout.fragment_sign_up, container, false);*/
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Connect the back arrow ImageView
        ImageView ivBackArrow = view.findViewById(R.id.ivBackArrowSignUp);
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to MainFragment
                navigateToMainFragment();
            }
        });

        // Rest of your existing code...
        return view;


    }

    @Override
    public void onStart(){
        super.onStart();
        //connecting components
        fbs = FirebaseServices.getInstance();
        etUsername = getView().findViewById(R.id.etUsernameSignup);
        etPassword = getView().findViewById(R.id.etPasswordSignup);
        btnSignup = getView().findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Data validation
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if (username.trim().isEmpty() && password.trim().isEmpty()) {
                    Toast.makeText(getActivity(), "Some fields are empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Signup procedure
                fbs.getAuth().createUserWithEmailAndPassword(username, password).addOnSuccessListener(
                        new OnSuccessListener<AuthResult>(){

                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(getActivity(), "Success!",Toast.LENGTH_SHORT).show();
                                gotoMainPageFragment(); // Navigate to the main menu
                            }
                        }

                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e){
                        Toast.makeText(getActivity(), "Failed!",Toast.LENGTH_SHORT).show();
                    }

                });



            }
        });

        tvLoginFromSignup = getView().findViewById(R.id.tvLoginFromSignup);
        tvLoginFromSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoLoginFragment();
            }

        });
    }

    private void gotoLoginFragment() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new LoginFragment());
        ft.commit();
        ft.addToBackStack(null);
    }

    private void gotoMainPageFragment() {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new MainPageFragment());
        ft.addToBackStack(null);
        ft.commit();
    }

    private void navigateToMainFragment() {
        // Create an instance of MainFragment
        MainFragment mainFragment = new MainFragment();

        // Perform the fragment transaction
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mainFragment) // Replace with your container ID
                .addToBackStack(null) // Optional: Add to back stack if you want users to return
                .commit();
    }

}