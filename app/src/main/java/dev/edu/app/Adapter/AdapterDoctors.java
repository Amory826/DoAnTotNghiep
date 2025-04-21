package dev.edu.app.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import dev.edu.app.Model.DoctorsModel;
import dev.edu.app.Model.ServiceModel;
import dev.edu.app.Model.UserData;
import dev.edu.app.Screen.Home.DetailActivity;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemDoctorBinding;

public class AdapterDoctors extends RecyclerView.Adapter<AdapterDoctors.MyViewHolder> {

    private List<DoctorsModel> doctors;
    private List<ServiceModel> services;

    public AdapterDoctors(List<DoctorsModel> doctors, List<ServiceModel> services) {
        this.doctors = doctors;
        this.services = services;
    }

    public void setServices(List<ServiceModel> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdapterDoctors.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDoctors.MyViewHolder holder, int position) {
        DoctorsModel doctor = doctors.get(position);

        // Set doctor's name
        holder.binding.tvDoctorName.setText(doctor.getName());

        // Set specialties
        StringBuilder specialties = new StringBuilder();
        for (String serviceId : doctor.getServices()) {
            for (ServiceModel service : services) {
                if (service.getKeyID().equals(serviceId)) {
                    if (specialties.length() > 0) {
                        specialties.append(", ");
                    }
                    specialties.append(service.getName());
                }
            }
        }
        holder.binding.tvSpecialty.setText(specialties.toString());

        // Load profile picture
        if (doctor.getProfilePicture() != null && !doctor.getProfilePicture().isEmpty()) {
            Picasso.get().load(doctor.getProfilePicture()).into(holder.binding.ivProfilePicture);
        } else {
            if ("Nam".equals(doctor.getGender())) {
                holder.binding.ivProfilePicture.setImageResource(R.drawable.nam);
            } else {
                holder.binding.ivProfilePicture.setImageResource(R.drawable.nu);
            }
        }

        // Set favorite icon
        if (doctor.isFavorite()) {
            holder.binding.btnFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.binding.btnFavorite.setImageResource(R.drawable.ic_favorite2);
        }

        // Handle favorite icon click
        holder.binding.btnFavorite.setOnClickListener(v -> {
            boolean newFavoriteStatus = !doctor.isFavorite();
            doctor.setFavorite(newFavoriteStatus);
            notifyItemChanged(position);

            // Update favorite status in Firebase
            updateFavoriteDoctorsInFirebase(newFavoriteStatus, doctor, v);

            // Show a toast message
            String message = doctor.getName() + (newFavoriteStatus ? " has been added to favorites." : " has been removed from favorites.");
            Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
        });

        holder.binding.btnInfo.setOnClickListener(v -> {
            // Show doctor's information
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            Animation animation = android.view.animation.AnimationUtils.loadAnimation(v.getContext(), R.anim.slide_in_right);
            v.startAnimation(animation);
            List<String> services = new ArrayList<>();
            for (String serviceId : doctor.getServices()) {
                for (ServiceModel service : this.services) {
                    if (service.getKeyID().equals(serviceId)) {
                        services.add(service.getName());
                    }
                }
            }
            doctor.setServices(services);
            intent.putExtra("doctor", doctor);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(doctors.size(), 4);
    }

    private void updateFavoriteDoctorsInFirebase(boolean isFavorite, DoctorsModel doctor, View v) {
        UserData userData = new UserData(v.getContext());
        String userId = userData.getData("id");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("favoriteDoctors");

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DoctorsModel> favoriteDoctors = new ArrayList<>();
                if (task.getResult().exists()) {
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        DoctorsModel favoriteDoctor = snapshot.getValue(DoctorsModel.class);
                        if (favoriteDoctor != null) {
                            favoriteDoctors.add(favoriteDoctor);
                        }
                    }
                }
                if (isFavorite) {
                    favoriteDoctors.add(doctor);
                } else {
                    favoriteDoctors.removeIf(d -> d.getDoctorId().equals(doctor.getDoctorId()));
                }
                userRef.setValue(favoriteDoctors);
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemDoctorBinding binding;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemDoctorBinding.bind(itemView);
        }
    }
}
