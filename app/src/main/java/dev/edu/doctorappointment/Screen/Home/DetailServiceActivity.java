package dev.edu.doctorappointment.Screen.Home;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdapterDoctors;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityDetailBinding;
import dev.edu.doctorappointment.databinding.ActivityDetailServiceBinding;

public class DetailServiceActivity extends AppCompatActivity {
    ActivityDetailServiceBinding binding;
    ServiceModel service;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private List<DoctorsModel> doctors = new ArrayList<>();
    private AdapterDoctors adapterDoctors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get service from intent
        if (getIntent().hasExtra("SERVICE_KEY")) {
            service = (ServiceModel) getIntent().getSerializableExtra("SERVICE_KEY");
            displayServiceDetails();
            fetchDoctorsByService();
        } else {
            Toast.makeText(this, "Error: No service data found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listeners
        binding.ivBack.setOnClickListener(v -> finish());
//        binding.btnBookService.setOnClickListener(v -> handleBookService());
    }

    private void displayServiceDetails() {
        if (service != null) {
            binding.tvDoctorName.setText(service.getName());
            binding.tvSpecialty.setText("Giá thành: " + service.getPrice());
            binding.tvAboutDoctor.setText(service.getDescription());
            
            // Load service icon if available
            if (service.getIcon() != null && !service.getIcon().isEmpty()) {
                Picasso.get().load(service.getIcon()).into(binding.ivProfilePicture);
            }
        }
    }

    private void fetchDoctorsByService() {
        if (service == null || service.getKeyID() == null) return;

        DatabaseReference doctorRef = database.getReference("Doctors");
        doctorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctors.clear();
                if (!snapshot.exists()) {
                    return;
                }
                
                for (DataSnapshot data : snapshot.getChildren()) {
                    DoctorsModel doctor = data.getValue(DoctorsModel.class);
                    if (doctor != null && doctor.getServices() != null && 
                        doctor.getServices().contains(service.getKeyID())) {
                        doctors.add(doctor);
                    }
                }
                
                setupDoctorsRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailServiceActivity.this, "Failed to load doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDoctorsRecyclerView() {
        List<ServiceModel> servicesList = new ArrayList<>();
        servicesList.add(service);
        
        adapterDoctors = new AdapterDoctors(doctors, servicesList);
        binding.recyclerViewWorkingHours.setAdapter(adapterDoctors);
        binding.recyclerViewWorkingHours.setLayoutManager(new LinearLayoutManager(this));
    }

    private void handleBookService() {
        if (doctors.isEmpty()) {
            Toast.makeText(this, "No doctors available for this service", Toast.LENGTH_SHORT).show();
            return;
        }

        // You could either:
        // 1. Show a dialog to choose a doctor first
        // 2. Or redirect to a booking page with the service pre-selected
        
        // Option 1: Show dialog to choose doctor
        if (doctors.size() > 1) {
            showDoctorSelectionDialog();
        } else {
            // Only one doctor available, proceed directly with that doctor
            navigateToBooking(doctors.get(0));
        }
    }

    private void showDoctorSelectionDialog() {
        // Create a dialog with a list of doctors
        String[] doctorNames = new String[doctors.size()];
        for (int i = 0; i < doctors.size(); i++) {
            doctorNames[i] = doctors.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Doctor")
               .setItems(doctorNames, (dialog, which) -> {
                   // User selected a doctor, navigate to booking
                   navigateToBooking(doctors.get(which));
               })
               .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
        
        builder.create().show();
    }

    private void navigateToBooking(DoctorsModel doctor) {
        // Navigate to booking screen with doctor and service data
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("DOCTOR_KEY", doctor);
        intent.putExtra("SERVICE_KEY", service);
        startActivity(intent);
    }
}