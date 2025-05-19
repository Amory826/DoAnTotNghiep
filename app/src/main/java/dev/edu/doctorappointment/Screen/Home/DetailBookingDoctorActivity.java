package dev.edu.doctorappointment.Screen.Home;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kongzue.dialogx.dialogs.TipDialog;

import java.util.HashMap;
import java.util.Map;

import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityDetailBookingDoctorBinding;

public class DetailBookingDoctorActivity extends AppCompatActivity {
    private ActivityDetailBookingDoctorBinding binding;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference appointmentsRef = database.getReference("Appointments");
    private DatabaseReference usersRef = database.getReference("Users");
    private String appointmentId;
    private AppointmentModel currentAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBookingDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get appointment ID from intent
        appointmentId = getIntent().getStringExtra("appointmentId");
        if (appointmentId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        loadAppointmentDetails();
    }

    private void setupViews() {
        // Back button
        binding.ivBack.setOnClickListener(v -> finish());

        // Submit button
        binding.btnSubmit.setOnClickListener(v -> saveExaminationResults());
    }

    private void loadAppointmentDetails() {
        appointmentsRef.child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentAppointment = snapshot.getValue(AppointmentModel.class);
                    if (currentAppointment != null) {
                        // Set appointment details
                        binding.tvAppointmentDate.setText("Ngày khám: " + currentAppointment.getAppointmentTime());
                        binding.tvAppointmentTime.setText("Giờ khám: " + currentAppointment.getAppointmentSlot());
                        binding.tvService.setText("Dịch vụ: " + currentAppointment.getServiceId());

                        // Load patient information
                        loadPatientInfo(currentAppointment.getUserId());

                        // Load existing examination results if any
                        if (currentAppointment.getExaminationResults() != null) {
                            Map<String, String> results = currentAppointment.getExaminationResults();
                            binding.etDiagnosis.setText(results.get("diagnosis"));
                            binding.etPrescription.setText(results.get("prescription"));
                            binding.etTreatmentGuide.setText(results.get("treatmentGuide"));
                            binding.etNotes.setText(results.get("notes"));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailBookingDoctorActivity.this, 
                    "Lỗi khi tải thông tin lịch hẹn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientInfo(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null) {
                        binding.tvPatientName.setText("Họ tên: " + user.getName());
                        binding.tvPatientPhone.setText("Số điện thoại: " + user.getPhone());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailBookingDoctorActivity.this, 
                    "Lỗi khi tải thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveExaminationResults() {
        if (currentAppointment == null) return;

        // Get input values
        String diagnosis = binding.etDiagnosis.getText().toString().trim();
        String prescription = binding.etPrescription.getText().toString().trim();
        String treatmentGuide = binding.etTreatmentGuide.getText().toString().trim();
        String notes = binding.etNotes.getText().toString().trim();

        // Validate required fields
        if (diagnosis.isEmpty()) {
            binding.etDiagnosis.setError("Vui lòng nhập chẩn đoán");
            return;
        }

        // Create examination results map
        Map<String, String> examinationResults = new HashMap<>();
        examinationResults.put("diagnosis", diagnosis);
        examinationResults.put("prescription", prescription);
        examinationResults.put("treatmentGuide", treatmentGuide);
        examinationResults.put("notes", notes);

        // Update appointment in Firebase
        appointmentsRef.child(appointmentId).child("status").setValue("Đã trả kết quả");
        appointmentsRef.child(appointmentId).child("ReturnResults").setValue(examinationResults)
            .addOnSuccessListener(aVoid -> {
                TipDialog.show(this, "Thông tin đã được lưu", TipDialog.TYPE.SUCCESS);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(DetailBookingDoctorActivity.this, 
                    "Lỗi khi lưu kết quả khám bệnh", Toast.LENGTH_SHORT).show();
            });
    }
}
