package dev.edu.app.Screen.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityPayment2Binding;

public class Payment2Activity extends AppCompatActivity {
    ActivityPayment2Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPayment2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(getIntent().hasExtra("doctor")) {
            binding.tvDoctorname.setText(getIntent().getStringExtra("doctor"));
        }
        if(getIntent().hasExtra("date")) {
            binding.tvDate.setText(getIntent().getStringExtra("date"));
        }

    }

    public void done(View view) {
        Intent intent = new Intent(Payment2Activity.this, HomeActivity.class);
        startActivity(intent);
    }
}