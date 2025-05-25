package dev.edu.doctorappointment.Screen.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.databinding.ActivityAppointmentManagementBinding;

public class AppointmentManagementActivity extends AppCompatActivity {
    private ActivityAppointmentManagementBinding binding;
    private List<AppointmentModel> allAppointments;
    private List<AppointmentModel> filteredAppointments;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference appointmentRef;
    private DatabaseReference doctorRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupChipGroup();
        loadAppointments();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        allAppointments = new ArrayList<>();
        filteredAppointments = new ArrayList<>();
//        adapter = new AppointmentAdapter(filteredAppointments,
//                this::updateAppointmentStatus,
//                this::loadDoctorInfo);

//        binding.rvAppointments.setAdapter(adapter);
//        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(this));

        appointmentRef = database.getReference("Appointments");
        doctorRef = database.getReference("Doctors");
    }

    private void setupChipGroup() {
        binding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> filterAppointments());
    }

    private void loadAppointments() {
        binding.progressBar.setVisibility(View.VISIBLE);
        appointmentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allAppointments.clear();
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    AppointmentModel appointment = appointmentSnapshot.getValue(AppointmentModel.class);
                    if (appointment != null) {
                        appointment.setAppointmentId(appointmentSnapshot.getKey());
                        allAppointments.add(appointment);
                    }
                }
                filterAppointments();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppointmentManagementActivity.this,
                        "Lỗi: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterAppointments() {
        Chip selectedChip = findViewById(binding.chipGroup.getCheckedChipId());
        String filter = selectedChip != null ? selectedChip.getText().toString() : "Tất cả";

        filteredAppointments.clear();
        filteredAppointments.addAll(allAppointments.stream()
                .filter(appointment -> {
                    switch (filter) {
                        case "Chờ xác nhận":
                            return appointment.getStatus().equals("pending");
                        case "Đã xác nhận":
                            return appointment.getStatus().equals("approved");
                        case "Đã từ chối":
                            return appointment.getStatus().equals("rejected");
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList()));

//        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        binding.tvEmpty.setVisibility(filteredAppointments.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateAppointmentStatus(AppointmentModel appointment, String newStatus) {
        binding.progressBar.setVisibility(View.VISIBLE);
        appointment.setStatus(newStatus);

        appointmentRef.child(appointment.getAppointmentId()).setValue(appointment)
                .addOnSuccessListener(unused -> {
                    String message = "Đã cập nhật trạng thái lịch hẹn";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                });
    }

//    private void loadDoctorInfo(AppointmentModel appointment, AppointmentAdapter.DoctorInfoCallback callback) {
//        doctorRef.child(appointment.getDoctorId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                DoctorsModel doctor = snapshot.getValue(DoctorsModel.class);
//                if (doctor != null) {
//                    callback.onDoctorInfoLoaded(doctor);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(AppointmentManagementActivity.this,
//                        "Lỗi tải thông tin bác sĩ: " + error.getMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}