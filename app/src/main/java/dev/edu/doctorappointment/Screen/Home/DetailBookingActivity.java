package dev.edu.doctorappointment.Screen.Home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityDetailBookingBinding;

public class DetailBookingActivity extends AppCompatActivity {
    private ActivityDetailBookingBinding binding;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference appointmentsRef = database.getReference("Appointments");
    private DatabaseReference doctorsRef = database.getReference("Doctors");
    private DatabaseReference servicesRef = database.getReference("Services");
    private String appointmentId;
    private String doctorId;
    private String serviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy dữ liệu từ Intent
        appointmentId = getIntent().getStringExtra("appointmentId");
        doctorId = getIntent().getStringExtra("doctorId");
        serviceId = getIntent().getStringExtra("serviceId");

        if (appointmentId == null || doctorId == null || serviceId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        loadAppointmentDetails();
    }

    private void setupViews() {
        // Nút back
        binding.ivBack.setOnClickListener(v -> finish());

        // Nút hủy lịch
        binding.btnCancel.setOnClickListener(v -> {
            // TODO: Implement cancel appointment logic
            Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadAppointmentDetails() {
        appointmentsRef.child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DetailBookingActivity.this, "Không tìm thấy thông tin lịch hẹn", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                AppointmentModel appointment = snapshot.getValue(AppointmentModel.class);
                if (appointment != null) {
                    // Hiển thị thông tin cơ bản
                    binding.tvDate.setText("Ngày: " + appointment.getAppointmentTime());
                    binding.tvTime.setText("Giờ: " + appointment.getAppointmentSlot());
                    binding.tvStatus.setText(appointment.getStatus());

                    // Set status color based on appointment status
                    if ("Đã xác nhận".equals(appointment.getStatus()) || "Đã thanh toán".equals(appointment.getStatus())) {
                        binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else if ("Đã hủy".equals(appointment.getStatus()) || "Cancelled".equals(appointment.getStatus())) {
                        binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else {
                        binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    }

                    // Load thông tin bác sĩ
                    loadDoctorInfo();
                    // Load thông tin dịch vụ
                    loadServiceInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailBookingActivity.this, "Lỗi khi tải thông tin lịch hẹn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDoctorInfo() {
        doctorsRef.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DoctorsModel doctor = snapshot.getValue(DoctorsModel.class);
                    if (doctor != null) {
                        binding.tvDoctorName.setText("Bác sĩ " + doctor.getName());
                        binding.tvDoctorSpecialty.setText("Chuyên khoa: " + doctor.getClinicName());
                        
                        // Load avatar
                        if (!doctor.getProfilePicture().isEmpty()) {
                            Picasso.get().load(doctor.getProfilePicture()).into(binding.ivDoctorAvatar);
                        } else {
                            binding.ivDoctorAvatar.setImageResource(
                                doctor.getGender().equals("Nam") ? R.drawable.nam : R.drawable.nu
                            );
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailBookingActivity.this, "Lỗi khi tải thông tin bác sĩ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadServiceInfo() {
        servicesRef.orderByChild("name").equalTo(serviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ServiceModel service = dataSnapshot.getValue(ServiceModel.class);
                        if (service != null) {
                            binding.tvService.setText("Dịch vụ: " + service.getName());
                            binding.tvPrice.setText("Giá: " + service.getPrice() + " VNĐ");
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailBookingActivity.this, "Lỗi khi tải thông tin dịch vụ", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 