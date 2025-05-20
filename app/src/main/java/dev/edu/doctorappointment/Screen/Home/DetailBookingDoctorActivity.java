package dev.edu.doctorappointment.Screen.Home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kongzue.dialogx.dialogs.TipDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.FollowUpAppointmentModel;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Utils.NotificationHelper;
import dev.edu.doctorappointment.databinding.ActivityDetailBookingDoctorBinding;
import dev.edu.doctorappointment.databinding.DialogAppointmentBinding;

public class DetailBookingDoctorActivity extends AppCompatActivity {
    private ActivityDetailBookingDoctorBinding binding;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference appointmentsRef = database.getReference("Appointments");
    private DatabaseReference followUpAppointmentsRef = database.getReference("FollowUpAppointments");
    private DatabaseReference usersRef = database.getReference("Users");
    private String appointmentId;
    private AppointmentModel currentAppointment;
    private Calendar selectedDate = Calendar.getInstance();
    private String selectedTimeSlot;
    private String selectedService;

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

        // Follow-up appointment button
        binding.btnFollowUp.setOnClickListener(v -> showFollowUpDialog());
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
                // Update bookingCountByDateTime for the doctor
                String doctorId = currentAppointment.getDoctorId();
                String appointmentDate = currentAppointment.getAppointmentTime();
                String appointmentTime = currentAppointment.getAppointmentSlot();
                
                // Replace invalid characters for Firebase key
                String safeDate = appointmentDate.replace("/", "_").replace(".", "_");
                
