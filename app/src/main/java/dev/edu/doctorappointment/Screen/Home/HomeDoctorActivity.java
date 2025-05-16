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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dev.edu.doctorappointment.Adapter.AdapterAppointmentDoctor;
import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Screen.LoginRegister.LoginActivity;
import dev.edu.doctorappointment.databinding.ActivityHomeDoctorBinding;

public class HomeDoctorActivity extends AppCompatActivity {

    ActivityHomeDoctorBinding binding;
    private List<AppointmentModel> appointments = new ArrayList<>();
    private AdapterAppointmentDoctor adapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private UserData userData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UserData
        userData = new UserData(this);

        // Setup UI
        setupUI();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup navigation buttons
        setupNavigation();

        // Load today's appointments
        loadTodayAppointments();
    }

    private void setupUI() {
        // Display doctor information
        binding.tvDoctorName.setText(userData.getData("name"));
        binding.tvClinicName.setText("Phòng khám: " + userData.getData("clinicName"));
        binding.tvSpecialtyDoctor.setText("Bác sĩ chuyên khoa");

        // Load profile picture if available
        String profilePicture = userData.getData("profilePicture");
        if (profilePicture != null && !profilePicture.isEmpty()) {
            Picasso.get().load(profilePicture).into(binding.ivProfilePicture);
        }

        // Set today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        String today = dateFormat.format(new Date());
        binding.dateToday.setText(today);

        // Apply animation
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.main.startAnimation(animation);
    }

    private void setupRecyclerView() {
        adapter = new AdapterAppointmentDoctor(appointments);
        binding.recyclerAppointments.setAdapter(adapter);
        binding.recyclerAppointments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupNavigation() {
        binding.home.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeDoctorActivity.class));
        });

        binding.mess.setOnClickListener(v -> {
            Intent intent = new Intent(this, MessActivity.class);
            // Pass userType to ensure proper navigation back
            intent.putExtra("userType", "doctor");
            startActivity(intent);
        });

        binding.profile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            // Pass userType to ensure proper navigation back
            intent.putExtra("userType", "doctor");
            startActivity(intent);
        });

        binding.booking.setOnClickListener(v -> {
            // For doctors, this might show a list of appointments or calendar
        });
    }

    private void loadTodayAppointments() {
        String doctorId = userData.getData("id");
        if (doctorId == null || doctorId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin bác sĩ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get today's date in string format for filtering
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String today = dateFormat.format(new Date());

        // For debugging - show date we're looking for
        Log.d("AppointmentDebug", "Today's date: " + today);
        Log.d("AppointmentDebug", "Doctor ID: " + doctorId);

        DatabaseReference appointmentsRef = database.getReference("Appointments");
        Query query = appointmentsRef.orderByChild("doctorId").equalTo(doctorId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointments.clear();
                if (!snapshot.exists()) {
                    adapter.notifyDataSetChanged();
                    return;
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AppointmentModel appointment = dataSnapshot.getValue(AppointmentModel.class);
                    if (appointment != null) {
                        // tim kiem cac lich kham benh có tong ngay hom nay
                        if (appointment.getAppointmentTime() != null &&
                            appointment.getAppointmentTime().equals(today)) {
                            appointments.add(appointment);
                            Log.d("AppointmentDebug", "Added appointment for today: " + appointment.getAppointmentTime());
                        }
                    }
                }

                adapter.notifyDataSetChanged();

                // Show a message if no appointments
                if (appointments.isEmpty()) {
                    Toast.makeText(HomeDoctorActivity.this, "Không có lịch hẹn hôm nay", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("AppointmentDebug", "Found " + appointments.size() + " appointments for today");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeDoctorActivity.this, "Lỗi khi tải lịch hẹn", Toast.LENGTH_SHORT).show();
                Log.e("AppointmentDebug", "Database error: " + error.getMessage());
            }
        });
    }
}
