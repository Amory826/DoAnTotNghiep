package dev.edu.doctorappointment.Screen.Admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdminAppointmentAdapter;
import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.databinding.ActivityAppointmentManagementBinding;

public class AppointmentManagementActivity extends AppCompatActivity
        implements AdminAppointmentAdapter.AppointmentActionListener {
    private static final String TAG = "AppointmentManagement";
    private ActivityAppointmentManagementBinding binding;
    private AdminAppointmentAdapter adapter;
    private List<AppointmentModel> allAppointments;
    private DatabaseReference appointmentRef;
    private DatabaseReference doctorRef;
    private DatabaseReference userRef;

    // Constants for appointment status
    private static final String STATUS_PAID = "Đã thanh toán";
    private static final String STATUS_CONFIRMED = "Đã xác nhận";
    private static final String STATUS_COMPLETED = "Đã trả kết quả";
    private static final String STATUS_CANCELLED = "Đã hủy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFirebase();
        setupRecyclerView();
        setupChipGroup();
        loadAppointments();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        appointmentRef = database.getReference("Appointments");
        doctorRef = database.getReference("Doctors");
        userRef = database.getReference("Users");
    }

    private void setupRecyclerView() {
        allAppointments = new ArrayList<>();
        adapter = new AdminAppointmentAdapter(
                this,
                allAppointments,
                this,
                this::loadDoctorInfo,
                this::loadUserInfo
        );
        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAppointments.setAdapter(adapter);
    }

    private void loadUserInfo(String userId, AdminAppointmentAdapter.UserInfoCallback callback) {
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    callback.onUserInfoLoaded(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading user info: " + error.getMessage());
            }
        });
    }

    private void setupChipGroup() {
        binding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                String status = getStatusFromChipId(checkedId);
                filterAppointments(status);
            }
        });
    }

    private String getStatusFromChipId(int chipId) {
        if (chipId == binding.chipAll.getId()) return "all";
        if (chipId == binding.chipPaid.getId()) return STATUS_PAID;
        if (chipId == binding.chipConfirmed.getId()) return STATUS_CONFIRMED;
        if (chipId == binding.chipCompleted.getId()) return STATUS_COMPLETED;
        if (chipId == binding.chipCancelled.getId()) return STATUS_CANCELLED;
        return "all";
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
                filterAppointments(getStatusFromChipId(binding.chipGroup.getCheckedChipId()));
                binding.progressBar.setVisibility(View.GONE);
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading appointments: " + error.getMessage());
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(AppointmentManagementActivity.this,
                        "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDoctorInfo(AppointmentModel appointment, AdminAppointmentAdapter.DoctorInfoCallback callback) {
        doctorRef.child(appointment.getDoctorId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DoctorsModel doctor = snapshot.getValue(DoctorsModel.class);
                if (doctor != null) {
                    callback.onDoctorInfoLoaded(doctor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading doctor info: " + error.getMessage());
            }
        });
    }

    private void filterAppointments(String status) {
        List<AppointmentModel> filteredList = new ArrayList<>();
        if (status.equals("all")) {
            filteredList.addAll(allAppointments);
        } else {
            for (AppointmentModel appointment : allAppointments) {
                if (appointment.getStatus().equals(status)) {
                    filteredList.add(appointment);
                }
            }
        }
        adapter.updateAppointments(filteredList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvAppointments.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvAppointments.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onApprove(AppointmentModel appointment) {
        updateAppointmentStatus(appointment, STATUS_CONFIRMED);
    }

    @Override
    public void onCancel(AppointmentModel appointment) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận từ chối")
                .setMessage("Bạn có chắc chắn muốn từ chối lịch hẹn này?")
                .setPositiveButton("Từ chối", (dialog, which) ->
                        updateAppointmentStatus(appointment, STATUS_CANCELLED))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onConfirm(AppointmentModel appointment) {

    }

    @Override
    public void onPayment(AppointmentModel appointment) {

    }

    private void updateAppointmentStatus(AppointmentModel appointment, String newStatus) {
        appointmentRef.child(appointment.getAppointmentId())
                .child("status")
                .setValue(newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(this,
                        "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}