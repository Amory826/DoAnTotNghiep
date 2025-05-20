package dev.edu.doctorappointment.Screen.LoginRegister;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Screen.Home.HomeDoctorActivity;
import dev.edu.doctorappointment.Screen.Home.HomeActivity;
import dev.edu.doctorappointment.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private static final String TAG = "LoginActivity";

    private void saveFCMToken(String userId, String userType) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d(TAG, "New token for " + userType + ": " + token);

                        DatabaseReference tokensRef = FirebaseDatabase.getInstance()
                                .getReference("UserTokens")
                                .child(userType)
                                .child(userId);

                        tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<String> tokens = new ArrayList<>();
                                
                                if (snapshot.exists()) {
                                    for (DataSnapshot tokenSnapshot : snapshot.getChildren()) {
                                        String existingToken = tokenSnapshot.getValue(String.class);
                                        if (existingToken != null && !existingToken.equals(token)) {
                                            tokens.add(existingToken);
                                            Log.d(TAG, "Existing token: " + existingToken);
                                        }
                                    }
                                }
                                
                                tokens.add(token);
                                Log.d(TAG, "Saving " + tokens.size() + " tokens for " + userType);
                                
                                tokensRef.setValue(tokens)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Tokens saved successfully for " + userType))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error saving tokens for " + userType, e));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error getting tokens for " + userType, error.toException());
                            }
                        });
                    } else {
                        Log.e(TAG, "Failed to get FCM token for " + userType, task.getException());
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.back.setOnClickListener(v -> {
            Animation animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            binding.main.startAnimation(animation);
            finish();
        });

        binding.register.setOnClickListener(v -> {
            Animation animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            binding.main.startAnimation(animation);
            startActivity(new android.content.Intent(this, dev.edu.doctorappointment.Screen.LoginRegister.RegisterActivity.class));
        });

        binding.login.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();
            if(email.isEmpty()){
                binding.email.setError("Vui lòng nhập email");
                binding.email.requestFocus();
                return;
            }
            if(password.isEmpty()){
                binding.password.setError("Vui lòng nhập mật khẩu");
                binding.password.requestFocus();
                return;
            }
            WaitDialog.show(this, "Đang tải...");

            Query query = myRef.orderByChild("email").equalTo(email);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Log.d(TAG, "User info: " + query);
                        UserModel userModel = snapshot.getChildren().iterator().next().getValue(UserModel.class);
                        assert userModel != null;
                        String pass = userModel.getPassword();
                        if(pass.equals(password)){
                            WaitDialog.dismiss();
                            TipDialog.show(LoginActivity.this, "Đăng nhập thành công", TipDialog.TYPE.SUCCESS);
                            
                            // Save user data
                            dev.edu.doctorappointment.Model.UserData userData = new dev.edu.doctorappointment.Model.UserData(LoginActivity.this);
                            userData.saveData("id", userModel.getKeyID());
                            userData.saveData("email", email);
                            userData.saveData("password", password);
                            userData.saveData("isLogin", "true");
                            userData.saveData("name", userModel.getName());
                            userData.saveData("phone", userModel.getPhone());
                            userData.saveData("userType", "user");

                            // Save FCM token
                            saveFCMToken(userModel.getKeyID(), "user");

                            // Navigate to home
                            new Handler().postDelayed(() -> {
                                startActivity(new android.content.Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            }, 1000L);
                        } else {
                            WaitDialog.dismiss();
                            binding.password.setError("Sai mật khẩu");
                            binding.password.requestFocus();
                        }
                    } else {
                        WaitDialog.dismiss();
                        binding.email.setError("User does not exist");
                        binding.email.requestFocus();
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    WaitDialog.dismiss();
                }
            });
        });

        binding.login.setOnLongClickListener(v -> {
            String doctorId = binding.email.getText().toString();
            String password = binding.password.getText().toString();
            
            if(doctorId.isEmpty()) {
                binding.email.setError("Vui lòng nhập ID bác sĩ");
                binding.email.requestFocus();
                return true;
            }
            
            if(password.isEmpty()) {
                binding.password.setError("Vui lòng nhập mật khẩu");
                binding.password.requestFocus();
                return true;
            }
            
            WaitDialog.show(LoginActivity.this, "Đang tải...");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Doctors");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean doctorFound = false;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        DoctorsModel doctorsModel = ds.getValue(DoctorsModel.class);
                        if (doctorsModel != null && doctorsModel.getDoctorId().equals(doctorId)) {
                            doctorFound = true;
                            
                            if (doctorsModel.getPassword() != null && doctorsModel.getPassword().equals(password)) {
                                WaitDialog.dismiss();
                                TipDialog.show(LoginActivity.this, "Đăng nhập bác sĩ thành công", TipDialog.TYPE.SUCCESS);
                                
                                // Save doctor data
                                dev.edu.doctorappointment.Model.UserData userData = new dev.edu.doctorappointment.Model.UserData(LoginActivity.this);
                                userData.saveData("id", doctorsModel.getDoctorId());
                                userData.saveData("name", doctorsModel.getName());
                                userData.saveData("userType", "doctor");
                                userData.saveData("isLogin", "true");
                                userData.saveData("password", password);
                                userData.saveData("clinicName", doctorsModel.getClinicName());
                                userData.saveData("profilePicture", doctorsModel.getProfilePicture());
                                userData.saveData("gender", doctorsModel.getGender());
                                userData.saveData("birthYear", String.valueOf(doctorsModel.getBirthYear()));

                                // Save FCM token
                                saveFCMToken(doctorsModel.getDoctorId(), "doctor");

                                // Navigate to home
                                new Handler().postDelayed(() -> {
                                    startActivity(new android.content.Intent(LoginActivity.this, HomeDoctorActivity.class));
                                    finish();
                                }, 1000L);
                            } else {
                                WaitDialog.dismiss();
                                binding.password.setError("Mật khẩu không chính xác");
                                binding.password.requestFocus();
                            }
                            break;
                        }
                    }
                    if (!doctorFound) {
                        WaitDialog.dismiss();
                        TipDialog.show(LoginActivity.this, "ID bác sĩ không tồn tại", TipDialog.TYPE.ERROR);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    WaitDialog.dismiss();
                    TipDialog.show(LoginActivity.this, "Lỗi kết nối", TipDialog.TYPE.ERROR);
                }
            });
            return true;
        });
    }
}