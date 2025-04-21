package dev.edu.app.Screen.Home;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.edu.app.Adapter.AdapterDoctors;
import dev.edu.app.Adapter.AdapterService;
import dev.edu.app.Model.DoctorsModel;
import dev.edu.app.Model.ServiceModel;
import dev.edu.app.Model.UserData;
import dev.edu.app.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityFavoBinding;

public class FavoActivity extends AppCompatActivity {

    ActivityFavoBinding binding;
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
        binding = ActivityFavoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adapterDoctors = new AdapterDoctors(doctors, services);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapterDoctors);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setNestedScrollingEnabled(false);
        binding.recyclerView.setClipToPadding(false);
        binding.recyclerView.setClipChildren(false);
    }

    public void back(View view) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchFavoriteDoctors();
        fetchServices();
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
                adapterDoctors.setServices(services); // Update services in doctor adapter
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
                    if (userModel.getFavoriteDoctors() == null) return;
                    if (!userModel.getFavoriteDoctors().isEmpty() && userModel.getKeyID().equals(userId))
                        favoriteDoctors.add(userModel);
                }
                // Update favorite status in doctor adapter
                doctors.clear();
                for (UserModel user : favoriteDoctors) {
                    doctors.addAll(user.getFavoriteDoctors());
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