package dev.edu.doctorappointment.Screen.Home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import dev.edu.doctorappointment.databinding.ActivitySearchBinding;

public class SearchActivity extends AppCompatActivity {
    ActivitySearchBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private AdapterDoctors adapterDoctors;
    private AdapterService adapterService;
    private List<ServiceModel> services = new ArrayList<>();
    private List<DoctorsModel> doctors = new ArrayList<>();
    private List<UserModel> favoriteDoctors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(binding.rbDoctor.isChecked()) {
                    List<DoctorsModel> filteredDoctors = new ArrayList<>();
                    for (DoctorsModel doctor : doctors) {
                        if (doctor.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                            filteredDoctors.add(doctor);
                        }
                    }
                    adapterDoctors = new AdapterDoctors(filteredDoctors, services);
                    binding.rvSearch.setAdapter(adapterDoctors);
                    binding.rvSearch.setHasFixedSize(true);
                    binding.rvSearch.setNestedScrollingEnabled(false);
                    binding.rvSearch.setClipToPadding(false);
                    binding.rvSearch.setClipChildren(false);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this, RecyclerView.VERTICAL, false);
                    binding.rvSearch.setLayoutManager(linearLayoutManager);
                } else {
                    List<ServiceModel> filteredServices = new ArrayList<>();
                    for (ServiceModel service : services) {
                        if (service.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                            filteredServices.add(service);
                        }
                    }
                    adapterService = new AdapterService(filteredServices);
                    binding.rvSearch.setAdapter(adapterService);
                    binding.rvSearch.setHasFixedSize(true);
                    binding.rvSearch.setNestedScrollingEnabled(false);
                    binding.rvSearch.setClipToPadding(false);
                    binding.rvSearch.setClipChildren(false);
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(SearchActivity.this, 3);
                    binding.rvSearch.setLayoutManager(gridLayoutManager);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setuprvSearchs();
        fetchServices();
        fetchDoctors();
        fetchFavoriteDoctors();
    }

    private void setuprvSearchs() {
        // Setup Services rvSearch
        adapterDoctors = new AdapterDoctors(doctors, services);

        // Setup Doctors rvSearch
        adapterService = new AdapterService(services);

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
        DatabaseReference userRef = database.getReference("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }
                favoriteDoctors.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    UserModel userModel = data.getValue(UserModel.class);
                    assert userModel != null;
                    if(userModel.getFavoriteDoctors() == null) return;
                    if (userModel.getFavoriteDoctors().size() > 0 && userModel.getKeyID().equals(userId))
                        favoriteDoctors.add(userModel);
                }

                if(favoriteDoctors.size() == 0) return;

                // Update favorite status in doctor adapter
                for (UserModel user : favoriteDoctors) {
                    for (DoctorsModel doctor : doctors) {
                        for (DoctorsModel favoriteDoctor : user.getFavoriteDoctors()) {
                            if (doctor.getDoctorId().equals(favoriteDoctor.getDoctorId())) {
                                doctor.setFavorite(true);
                            }
                        }
                    }
                }
                adapterDoctors.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}