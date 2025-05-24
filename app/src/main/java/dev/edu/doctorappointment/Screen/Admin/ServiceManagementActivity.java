package dev.edu.doctorappointment.Screen.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.ServiceAdapter;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.databinding.ActivityServiceManagementBinding;
import dev.edu.doctorappointment.databinding.DialogAddEditServiceBinding;

public class ServiceManagementActivity extends AppCompatActivity {
    private ActivityServiceManagementBinding binding;
    private ServiceAdapter adapter;
    private List<ServiceModel> services;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference serviceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupFab();
        loadServices();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        services = new ArrayList<>();
        adapter = new ServiceAdapter(services, 
            service -> showEditDialog(service),
            service -> showDeleteDialog(service)
        );
        
        binding.rvServices.setAdapter(adapter);
        binding.rvServices.setLayoutManager(new LinearLayoutManager(this));
        serviceRef = database.getReference("Services");
    }

    private void setupFab() {
        binding.fabAddService.setOnClickListener(v -> showAddDialog());
    }

    private void loadServices() {
        binding.progressBar.setVisibility(View.VISIBLE);
        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                services.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ServiceModel service = data.getValue(ServiceModel.class);
                    if (service != null) {
                        service.setId(data.getKey());
                        services.add(service);
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyView();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServiceManagementActivity.this, 
                    "Lỗi: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateEmptyView() {
        binding.tvEmpty.setVisibility(services.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddDialog() {
        DialogAddEditServiceBinding dialogBinding = DialogAddEditServiceBinding.inflate(getLayoutInflater());
        
        new AlertDialog.Builder(this)
            .setTitle("Thêm dịch vụ mới")
            .setView(dialogBinding.getRoot())
            .setPositiveButton("Thêm", (dialog, which) -> {
                String name = dialogBinding.etServiceName.getText().toString();
                String description = dialogBinding.etServiceDescription.getText().toString();
                double price = Double.parseDouble(dialogBinding.etServicePrice.getText().toString());

                ServiceModel newService = new ServiceModel(name, description, price);
                addService(newService);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showEditDialog(ServiceModel service) {
        DialogAddEditServiceBinding dialogBinding = DialogAddEditServiceBinding.inflate(getLayoutInflater());
        
        dialogBinding.etServiceName.setText(service.getName());
        dialogBinding.etServiceDescription.setText(service.getDescription());
        dialogBinding.etServicePrice.setText(String.valueOf(service.getPrice()));

        new AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa dịch vụ")
            .setView(dialogBinding.getRoot())
            .setPositiveButton("Lưu", (dialog, which) -> {
                service.setName(dialogBinding.etServiceName.getText().toString());
                service.setDescription(dialogBinding.etServiceDescription.getText().toString());
                service.setPrice(Double.parseDouble(dialogBinding.etServicePrice.getText().toString()));

                updateService(service);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showDeleteDialog(ServiceModel service) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa dịch vụ")
            .setMessage("Bạn có chắc chắn muốn xóa dịch vụ này?")
            .setPositiveButton("Xóa", (dialog, which) -> deleteService(service))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void addService(ServiceModel service) {
        binding.progressBar.setVisibility(View.VISIBLE);
        serviceRef.push().setValue(service)
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, "Thêm dịch vụ thành công", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            });
    }

    private void updateService(ServiceModel service) {
        binding.progressBar.setVisibility(View.VISIBLE);
        serviceRef.child(service.getId()).setValue(service)
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            });
    }

    private void deleteService(ServiceModel service) {
        binding.progressBar.setVisibility(View.VISIBLE);
        serviceRef.child(service.getId()).removeValue()
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, "Xóa dịch vụ thành công", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            });
    }
} 