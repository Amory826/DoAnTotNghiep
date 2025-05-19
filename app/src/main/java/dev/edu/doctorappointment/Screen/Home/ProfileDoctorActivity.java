package dev.edu.doctorappointment.Screen.Home;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.squareup.picasso.Picasso;

import dev.edu.doctorappointment.Adapter.WorkingHoursAdapter;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Screen.LoginRegister.LoginActivity;
import dev.edu.doctorappointment.databinding.ActivityProfileDoctorBinding;
import dev.edu.doctorappointment.databinding.DialogChagepassBinding;
import dev.edu.doctorappointment.databinding.DialogEditProfileDoctorBinding;
import dev.edu.doctorappointment.databinding.DialogWorkingHoursBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileDoctorActivity extends AppCompatActivity {

    ActivityProfileDoctorBinding binding;
    private UserData userData;
    private int maxBookingsPerSlot = 1; // Default value
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userData = new UserData(this);
        init();
        
        // Fetch doctor data from Firebase first, then load UI
        fetchDoctorData();
        
        binding.logout.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            binding.main.startAnimation(animation);
            new android.os.Handler().postDelayed(() -> {
                userData.clearData();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }, 500);
        });
        
        binding.editProfile.setOnClickListener(v -> showEditProfileDialog());
        binding.tvName.setOnClickListener(v -> showEditProfileDialog());
        binding.tvClinic.setOnClickListener(v -> showEditProfileDialog());
        binding.tvBirthYear.setOnClickListener(v -> showEditProfileDialog());
        binding.tvGender.setOnClickListener(v -> showEditProfileDialog());
        
        binding.changePassword.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            DialogChagepassBinding dialogChagepassBinding = DialogChagepassBinding.inflate(getLayoutInflater());
            bottomSheetDialog.setContentView(dialogChagepassBinding.getRoot());
            dialogChagepassBinding.btnClose.setOnClickListener(v1 -> bottomSheetDialog.dismiss());
            dialogChagepassBinding.btnChangePass.setOnClickListener(v1 -> {
                String oldPass = dialogChagepassBinding.edtOldPass.getText().toString();
                String newPass = dialogChagepassBinding.edtNewPass.getText().toString();
                String confirmPass = dialogChagepassBinding.edtConfirmPass.getText().toString();
                if(oldPass.isEmpty()){
                    dialogChagepassBinding.edtOldPass.setError("Mật khẩu cũ là bắt buộc");
                    dialogChagepassBinding.edtOldPass.requestFocus();
                    return;
                }
                if(newPass.isEmpty()){
                    dialogChagepassBinding.edtNewPass.setError("Mật khẩu mới là bắt buộc");
                    dialogChagepassBinding.edtNewPass.requestFocus();
                    return;
                }
                if(confirmPass.isEmpty()){
                    dialogChagepassBinding.edtConfirmPass.setError("Xác nhận mật khẩu là bắt buộc");
                    dialogChagepassBinding.edtConfirmPass.requestFocus();
                    return;
                }
                if(!newPass.equals(confirmPass)){
                    dialogChagepassBinding.edtConfirmPass.setError("Mật khẩu không khớp");
                    dialogChagepassBinding.edtConfirmPass.requestFocus();
                    return;
                }
                String password = userData.getData("password");
                if(!oldPass.equals(password)){
                    dialogChagepassBinding.edtOldPass.setError("Mật khẩu cũ không chính xác");
                    dialogChagepassBinding.edtOldPass.requestFocus();
                    return;
                }
                userData.saveData("password", newPass);
                FirebaseDatabase.getInstance().getReference("Doctors").child(userData.getData("id")).child("password").setValue(newPass);
                bottomSheetDialog.dismiss();
                TipDialog.show(this, "Đổi mật khẩu thành công", TipDialog.TYPE.SUCCESS);
            });
            bottomSheetDialog.show();
        });
        
        binding.scheduleSettings.setOnClickListener(v -> showWorkingHoursDialog());
    }
    
    private void fetchDoctorData() {
        WaitDialog.show(this, "Đang tải...");
        String doctorId = userData.getData("id");
        
        if (doctorId == null || doctorId.isEmpty()) {
            WaitDialog.dismiss();
            TipDialog.show(this, "Không thể tìm thấy thông tin bác sĩ", TipDialog.TYPE.ERROR);
            return;
        }
        
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Doctors").child(doctorId);
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DoctorsModel doctorModel = snapshot.getValue(DoctorsModel.class);
                    if (doctorModel != null) {
                        // Update maxBookingsPerSlot from Firebase
                        maxBookingsPerSlot = doctorModel.getMaxBookingsPerSlot();
                        if (maxBookingsPerSlot <= 0) {
                            maxBookingsPerSlot = 1; // Default value if invalid
                        }
                        
                        // Save to local storage for future use
                        userData.saveData("maxBookingsPerSlot", String.valueOf(maxBookingsPerSlot));
                        
                        // Log for debugging
                        Log.d("DoctorProfile", "Loaded maxBookingsPerSlot: " + maxBookingsPerSlot);
                    }
                }
                
                // Load the UI after fetching data
                loadDoctorInfo();
                WaitDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                WaitDialog.dismiss();
                TipDialog.show(ProfileDoctorActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), TipDialog.TYPE.ERROR);
                loadDoctorInfo(); // Still try to load with available data
            }
        });
    }
    
    private void loadDoctorInfo() {
        // Set doctor profile info
        binding.doctorName.setText(userData.getData("name"));
        binding.tvName.setText("Tên: " + userData.getData("name"));
        binding.tvClinic.setText("Vị trí: " + userData.getData("clinicName"));
        binding.tvBirthYear.setText("Năm sinh: " + userData.getData("birthYear"));
        binding.tvGender.setText("Giới tính: " + userData.getData("gender"));
        
        // Load doctor image if available
        if (userData.getData("profilePicture") != null && !userData.getData("profilePicture").isEmpty()) {
            Picasso.get()
                .load(userData.getData("profilePicture"))
                .placeholder(R.drawable.nam)
                .into(binding.ivProfilePicture);
        }
    }

    private void init() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.main.startAnimation(animation);
        binding.home.setOnClickListener(v -> {
            startActivity(new Intent(ProfileDoctorActivity.this, HomeDoctorActivity.class));
            finish();
        });
        binding.mess.setOnClickListener(v -> {
            startActivity(new Intent(ProfileDoctorActivity.this, MessActivity.class));
        });
        binding.booking.setOnClickListener(v -> {
            startActivity(new Intent(ProfileDoctorActivity.this, BookingDoctorActivity.class));
        });
        binding.profile.setImageResource(R.drawable.profile);
    }
    
    private void showEditProfileDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        DialogEditProfileDoctorBinding dialogBinding = DialogEditProfileDoctorBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(dialogBinding.getRoot());
        
        // Pre-populate fields with current data
        dialogBinding.edtName.setText(userData.getData("name"));
        dialogBinding.edtViTri.setText(userData.getData("clinicName"));
        dialogBinding.edtPhone.setText(userData.getData("birthYear"));
        
        // Get maxBookingsPerSlot from class variable (updated from Firebase)
        dialogBinding.edtMaxBooking.setText(String.valueOf(maxBookingsPerSlot));
        
        // Set close button listener
        dialogBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        
        // Set save button listener
        dialogBinding.btnSave.setOnClickListener(v -> {
            String name = dialogBinding.edtName.getText().toString().trim();
            String viTri = dialogBinding.edtViTri.getText().toString().trim();
            String birthYear = dialogBinding.edtPhone.getText().toString().trim();
            String maxBookingsPerSlotStr = dialogBinding.edtMaxBooking.getText().toString().trim();
            
            // Validate inputs
            if(name.isEmpty()) {
                dialogBinding.edtName.setError("Tên là bắt buộc");
                dialogBinding.edtName.requestFocus();
                return;
            }
            
            if(viTri.isEmpty()) {
                dialogBinding.edtViTri.setError("Vị trí là bắt buộc");
                dialogBinding.edtViTri.requestFocus();
                return;
            }
            
            if(birthYear.isEmpty()) {
                dialogBinding.edtPhone.setError("Năm sinh là bắt buộc");
                dialogBinding.edtPhone.requestFocus();
                return;
            }
            
            if(maxBookingsPerSlotStr.isEmpty()) {
                dialogBinding.edtMaxBooking.setError("Số lượng tối đa là bắt buộc");
                dialogBinding.edtMaxBooking.requestFocus();
                return;
            }
            
            // Validate maxBookingsPerSlot is a number greater than 0
            int maxBookingsValue;
            try {
                maxBookingsValue = Integer.parseInt(maxBookingsPerSlotStr);
                if (maxBookingsValue <= 0) {
                    dialogBinding.edtMaxBooking.setError("Số lượng phải lớn hơn 0");
                    dialogBinding.edtMaxBooking.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                dialogBinding.edtMaxBooking.setError("Số lượng phải là số");
                dialogBinding.edtMaxBooking.requestFocus();
                return;
            }
            
            // Update local storage
            userData.saveData("name", name);
            userData.saveData("clinicName", viTri);
            userData.saveData("birthYear", birthYear);
            userData.saveData("maxBookingsPerSlot", maxBookingsPerSlotStr);
            
            // Update class variable
            maxBookingsPerSlot = maxBookingsValue;
            
            // Update Firebase database
            DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Doctors")
                    .child(userData.getData("id"));
            
            doctorRef.child("name").setValue(name);
            doctorRef.child("clinicName").setValue(viTri);
            doctorRef.child("birthYear").setValue(Integer.parseInt(birthYear));
            doctorRef.child("maxBookingsPerSlot").setValue(maxBookingsValue);
            
            // Update UI
            binding.doctorName.setText(name);
            binding.tvName.setText("Tên: " + name);
            binding.tvClinic.setText("Vị trí: " + viTri);
            binding.tvBirthYear.setText("Năm sinh: " + birthYear);
            
            // Close dialog and show success message
            bottomSheetDialog.dismiss();
            TipDialog.show(this, "Thông tin đã được lưu", TipDialog.TYPE.SUCCESS);
        });
        
        bottomSheetDialog.show();
    }

    private void showWorkingHoursDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        DialogWorkingHoursBinding dialogBinding = DialogWorkingHoursBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(dialogBinding.getRoot());

        // Get current working hours from Firebase
        WaitDialog.show(this, "Đang tải...");
        String doctorId = userData.getData("id");
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Doctors").child(doctorId);
        
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                WaitDialog.dismiss();
                if (snapshot.exists()) {
                    DoctorsModel doctorModel = snapshot.getValue(DoctorsModel.class);
                    if (doctorModel != null && doctorModel.getWorkingHours() != null) {
                        List<String> currentWorkingHours = new ArrayList<>(doctorModel.getWorkingHours());
                        
                        // Setup RecyclerView
                        final WorkingHoursAdapter[] adapterRef = new WorkingHoursAdapter[1];
                        adapterRef[0] = new WorkingHoursAdapter(currentWorkingHours, position -> {
                            currentWorkingHours.remove(position);
                            adapterRef[0].updateData(currentWorkingHours);
                        });
                        dialogBinding.rvWorkingHours.setLayoutManager(new LinearLayoutManager(ProfileDoctorActivity.this));
                        dialogBinding.rvWorkingHours.setAdapter(adapterRef[0]);

                        // Time selection
                        dialogBinding.tvStartTime.setOnClickListener(v -> {
                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                ProfileDoctorActivity.this,
                                (view, hourOfDay, minute) -> {
                                    String time = String.format("%02d:%02d", hourOfDay, minute);
                                    dialogBinding.tvStartTime.setText(time);
                                },
                                8, 0, true
                            );
                            timePickerDialog.show();
                        });

                        dialogBinding.tvEndTime.setOnClickListener(v -> {
                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                ProfileDoctorActivity.this,
                                (view, hourOfDay, minute) -> {
                                    String time = String.format("%02d:%02d", hourOfDay, minute);
                                    dialogBinding.tvEndTime.setText(time);
                                },
                                17, 0, true
                            );
                            timePickerDialog.show();
                        });

                        // Add new working hours
                        dialogBinding.btnAddTime.setOnClickListener(v -> {
                            String startTime = dialogBinding.tvStartTime.getText().toString();
                            String endTime = dialogBinding.tvEndTime.getText().toString();

                            if (startTime.equals("Chọn giờ")) {
                                TipDialog.show(ProfileDoctorActivity.this, "Vui lòng chọn giờ bắt đầu", TipDialog.TYPE.WARNING);
                                return;
                            }

                            if (endTime.equals("Chọn giờ")) {
                                TipDialog.show(ProfileDoctorActivity.this, "Vui lòng chọn giờ kết thúc", TipDialog.TYPE.WARNING);
                                return;
                            }

                            // Validate time
                            String[] startParts = startTime.split(":");
                            String[] endParts = endTime.split(":");
                            int startHour = Integer.parseInt(startParts[0]);
                            int startMinute = Integer.parseInt(startParts[1]);
                            int endHour = Integer.parseInt(endParts[0]);
                            int endMinute = Integer.parseInt(endParts[1]);

                            if (startHour > endHour || (startHour == endHour && startMinute >= endMinute)) {
                                TipDialog.show(ProfileDoctorActivity.this, "Giờ kết thúc phải sau giờ bắt đầu", TipDialog.TYPE.ERROR);
                                return;
                            }

                            String workingHour = startTime + " - " + endTime;
                            if (!currentWorkingHours.contains(workingHour)) {
                                currentWorkingHours.add(workingHour);
                                adapterRef[0].updateData(currentWorkingHours);
                                dialogBinding.tvStartTime.setText("Chọn giờ");
                                dialogBinding.tvEndTime.setText("Chọn giờ");
                            } else {
                                TipDialog.show(ProfileDoctorActivity.this, "Ca làm việc này đã tồn tại", TipDialog.TYPE.WARNING);
                            }
                        });

                        // Save working hours
                        dialogBinding.btnSave.setOnClickListener(v -> {
                            if (currentWorkingHours.isEmpty()) {
                                TipDialog.show(ProfileDoctorActivity.this, "Vui lòng thêm ít nhất một ca làm việc", TipDialog.TYPE.WARNING);
                                return;
                            }

                            // Update Firebase
                            doctorRef.child("workingHours").setValue(currentWorkingHours)
                                    .addOnSuccessListener(aVoid -> {
                                        bottomSheetDialog.dismiss();
                                        TipDialog.show(ProfileDoctorActivity.this, "Đã lưu giờ làm việc", TipDialog.TYPE.SUCCESS);
                                    })
                                    .addOnFailureListener(e -> {
                                        TipDialog.show(ProfileDoctorActivity.this, "Lỗi khi lưu giờ làm việc: " + e.getMessage(), TipDialog.TYPE.ERROR);
                                    });
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                WaitDialog.dismiss();
                TipDialog.show(ProfileDoctorActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), TipDialog.TYPE.ERROR);
            }
        });

        // Cancel
        dialogBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }
} 