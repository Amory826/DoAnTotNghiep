package dev.edu.doctorappointment.Screen.LoginRegister;

import android.os.Bundle;
import android.os.Handler;
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
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;

import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
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
            Animation animation =  android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            binding.main.startAnimation(animation);
            finish();
        });

        binding.register.setOnClickListener(v -> {
            Animation animation =  android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            binding.main.startAnimation(animation);
            startActivity(new android.content.Intent(this, dev.edu.doctorappointment.Screen.LoginRegister.RegisterActivity.class));
        });

        binding.login.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();
            if(email.isEmpty()){
                binding.email.setError("Email is required");
                binding.email.requestFocus();
                return;
            }
            if(password.isEmpty()){
                binding.password.setError("Password is required");
                binding.password.requestFocus();
                return;
            }
            WaitDialog.show(this, "Loading...");

            Query query = myRef.orderByChild("email").equalTo(email);

            query.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        UserModel userModel =  snapshot.getChildren().iterator().next().getValue(UserModel.class);
                        assert userModel != null;
                        String pass = userModel.getPassword();
                        if(pass.equals(password)){
                            WaitDialog.dismiss();
                            TipDialog.show(LoginActivity.this, "Login Success", TipDialog.TYPE.SUCCESS);
                            new Handler().postDelayed(() -> {
                                dev.edu.doctorappointment.Model.UserData userData = new dev.edu.doctorappointment.Model.UserData(LoginActivity.this);
                                userData.saveData("id", userModel.getKeyID());
                                userData.saveData("email", email);
                                userData.saveData("password", password);
                                userData.saveData("isLogin", "true");
                                userData.saveData("name", userModel.getName());
                                userData.saveData("phone", userModel.getPhone());
                                startActivity(new android.content.Intent(LoginActivity.this, dev.edu.doctorappointment.Screen.Home.HomeActivity.class));
                                finish();
                            }, 1000L);
                        }else{
                            WaitDialog.dismiss();
                            binding.password.setError("Wrong Password");
                            binding.password.requestFocus();
                        }
                    }else{
                        WaitDialog.dismiss();
                        binding.email.setError("User does not exist");
                        binding.email.requestFocus();
                    }
                }
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    WaitDialog.dismiss();
                }
            });
        });

        binding.login.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String token = binding.email.getText().toString();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Doctors");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            DoctorsModel doctorsModel = ds.getValue(DoctorsModel.class);
                            if (doctorsModel != null && doctorsModel.getDoctorId().equals(token)) {
                                WaitDialog.dismiss();
                                TipDialog.show(LoginActivity.this, "Login Success", TipDialog.TYPE.SUCCESS);
                                new Handler().postDelayed(() -> {
                                    dev.edu.doctorappointment.Model.UserData userData = new dev.edu.doctorappointment.Model.UserData(LoginActivity.this);
                                    userData.saveData("id", doctorsModel.getDoctorId());
                                    userData.saveData("name", doctorsModel.getName());
                                    startActivity(new android.content.Intent(LoginActivity.this, dev.edu.doctorappointment.Screen.Home.MessActivity.class));
                                    finish();
                                }, 1000L);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                return false;
            }
        });


    }
}