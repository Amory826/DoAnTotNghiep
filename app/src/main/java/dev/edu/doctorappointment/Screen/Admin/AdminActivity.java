package dev.edu.doctorappointment.Screen.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdminManagementAdapter;
import dev.edu.doctorappointment.Model.ManagementItem;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Screen.LoginRegister.LoginActivity;
import dev.edu.doctorappointment.databinding.ActivityAdminBinding;

public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;
    private AdminManagementAdapter adapter;
    private List<ManagementItem> managementItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupNavigationCards();
        setupRecyclerView();
        initAnimation();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Quản lý hệ thống");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavigationCards() {
        binding.cardServices.setOnClickListener(v -> {
            Intent intent = new Intent(this, ServiceManagementActivity.class);
            startActivity(intent);
        });

        binding.cardAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppointmentManagementActivity.class);
            startActivity(intent);
        });

        binding.cardUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserManagementActivity.class);
            startActivity(intent);
        });

        // Thêm hiệu ứng khi nhấn vào card
        setupCardClickEffect(binding.cardServices);
        setupCardClickEffect(binding.cardAppointments);
        setupCardClickEffect(binding.cardUsers);
    }

    private void setupCardClickEffect(MaterialCardView cardView) {
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

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void logout() {
        UserData userData = new UserData(this);
        userData.clearData();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupRecyclerView() {
        managementItems = new ArrayList<>();
        // Add management items
//        loadManagementItems();
    }

    private void setupNavigation() {
        binding.cardServices.setOnClickListener(v ->
            startActivity(new Intent(this, ServiceManagementActivity.class)));
        
        binding.cardAppointments.setOnClickListener(v -> 
            startActivity(new Intent(this, AppointmentManagementActivity.class)));
        
        binding.cardUsers.setOnClickListener(v ->
            startActivity(new Intent(this, UserManagementActivity.class)));
    }

    private void initAnimation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.cardUsers.startAnimation(animation);
    }
} 