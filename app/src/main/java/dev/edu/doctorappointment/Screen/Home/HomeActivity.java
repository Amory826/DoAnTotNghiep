package dev.edu.doctorappointment.Screen.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdapterDoctors;
import dev.edu.doctorappointment.Adapter.AdapterService;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private AdapterDoctors adapterDoctors;
    private AdapterService adapterService;
    private List<ServiceModel> services = new ArrayList<>();
    private List<DoctorsModel> doctors = new ArrayList<>();
    private List<DoctorsModel> favoriteDoctors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        setupRecyclerViews();
        fetchServices();
        fetchDoctors();
        fetchFavoriteDoctors();
    }

    private void setupRecyclerViews() {
        // Setup Services RecyclerView
        adapterService = new AdapterService(services);
        binding.recyclerView.setAdapter(adapterService);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setNestedScrollingEnabled(true);
        binding.recyclerView.setClipToPadding(false);
        binding.recyclerView.setClipChildren(false);
        
        // Create a custom GridLayoutManager with 2 rows and horizontal scrolling
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
        binding.recyclerView.setLayoutManager(gridLayoutManager);

        // Setup Doctors RecyclerView
        adapterDoctors = new AdapterDoctors(doctors, services);
        binding.recyclerView2.setAdapter(adapterDoctors);
        binding.recyclerView2.setHasFixedSize(true);
        binding.recyclerView2.setNestedScrollingEnabled(false);
        binding.recyclerView2.setClipToPadding(false);
        binding.recyclerView2.setClipChildren(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.recyclerView2.setLayoutManager(linearLayoutManager);
    }

    private void fetchServices() {
        DatabaseReference serviceRef = database.getReference("Services");
        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                services.clear();
                if (!snapshot.exists()) {
                    return;
                }
                for (DataSnapshot data : snapshot.getChildren()) {
                    ServiceModel service = data.getValue(ServiceModel.class);
                    services.add(service);
                }
                adapterService.notifyDataSetChanged();
                adapterDoctors.setServices(services); // Update services in doctor adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void fetchDoctors() {
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
                    doctors.add(doctor);
                }
                adapterDoctors.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void fetchFavoriteDoctors() {
        UserData userData = new UserData(this);
        String userId = userData.getData("id");
        DatabaseReference userRef = database.getReference("Users").child(userId).child("favoriteDoctors");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteDoctors.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        DoctorsModel doctor = data.getValue(DoctorsModel.class);
                        if (doctor != null) {
                            favoriteDoctors.add(doctor);
                        }
                    }

                    // Update favorite status in doctor adapter
                    for (DoctorsModel doctor : doctors) {
                        for (DoctorsModel favoriteDoctor : favoriteDoctors) {
                            if (doctor.getDoctorId().equals(favoriteDoctor.getDoctorId())) {
                                doctor.setFavorite(true);
                            }
                        }
                    }
                    adapterDoctors.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        binding.search.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    private void init() {
        binding.fullname.setText(new UserData(this).getData("name"));
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.main.startAnimation(animation);
        binding.mess.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MessActivity.class);
            startActivity(intent);
        });
        binding.booking.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookingActivity.class);
            startActivity(intent);
        });
        binding.profile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}
