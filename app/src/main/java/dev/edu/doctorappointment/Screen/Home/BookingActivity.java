package dev.edu.doctorappointment.Screen.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdapterBooking;
import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityBookingBinding;

public class BookingActivity extends AppCompatActivity {
    ActivityBookingBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRefService = database.getReference("Services");
    DatabaseReference myRefDoctor = database.getReference("Doctors");
    DatabaseReference myRefAppointment = database.getReference("Appointments");

    List<ServiceModel> serviceModels = new ArrayList<>();
    List<DoctorsModel> doctorsModels = new ArrayList<>();
    List<AppointmentModel> appointmentModels = new ArrayList<>();
    AdapterBooking adapterBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();

        adapterBooking = new AdapterBooking(appointmentModels, doctorsModels, serviceModels);
        binding.recyclerView.setAdapter(adapterBooking);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(20);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchDoctor();
        fetchService();
        fetchAppointment();
    }

    private void fetchAppointment() {
        myRefAppointment.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AppointmentModel model = dataSnapshot.getValue(AppointmentModel.class);
                    assert model != null;
                    if (model.getUserId().equals(new UserData(BookingActivity.this).getData("id"))) {
                        appointmentModels.add(model);
                    }
                }
                //ddaor nguoc list
                for (int i = 0; i < appointmentModels.size() / 2; i++) {
                    AppointmentModel temp = appointmentModels.get(i);
                    appointmentModels.set(i, appointmentModels.get(appointmentModels.size() - 1 - i));
                    appointmentModels.set(appointmentModels.size() - 1 - i, temp);
                }

                adapterBooking.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchService() {
        myRefService.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                serviceModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ServiceModel model = dataSnapshot.getValue(ServiceModel.class);
                    serviceModels.add(model);
                }
                adapterBooking.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDoctor() {
        myRefDoctor.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorsModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DoctorsModel model = dataSnapshot.getValue(DoctorsModel.class);
                    doctorsModels.add(model);
                }
                adapterBooking.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void init() {
        binding.textView4.setText("Booking");
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.main.startAnimation(animation);
        binding.mess.setOnClickListener(v -> {
            Intent intent = new Intent(BookingActivity.this, MessActivity.class);
            startActivity(intent);
        });
        binding.home.setOnClickListener(v -> {
            Intent intent = new Intent(BookingActivity.this, HomeActivity.class);
            startActivity(intent);
        });
        binding.profile.setOnClickListener(v -> {
            Intent intent = new Intent(BookingActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}