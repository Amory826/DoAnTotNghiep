package dev.edu.doctorappointment.Screen.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdapterAppointmentDoctor;
import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityBookingDoctorBinding;

public class BookingDoctorActivity extends AppCompatActivity {
    ActivityBookingDoctorBinding binding;
    private List<AppointmentModel> appointments = new ArrayList<>();
    private AdapterAppointmentDoctor adapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private UserData userData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBookingDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UserData
        userData = new UserData(this);

        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup navigation and UI
        setupNavigation();
        
        // Load doctor's appointments
        loadDoctorAppointments();
    }
    
    private void setupRecyclerView() {
        adapter = new AdapterAppointmentDoctor(appointments);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void setupNavigation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.main.startAnimation(animation);
        
        binding.home.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeDoctorActivity.class);
            startActivity(intent);
        });
        
        binding.mess.setOnClickListener(v -> {
            Intent intent = new Intent(this, MessActivity.class);
            intent.putExtra("userType", "doctor");
            startActivity(intent);
        });
        
        binding.profile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userType", "doctor");
            startActivity(intent);
        });
        
        binding.booking.setOnClickListener(v -> {
            // Already in BookingDoctorActivity - do nothing or refresh
        });
    }
    
    private void loadDoctorAppointments() {
        String doctorId = userData.getData("id");
        if (doctorId == null || doctorId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin bác sĩ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Log for debugging
        Log.d("BookingDoctorActivity", "Loading appointments for doctor ID: " + doctorId);
        
        DatabaseReference appointmentsRef = database.getReference("Appointments");
        Query query = appointmentsRef.orderByChild("doctorId").equalTo(doctorId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointments.clear();
                if (!snapshot.exists()) {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(BookingDoctorActivity.this, "Không có lịch hẹn nào", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AppointmentModel appointment = dataSnapshot.getValue(AppointmentModel.class);
                    if (appointment != null) {
                        appointments.add(appointment);
                        Log.d("BookingDoctorActivity", "Added appointment: " + appointment.getAppointmentId());
                    }
                }
                
                // Sort appointments in reverse order (newest first)
                for (int i = 0; i < appointments.size() / 2; i++) {
                    AppointmentModel temp = appointments.get(i);
                    appointments.set(i, appointments.get(appointments.size() - 1 - i));
                    appointments.set(appointments.size() - 1 - i, temp);
                }
                
                adapter.notifyDataSetChanged();
                
                if (appointments.isEmpty()) {
                    Toast.makeText(BookingDoctorActivity.this, "Không có lịch hẹn nào", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("BookingDoctorActivity", "Found " + appointments.size() + " appointments");
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingDoctorActivity.this, "Lỗi khi tải lịch hẹn", Toast.LENGTH_SHORT).show();
                Log.e("BookingDoctorActivity", "Database error: " + error.getMessage());
            }
        });
    }
}
