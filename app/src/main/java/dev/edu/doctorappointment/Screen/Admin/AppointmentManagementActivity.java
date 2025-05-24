package dev.edu.doctorappointment.Screen.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import dev.edu.doctorappointment.databinding.ActivityAppointmentManagementBinding;

public class AppointmentManagementActivity extends AppCompatActivity {
    private ActivityAppointmentManagementBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupNavigationCards();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupNavigationCards() {
        binding.cardNewAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewAppointmentsActivity.class);
            startActivity(intent);
        });

        binding.cardAppointmentHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppointmentHistoryActivity.class);
            startActivity(intent);
        });

        // Thêm hiệu ứng khi nhấn vào card
        setupCardClickEffect(binding.cardNewAppointments);
        setupCardClickEffect(binding.cardAppointmentHistory);
    }

    private void setupCardClickEffect(CardView cardView) {
        cardView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    cardView.setCardElevation(2f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    cardView.setCardElevation(8f);
                    break;
            }
            return false;
        });
    }
} 