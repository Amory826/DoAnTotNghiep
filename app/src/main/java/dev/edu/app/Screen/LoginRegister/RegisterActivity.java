package dev.edu.app.Screen.LoginRegister;

import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;

import java.util.ArrayList;

import dev.edu.app.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
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

        binding.login.setOnClickListener(v -> {
            Animation animation =  android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            binding.main.startAnimation(animation);
            finish();
        });

        binding.register.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();
            String name = binding.fullname.getText().toString();
            String phone = binding.phone.getText().toString();
            String rePassword = binding.repassword.getText().toString();
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
            if(rePassword.isEmpty()){
                binding.repassword.setError("Re-Password is required");
                binding.repassword.requestFocus();
                return;
            }
            if(name.isEmpty()){
                binding.fullname.setError("Full Name is required");
                binding.fullname.requestFocus();
                return;
            }
            if(phone.isEmpty()){
                binding.phone.setError("Phone is required");
                binding.phone.requestFocus();
                return;
            }
            if(!password.equals(rePassword)){
                binding.repassword.setError("Password not match");
                binding.repassword.requestFocus();
                return;
            }

            WaitDialog.show(this, "Registering...");

            UserModel userModel = new UserModel();
            userModel.setEmail(email);
            userModel.setPassword(password);
            userModel.setName(name);
            userModel.setPhone(phone);
            userModel.setKeyID(myRef.push().getKey());
            userModel.setFavoriteDoctors(new ArrayList<>());
            myRef.child(userModel.getKeyID()).setValue(userModel).addOnCompleteListener(task -> {
                WaitDialog.dismiss();
                if(task.isSuccessful()){
                    TipDialog.show(this, "Success", TipDialog.TYPE.SUCCESS);
                    new Handler().postDelayed(this::finish, 2000);
                }
            });
        });

    }
}