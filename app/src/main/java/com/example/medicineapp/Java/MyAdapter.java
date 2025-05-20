package com.example.medicineapp.Java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicineapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>
{
    Context context;
    ArrayList<Medicine> medList;
    private FirebaseServices fbs;
    private OnItemClickListener listener;

    // Define the interface
    public interface OnItemClickListener {
        void onItemClick(Medicine medicine); // Pass the clicked Medicine object
    }

    public MyAdapter(Context context, ArrayList<Medicine> medList, OnItemClickListener listener) {
        this.context = context;
        this.medList = medList;
        this.fbs = FirebaseServices.getInstance();
        this.listener = listener;
    }


    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v= LayoutInflater.from(context).inflate(R.layout.med_item ,parent,false);
        return  new MyAdapter.MyViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        Medicine med = medList.get(position);
        holder.tvName.setText(med.getName());
        holder.tvExpiration.setText(med.getExpiration());
        holder.tvMealTme.setText(med.getMealtime());
        holder.tvQuantity.setText(med.getQuantity());
        // Load image into ImageView using Glide

        // Load image into ImageView using Picasso
        String photo = med.getPhoto();
        if (photo != null && !photo.isEmpty()) {
            Picasso.get()
                    .load(photo)
                    .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                    .error(R.drawable.error_image) // Error image if loading fails
                    .into(holder.ivMedPhoto);
        } else {
            holder.ivMedPhoto.setImageResource(R.drawable.default_image); // Default image
        }
        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(med); // Notify listener with the clicked Medicine
            }
        });

    }

    @Override
    public int getItemCount(){
        return medList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        TextView tvExpiration;
        TextView tvMealTme;
        TextView tvQuantity;
        ImageView ivMedPhoto;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.tvNameAddMed);
            tvExpiration=itemView.findViewById(R.id.tvExpirationMedItem);
            tvMealTme=itemView.findViewById(R.id.tvMealTimeAddMed);
            tvQuantity=itemView.findViewById(R.id.tvQuantityAddMed);
            ivMedPhoto = itemView.findViewById(R.id.imageView2);


        }
    }



}
