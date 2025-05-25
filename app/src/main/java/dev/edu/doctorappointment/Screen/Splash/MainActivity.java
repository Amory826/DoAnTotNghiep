package dev.edu.doctorappointment.Screen.Splash;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Screen.Admin.AdminActivity;
import dev.edu.doctorappointment.Screen.Home.HomeDoctorActivity;
import dev.edu.doctorappointment.Screen.Home.HomeActivity;
import dev.edu.doctorappointment.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                getString(R.string.default_notification_channel_id),
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for appointment notifications");
            channel.enableLights(true);
            channel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new android.os.Handler().postDelayed(() -> {
            UserData userData = new UserData(this);
            String isLogin = userData.getData("isLogin");
            
            if(isLogin.equals("true")) {
                // Check user type to navigate to correct screen
                String userType = userData.getData("userType");
                Intent intent;
                
                if(userType != null && userType.equals("doctor")) {
                    // Doctor login
                    intent = new Intent(this, HomeDoctorActivity.class);
                } else if(userType != null && userType.equals("admin")){
                    intent = new Intent(this, AdminActivity.class);
                } else{
                    // Regular user login
                    intent = new Intent(this, HomeActivity.class);
                }
                
                startActivity(intent);
                finish();
                return;
            }
            
            // Not logged in, go to splash
            startActivity(new Intent(this, SplashActivity2.class));
            finish();
        }, 3000);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
}