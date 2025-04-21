package dev.edu.app.Screen.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.FirebaseDatabase;
import com.kongzue.dialogx.dialogs.TipDialog;

import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityProfileBinding;
import dev.edu.doctorappointment.databinding.DialogChagepassBinding;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;
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

        init();

        binding.logout.setOnClickListener(v -> {
            Animation animation =  AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            binding.main.startAnimation(animation);
            new android.os.Handler().postDelayed(() -> {
                new dev.edu.app.Model.UserData(this).clearData();
                startActivity(new android.content.Intent(this, dev.edu.app.Screen.Splash.MainActivity.class));
                finish();
            }, 500);
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

        binding.tvName.setText(new dev.edu.app.Model.UserData(this).getData("name"));
        binding.tvEmail.setText(new dev.edu.app.Model.UserData(this).getData("email"));
        binding.tvPhone.setText(new dev.edu.app.Model.UserData(this).getData("phone"));

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
                String password = new dev.edu.app.Model.UserData(this).getData("password");
                if(!oldPass.equals(password)){
                    dialogChagepassBinding.edtOldPass.setError("Old password is incorrect");
                    dialogChagepassBinding.edtOldPass.requestFocus();
                    return;
                }
                new dev.edu.app.Model.UserData(this).saveData("password", newPass);
                FirebaseDatabase.getInstance().getReference("Users").child(new dev.edu.app.Model.UserData(this).getData("id")).child("password").setValue(newPass);
                bottomSheetDialog.dismiss();
                TipDialog.show(this, "Change password success", TipDialog.TYPE.SUCCESS);
            });
            bottomSheetDialog.show();

        });
    }


    private void init() {
        binding.textView4.setText("Profile");
        Animation animation =  AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.main.startAnimation(animation);
        binding.mess.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MessActivity.class);
            startActivity(intent);
        });
        binding.home.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
        });
        binding.booking.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, BookingActivity.class);
            startActivity(intent);
        });
    }
}