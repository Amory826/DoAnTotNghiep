package dev.edu.doctorappointment.Screen.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdminManagementAdapter;
import dev.edu.doctorappointment.Model.ManagementItem;
import dev.edu.doctorappointment.R;
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

        setupRecyclerView();
        setupNavigation();
        initAnimation();
    }

    private void setupRecyclerView() {
        managementItems = new ArrayList<>();
        adapter = new AdminManagementAdapter(managementItems);
        
        binding.rvManagement.setAdapter(adapter);
        binding.rvManagement.setHasFixedSize(true);
        binding.rvManagement.setLayoutManager(new GridLayoutManager(this, 2));

        // Add management items
        loadManagementItems();
    }

    private void loadManagementItems() {
        // Services management items
        managementItems.add(new ManagementItem(
            "Thêm dịch vụ",
            R.drawable.ic_add,
            () -> startActivity(new Intent(this, ServiceManagementActivity.class))
        ));
        managementItems.add(new ManagementItem(
            "Danh sách dịch vụ",
            R.drawable.cardiology,
            () -> startActivity(new Intent(this, ServiceListActivity.class))
        ));

        // Appointments management items
        managementItems.add(new ManagementItem(
            "Lịch đặt khám mới",
            R.drawable.booking,
            () -> startActivity(new Intent(this, NewAppointmentsActivity.class))
        ));
        managementItems.add(new ManagementItem(
            "Lịch sử đặt khám",
            R.drawable.history,
            () -> startActivity(new Intent(this, AppointmentHistoryActivity.class))
        ));

        // Users management items
        managementItems.add(new ManagementItem(
            "Quản lý người dùng",
            R.drawable.profile,
            () -> startActivity(new Intent(this, UserManagementActivity.class))
        ));
        managementItems.add(new ManagementItem(
            "Thống kê",
            R.drawable.statistics,
            () -> startActivity(new Intent(this, StatisticsActivity.class))
        ));

        adapter.notifyDataSetChanged();
    }

    private void setupNavigation() {
        binding.btnServices.setOnClickListener(v -> 
            startActivity(new Intent(this, ServiceManagementActivity.class)));
        
        binding.btnAppointments.setOnClickListener(v -> 
            startActivity(new Intent(this, AppointmentManagementActivity.class)));
        
        binding.btnUsers.setOnClickListener(v -> 
            startActivity(new Intent(this, UserManagementActivity.class)));
    }

    private void initAnimation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.main.startAnimation(animation);
    }
} 