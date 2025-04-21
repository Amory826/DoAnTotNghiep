package dev.edu.app.Screen.Splash;

import android.os.Bundle;
import android.view.animation.Animation;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivitySplash2Binding;

public class SplashActivity2 extends AppCompatActivity {
    ActivitySplash2Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplash2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.login.setOnClickListener(v -> {
            Animation animation =  android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            binding.main.startAnimation(animation);
            startActivity(new android.content.Intent(this, dev.edu.app.Screen.LoginRegister.LoginActivity.class));
        });

        binding.signup.setOnClickListener(v -> {
            Animation animation =  android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            binding.main.startAnimation(animation);
            startActivity(new android.content.Intent(this, dev.edu.app.Screen.LoginRegister.RegisterActivity.class));
        });
    }
}