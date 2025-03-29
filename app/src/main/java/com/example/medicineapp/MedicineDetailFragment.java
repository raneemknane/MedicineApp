package com.example.medicineapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MedicineDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicineDetailFragment extends Fragment {

    private Medicine medicine;



    public MedicineDetailFragment() {
        // Required empty public constructor
    }


    /**
     * Factory method to create a new instance of MedicineDetailFragment.
     *
     * @param medicine The Medicine object to display in the fragment.
     * @return A new instance of MedicineDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MedicineDetailFragment newInstance( Medicine medicine) {
        MedicineDetailFragment fragment = new MedicineDetailFragment();
        Bundle args = new Bundle();

        // Pass the Medicine object using Serializable
        args.putSerializable("medicine", medicine);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            medicine = (Medicine) getArguments().getSerializable("medicine");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_medicine_detail, container, false);
        View view = inflater.inflate(R.layout.fragment_medicine_detail, container, false);
        // Initialize views
        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvExpiration = view.findViewById(R.id.tvDetailExpiration);
        TextView tvMealTime = view.findViewById(R.id.tvDetailMealTime);
        TextView tvQuantity = view.findViewById(R.id.tvDetailQuantity);
        ImageView ivPhoto = view.findViewById(R.id.ivDetailPhoto);

        // Bind data to views
        if (medicine != null) {
            tvName.setText(medicine.getName());
            tvExpiration.setText(medicine.getExpiration());
            tvMealTime.setText(medicine.getMealtime());
            tvQuantity.setText(medicine.getQuantity());

            String photoUrl = medicine.getPhoto();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Picasso.get()
                        .load(photoUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivPhoto);
            } else {
                ivPhoto.setImageResource(R.drawable.default_image);
            }
        }

        return view;
    }

}