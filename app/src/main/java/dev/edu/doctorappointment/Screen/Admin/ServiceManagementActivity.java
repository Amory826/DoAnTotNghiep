package dev.edu.doctorappointment.Screen.Admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.edu.doctorappointment.Adapter.ServiceAdminAdapter;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.databinding.ActivityServiceManagementBinding;
import dev.edu.doctorappointment.databinding.DialogAddEditServiceBinding;

public class ServiceManagementActivity extends AppCompatActivity {
    private ActivityServiceManagementBinding binding;
    private ServiceAdminAdapter adapter;
    private List<ServiceModel> services;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference serviceRef;
    private DecimalFormat priceFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupPriceFormatter();
        setupToolbar();
        setupRecyclerView();
        setupFab();
        loadServices();
    }

    private void setupPriceFormatter() {
        priceFormatter = new DecimalFormat("#,###");
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        services = new ArrayList<>();
        adapter = new ServiceAdminAdapter(services,
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
                        service.setKeyID(data.getKey());
                        services.add(service);
                    }
                }
                adapter.updateServices(services);
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
        binding.rvServices.setVisibility(services.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupPriceEditText(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[,]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = priceFormatter.format(parsed);
                            current = formatted;
                            editText.setText(formatted);
                            editText.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    editText.addTextChangedListener(this);
                }
            }
        });
    }

    private void showAddDialog() {
        DialogAddEditServiceBinding dialogBinding = DialogAddEditServiceBinding.inflate(getLayoutInflater());
        setupPriceEditText(dialogBinding.etServicePrice);

        new AlertDialog.Builder(this)
                .setTitle("Thêm dịch vụ mới")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = dialogBinding.etServiceName.getText().toString();
                    String description = dialogBinding.etServiceDescription.getText().toString();
                    String price = dialogBinding.etServicePrice.getText().toString();

                    if (name.isEmpty() || description.isEmpty() || price.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ServiceModel newService = new ServiceModel();
                    newService.setName(name);
                    newService.setDescription(description);
                    newService.setPrice(price);

                    addService(newService);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditDialog(ServiceModel service) {
        DialogAddEditServiceBinding dialogBinding = DialogAddEditServiceBinding.inflate(getLayoutInflater());
        setupPriceEditText(dialogBinding.etServicePrice);

        dialogBinding.etServiceName.setText(service.getName());
        dialogBinding.etServiceDescription.setText(service.getDescription());
        dialogBinding.etServicePrice.setText(service.getPrice());

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa dịch vụ")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = dialogBinding.etServiceName.getText().toString();
                    String description = dialogBinding.etServiceDescription.getText().toString();
                    String price = dialogBinding.etServicePrice.getText().toString();

                    if (name.isEmpty() || description.isEmpty() || price.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    service.setName(name);
                    service.setDescription(description);
                    service.setPrice(price);

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
        serviceRef.child(service.getKeyID()).setValue(service)
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
        serviceRef.child(service.getKeyID()).removeValue()
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