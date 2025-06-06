package dev.edu.doctorappointment.Screen.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kongzue.dialogx.dialogs.TipDialog;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityProfileBinding;
import dev.edu.doctorappointment.databinding.DialogChagepassBinding;
import dev.edu.doctorappointment.databinding.DialogEditProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    ActivityProfileBinding binding;
    private UserData userData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userData = new UserData(this);
        init();

        binding.logout.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            binding.main.startAnimation(animation);
            
            // Get current FCM token
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String currentToken = task.getResult();
                            String userId = userData.getData("id");
                            String userType = userData.getData("userType");
                            
                            // Remove current device token
                            DatabaseReference tokensRef = FirebaseDatabase.getInstance()
                                    .getReference("UserTokens")
                                    .child(userType)
                                    .child(userId);
                            
                            tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        List<String> tokens = new ArrayList<>();
                                        
                                        // Keep all tokens except the current device's token
                                        for (DataSnapshot tokenSnapshot : snapshot.getChildren()) {
                                            String token = tokenSnapshot.getValue(String.class);
                                            if (token != null && !token.equals(currentToken)) {
                                                tokens.add(token);
                                            }
                                        }
                                        
                                        // Update tokens list in Firebase
                                        tokensRef.setValue(tokens)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Token removed successfully");
                                                    // Clear user data and navigate to login
                                                    userData.clearData();
                                                    startActivity(new Intent(ProfileActivity.this, 
                                                            dev.edu.doctorappointment.Screen.Splash.MainActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error removing token", e);
                                                    // Still proceed with logout even if token removal fails
                                                    userData.clearData();
                                                    startActivity(new Intent(ProfileActivity.this, 
                                                            dev.edu.doctorappointment.Screen.Splash.MainActivity.class));
                                                    finish();
                                                });
                                    } else {
                                        // No tokens found, proceed with normal logout
                                        userData.clearData();
                                        startActivity(new Intent(ProfileActivity.this, 
                                                dev.edu.doctorappointment.Screen.Splash.MainActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error getting tokens", error.toException());
                                    // Proceed with normal logout even if there's an error
                                    userData.clearData();
                                    startActivity(new Intent(ProfileActivity.this, 
                                            dev.edu.doctorappointment.Screen.Splash.MainActivity.class));
                                    finish();
                                }
                            });
                        } else {
                            Log.e(TAG, "Failed to get FCM token", task.getException());
                            // Proceed with normal logout if token retrieval fails
                            userData.clearData();
                            startActivity(new Intent(ProfileActivity.this, 
                                    dev.edu.doctorappointment.Screen.Splash.MainActivity.class));
                            finish();
                        }
                    });
        });

        binding.payment.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, Payment3Activity.class);
            Animation animation =  AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            binding.main.startAnimation(animation);
            startActivity(intent);
        });

        binding.favorite.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FavoActivity.class);
            Animation animation =  AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            binding.main.startAnimation(animation);
            startActivity(intent);
        });

        binding.tvName.setText(userData.getData("name"));
        binding.tvEmail.setText(userData.getData("email"));
        binding.tvPhone.setText(userData.getData("phone"));
        
        // Add click listeners to edit profile information
        binding.tvName.setOnClickListener(v -> showEditProfileDialog());
        binding.tvEmail.setOnClickListener(v -> showEditProfileDialog());
        binding.tvPhone.setOnClickListener(v -> showEditProfileDialog());
        // Add an edit profile button click listener if one exists in the layout
        if (binding.editProfile != null) {
            binding.editProfile.setOnClickListener(v -> showEditProfileDialog());
        }

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
                    dialogChagepassBinding.edtOldPass.setError("Old password is required");
                    dialogChagepassBinding.edtOldPass.requestFocus();
                    return;
                }
                if(newPass.isEmpty()){
                    dialogChagepassBinding.edtNewPass.setError("New password is required");
                    dialogChagepassBinding.edtNewPass.requestFocus();
                    return;
                }
                if(confirmPass.isEmpty()){
                    dialogChagepassBinding.edtConfirmPass.setError("Confirm password is required");
                    dialogChagepassBinding.edtConfirmPass.requestFocus();
                    return;
                }
                if(!newPass.equals(confirmPass)){
                    dialogChagepassBinding.edtConfirmPass.setError("Password not match");
                    dialogChagepassBinding.edtConfirmPass.requestFocus();
                    return;
                }
                String password = userData.getData("password");
                if(!oldPass.equals(password)){
                    dialogChagepassBinding.edtOldPass.setError("Old password is incorrect");
                    dialogChagepassBinding.edtOldPass.requestFocus();
                    return;
                }
                userData.saveData("password", newPass);
                FirebaseDatabase.getInstance().getReference("Users").child(userData.getData("id")).child("password").setValue(newPass);
                bottomSheetDialog.dismiss();
                TipDialog.show(this, "Change password success", TipDialog.TYPE.SUCCESS);
            });
            bottomSheetDialog.show();

        });
    }

    // Helper method to navigate to the appropriate home screen based on user type
    private void navigateToHome() {
        String userType = userData.getData("userType");
        Intent intent;
        
        if (userType != null && userType.equals("doctor")) {
            intent = new Intent(this, HomeDoctorActivity.class);
        } else {
            intent = new Intent(this, HomeActivity.class);
        }
        
        startActivity(intent);
    }

    private void init() {
        Animation animation =  AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.main.startAnimation(animation);
        binding.mess.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MessActivity.class);
            startActivity(intent);
        });
        binding.home.setOnClickListener(v -> {
            navigateToHome();
        });
        binding.booking.setOnClickListener(v -> {
            // Check user type to determine which booking activity to navigate to
            String userType = userData.getData("userType");
            Intent intent;
            
            if (userType != null && userType.equals("doctor")) {
                intent = new Intent(ProfileActivity.this, BookingDoctorActivity.class);
            } else {
                intent = new Intent(ProfileActivity.this, BookingActivity.class);
            }
            
            startActivity(intent);
        });
    }
    
    private void showEditProfileDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(dialogBinding.getRoot());
        
        // Pre-populate fields with current data
        dialogBinding.edtName.setText(userData.getData("name"));
        dialogBinding.edtEmail.setText(userData.getData("email"));
        dialogBinding.edtPhone.setText(userData.getData("phone"));
        
        // Set close button listener
        dialogBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        
        // Set save button listener
        dialogBinding.btnSave.setOnClickListener(v -> {
            String name = dialogBinding.edtName.getText().toString().trim();
            String email = dialogBinding.edtEmail.getText().toString().trim();
            String phone = dialogBinding.edtPhone.getText().toString().trim();
            
            // Validate inputs
            if(name.isEmpty()) {
                dialogBinding.edtName.setError("Name is required");
                dialogBinding.edtName.requestFocus();
                return;
            }
            
            if(email.isEmpty()) {
                dialogBinding.edtEmail.setError("Email is required");
                dialogBinding.edtEmail.requestFocus();
                return;
            }
            
            if(phone.isEmpty()) {
                dialogBinding.edtPhone.setError("Phone number is required");
                dialogBinding.edtPhone.requestFocus();
                return;
            }
            
            // Update local storage
            userData.saveData("name", name);
            userData.saveData("email", email);
            userData.saveData("phone", phone);
            
            // Update Firebase database
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userData.getData("id"))
                    .child("name")
                    .setValue(name);
                    
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userData.getData("id"))
                    .child("email")
                    .setValue(email);
                    
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userData.getData("id"))
                    .child("phone")
                    .setValue(phone);
            
            // Update UI
            binding.tvName.setText(name);
            binding.tvEmail.setText(email);
            binding.tvPhone.setText(phone);
            
            // Close dialog and show success message
            bottomSheetDialog.dismiss();
            TipDialog.show(this, "Thông tin đã được lưu", TipDialog.TYPE.SUCCESS);
        });
        
        bottomSheetDialog.show();
    }
}