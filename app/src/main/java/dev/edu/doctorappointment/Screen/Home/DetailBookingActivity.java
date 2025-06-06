package dev.edu.doctorappointment.Screen.Home;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private AppointmentModel currentAppointment;

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
            showCancelConfirmationDialog();
        });
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy lịch")
            .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này?")
            .setPositiveButton("Có", (dialog, which) -> {
                cancelAppointment();
            })
            .setNegativeButton("Không", null)
            .show();
    }

    private void cancelAppointment() {
        if (currentAppointment == null) return;

        // Cập nhật trạng thái lịch hẹn
        appointmentsRef.child(appointmentId).child("status").setValue("Đã hủy");
        updateDoctorBookingCount();
    }

    private void updateDoctorBookingCount() {
        if (currentAppointment == null) return;

        String originalDate = currentAppointment.getAppointmentTime();
        // Convert date to dd_mm_yyyy format
        String[] dateParts = originalDate.split("/");
        String formattedDate = dateParts[0] + "_" + dateParts[1] + "_" + dateParts[2];
        String time = currentAppointment.getAppointmentSlot();
        Log.d("DoctorModel123", "Current count for date: " + formattedDate + ", time: " + time );
        
        doctorsRef.child(doctorId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                DoctorsModel doctor = snapshot.getValue(DoctorsModel.class);
                if (doctor != null) {
                    Map<String, Map<String, Integer>> bookingCounts = doctor.getBookingCountByDateTime();
                    if (bookingCounts == null) {
                        bookingCounts = new HashMap<>();
                    }
                    
                    Map<String, Integer> timeSlots = bookingCounts.get(formattedDate);
                    if (timeSlots == null) {
                        timeSlots = new HashMap<>();
                    }
                    
                    int currentCount = timeSlots.getOrDefault(time, 0);
                    Log.d("DoctorModel123", "Current count for date: " + formattedDate + ", time: " + time + " is: " + currentCount);
                    
                    if (currentCount > 0) {
                        timeSlots.put(time, currentCount - 1);
                        bookingCounts.put(formattedDate, timeSlots);
                        
                        // Debug log
                        Log.d("DoctorModel", "Updated count for date: " + formattedDate + ", time: " + time + " to: " + (currentCount - 1));
                        
                        // Cập nhật lại bookingCountByDateTime
                        doctorsRef.child(doctorId).child("bookingCountByDateTime")
                            .setValue(bookingCounts)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("DoctorModel", "Successfully updated bookingCountByDateTime");
                                Toast.makeText(DetailBookingActivity.this, 
                                    "Đã hủy lịch hẹn thành công", Toast.LENGTH_SHORT).show();
                                
                                // Gửi kết quả về BookingActivity để refresh
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("appointmentCancelled", true);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DoctorModel", "Error updating bookingCountByDateTime: " + e.getMessage());
                                Toast.makeText(DetailBookingActivity.this, 
                                    "Lỗi khi cập nhật số lượng lịch hẹn", Toast.LENGTH_SHORT).show();
                            });
                    } else {
                        Log.d("DoctorModel", "No bookings found for date: " + formattedDate + ", time: " + time);
                        // Nếu không có lịch hẹn nào cho slot này, vẫn cập nhật trạng thái
                        Toast.makeText(DetailBookingActivity.this, 
                            "Đã hủy lịch hẹn thành công", Toast.LENGTH_SHORT).show();
                        
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("appointmentCancelled", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("DoctorModel", "Error fetching doctor data: " + e.getMessage());
            Toast.makeText(DetailBookingActivity.this, 
                "Lỗi khi cập nhật số lượng lịch hẹn", Toast.LENGTH_SHORT).show();
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

                currentAppointment = snapshot.getValue(AppointmentModel.class);
                if (currentAppointment != null) {
                    // Hiển thị thông tin cơ bản
                    binding.tvDate.setText("Ngày: " + currentAppointment.getAppointmentTime());
                    binding.tvTime.setText("Giờ: " + currentAppointment.getAppointmentSlot());
                    binding.tvStatus.setText(currentAppointment.getStatus());

                    // Set status color and button visibility based on appointment status
                    String status = currentAppointment.getStatus();

                    // Lấy ngày hiện tại
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    // Parse ngày hẹn
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    try {
                        Date appointmentDate = dateFormat.parse(currentAppointment.getAppointmentTime());
                        Calendar appointmentCal = Calendar.getInstance();
                        appointmentCal.setTime(appointmentDate);
                        appointmentCal.set(Calendar.HOUR_OF_DAY, 0);
                        appointmentCal.set(Calendar.MINUTE, 0);
                        appointmentCal.set(Calendar.SECOND, 0);
                        appointmentCal.set(Calendar.MILLISECOND, 0);

                        boolean isAppointmentToday = appointmentCal.equals(today);
                        boolean isPastAppointment = appointmentCal.before(today);

                        if ("Đã xác nhận".equals(status) || "Đã thanh toán".equals(status) || "Đã trả kết quả".equals(status)) {
                            binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            // Chỉ cho phép hủy nếu không phải ngày hẹn và chưa qua ngày hẹn
                            if ("Đã thanh toán".equals(status) && !isAppointmentToday && !isPastAppointment) {
                                binding.btnCancel.setVisibility(View.VISIBLE);
                            } else {
                                binding.btnCancel.setVisibility(View.GONE);
                            }
                        } else if ("Đã hủy".equals(status) || "Cancelled".equals(status)) {
                            binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            binding.btnCancel.setVisibility(View.GONE);
                        } else {
                            binding.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                            // Chỉ cho phép hủy nếu không phải ngày hẹn và chưa qua ngày hẹn
                            if (!isAppointmentToday && !isPastAppointment) {
                                binding.btnCancel.setVisibility(View.VISIBLE);
                            } else {
                                binding.btnCancel.setVisibility(View.GONE);
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        // Nếu có lỗi khi parse ngày, ẩn nút hủy để đảm bảo an toàn
                        binding.btnCancel.setVisibility(View.GONE);
                    }

                    // Load examination results if status is "Đã trả kết quả"
                    if ("Đã trả kết quả".equals(status)) {
                        loadExaminationResults();
                    } else {
                        binding.layoutExaminationResults.setVisibility(View.GONE);
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

    private void loadExaminationResults() {
        binding.layoutExaminationResults.setVisibility(View.VISIBLE);
        
        // Load examination results from ReturnResults
        appointmentsRef.child(appointmentId).child("ReturnResults").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String diagnosis = snapshot.child("diagnosis").getValue(String.class);
                    String prescription = snapshot.child("prescription").getValue(String.class);
                    String treatmentGuide = snapshot.child("treatmentGuide").getValue(String.class);
                    
                    if (diagnosis != null) {
                        binding.tvDiagnosis.setText(diagnosis);
                    }
                    if (prescription != null) {
                        binding.tvPrescription.setText(prescription);
                    }
                    if (treatmentGuide != null) {
                        binding.tvTreatmentGuide.setText(treatmentGuide);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailBookingActivity.this, "Lỗi khi tải kết quả khám bệnh", Toast.LENGTH_SHORT).show();
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