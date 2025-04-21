package dev.edu.app.Screen.Splash;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dev.edu.app.Model.UserData;
import dev.edu.doctorappointment.R;

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
            String isLogin = new UserData(this) .getData("isLogin");
            if(isLogin.equals("true")){
                startActivity(new android.content.Intent(this, dev.edu.app.Screen.Home.HomeActivity.class));
                finish();
                return;
            }
            startActivity(new android.content.Intent(this, dev.edu.app.Screen.Splash.SplashActivity2.class));
            finish();
        }, 3000);

    }
}