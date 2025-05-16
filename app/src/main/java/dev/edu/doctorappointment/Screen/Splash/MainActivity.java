package dev.edu.doctorappointment.Screen.Splash;

import android.content.Intent;
import android.os.Bundle;

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
import dev.edu.doctorappointment.Screen.Home.HomeDoctorActivity;
import dev.edu.doctorappointment.Screen.Home.HomeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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
                } else {
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
    }
}