                DatabaseReference doctorRef = database.getReference("Doctors").child(doctorId);
                doctorRef.get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        DoctorsModel doctor = snapshot.getValue(DoctorsModel.class);
                        if (doctor != null) {
                            Map<String, Map<String, Integer>> bookingCounts = doctor.getBookingCountByDateTime();
                            if (bookingCounts == null) {
                                bookingCounts = new HashMap<>();
                            }
                            
                            Map<String, Integer> timeSlots = bookingCounts.get(safeDate);
                            if (timeSlots == null) {
                                timeSlots = new HashMap<>();
                            }
                            
                            int currentCount = timeSlots.getOrDefault(appointmentTime, 0);
                            if (currentCount > 0) {
                                timeSlots.put(appointmentTime, currentCount - 1);
                                bookingCounts.put(safeDate, timeSlots);
                                
                                // Update bookingCountByDateTime
                                doctorRef.child("bookingCountByDateTime")
                                    .setValue(bookingCounts)
                                    .addOnSuccessListener(aVoid2 -> {
                                        TipDialog.show(this, "Thông tin đã được lưu", TipDialog.TYPE.SUCCESS);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(DetailBookingDoctorActivity.this, 
                                            "Lỗi khi cập nhật số lượng lịch hẹn", Toast.LENGTH_SHORT).show();
                                    });
                            } else {
                                TipDialog.show(this, "Thông tin đã được lưu", TipDialog.TYPE.SUCCESS);
                                finish();
                            }
                        }
                    }
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(DetailBookingDoctorActivity.this, 
                    "Lỗi khi lưu kết quả khám bệnh", Toast.LENGTH_SHORT).show();
            });
    }

    private void showFollowUpDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        DialogAppointmentBinding dialogBinding = DialogAppointmentBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(dialogBinding.getRoot());

        // Set up date picker
        dialogBinding.tvDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dialogBinding.tvDate.setText(sdf.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Get doctor's working hours
        DatabaseReference doctorRef = database.getReference("Doctors").child(currentAppointment.getDoctorId());
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DoctorsModel doctor = snapshot.getValue(DoctorsModel.class);
                    if (doctor != null && doctor.getWorkingHours() != null) {
                        List<String> workingHours = doctor.getWorkingHours();

                        // Set up time slots spinner with working hours
                        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(DetailBookingDoctorActivity.this,
                            android.R.layout.simple_spinner_item, workingHours);
                        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        dialogBinding.spnTime.setAdapter(timeAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailBookingDoctorActivity.this,
                    "Lỗi khi tải thông tin giờ làm việc", Toast.LENGTH_SHORT).show();
            }
        });
        DatabaseReference doctorR = database.getReference("Doctors").child(currentAppointment.getDoctorId());

        doctorR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DoctorsModel doctor = snapshot.getValue(DoctorsModel.class);
                    if (doctor != null && doctor.getServices() != null) {
                        // Tạo ArrayAdapter từ danh sách dịch vụ của bác sĩ
                        DatabaseReference servicesRef = database.getReference("Services");
                        List<String> serviceIds = doctor.getServices();
                        List<String> serviceNames = new ArrayList<>();

                        servicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (String id : serviceIds) {
                                    if (snapshot.hasChild(id)) {
                                        String name = snapshot.child(id).child("name").getValue(String.class);
                                        if (name != null) {
                                            serviceNames.add(name);
                                        }
                                    }
                                }

                                if (!serviceNames.isEmpty()) {
                                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(DetailBookingDoctorActivity.this,
                                            android.R.layout.simple_spinner_item, serviceNames);
                                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    dialogBinding.spnService.setAdapter(adapter1);
                                    dialogBinding.spnService.setSelection(0);
                                } else {
                                    Toast.makeText(DetailBookingDoctorActivity.this,
                                            "Không có dịch vụ nào cho bác sĩ này", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(DetailBookingDoctorActivity.this,
                                        "Lỗi khi tải thông tin dịch vụ", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialogBinding.spnService.setSelection(0);
                    } else {
                        Toast.makeText(DetailBookingDoctorActivity.this,
                                "Không có dịch vụ nào cho bác sĩ này", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailBookingDoctorActivity.this,
                            "Không tìm thấy thông tin bác sĩ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailBookingDoctorActivity.this,
                        "Lỗi khi tải thông tin bác sĩ", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up buttons
        dialogBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        dialogBinding.btnBook.setOnClickListener(v -> {
            String date = dialogBinding.tvDate.getText().toString();
            String time = dialogBinding.spnTime.getSelectedItem().toString();
            String service = dialogBinding.spnService.getSelectedItem().toString();

            if (date.equals("Choose Date")) {
                Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
                return;
            }

            createFollowUpAppointment(date, time, service);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void createFollowUpAppointment(String date, String time, String service) {
        if (currentAppointment == null) return;

        String followUpId = followUpAppointmentsRef.push().getKey();
        FollowUpAppointmentModel followUp = new FollowUpAppointmentModel(
            followUpId,
            appointmentId,
            currentAppointment.getUserId(),
            currentAppointment.getDoctorId(),
            date,
            time,
            service,
            "Chờ xác nhận",
            "Lịch tái khám"
        );

        followUpAppointmentsRef.child(followUpId).setValue(followUp)
            .addOnSuccessListener(aVoid -> {
                // Send notifications
                String patientTitle = "Lịch tái khám mới";
                String doctorTitle = "Lịch tái khám mới";
                
                String patientMessage = "Bác sĩ đã hẹn bạn tái khám vào " + date + " lúc " + time;
                String doctorMessage = "Bạn đã tạo lịch tái khám cho bệnh nhân vào " + date + " lúc " + time;

                // Send notification to patient
                NotificationHelper.sendAppointmentNotification(
                    currentAppointment.getUserId(),
                    patientTitle,
                    patientMessage,
                    "user",
                    this
                );

                // Send notification to doctor
                NotificationHelper.sendAppointmentNotification(
                    currentAppointment.getDoctorId(),
                    doctorTitle,
                    doctorMessage,
                    "doctor",
                    this
                );

                TipDialog.show(this, "Đã tạo lịch tái khám", TipDialog.TYPE.SUCCESS);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tạo lịch tái khám", Toast.LENGTH_SHORT).show();
            });
    }
}
