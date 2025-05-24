package dev.edu.doctorappointment.Screen.Admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.edu.doctorappointment.Adapter.UserAdapter;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.databinding.ActivityUserManagementBinding;

public class UserManagementActivity extends AppCompatActivity {
    private ActivityUserManagementBinding binding;
    private UserAdapter adapter;
    private List<UserModel> allUsers;
    private List<UserModel> filteredUsers;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupChipGroup();
        loadUsers();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        allUsers = new ArrayList<>();
        filteredUsers = new ArrayList<>();
        adapter = new UserAdapter(filteredUsers,
            user -> toggleUserStatus(user),
            user -> showDeleteConfirmation(user)
        );
        
        binding.rvUsers.setAdapter(adapter);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userRef = database.getReference("Users");
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChipGroup() {
        binding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> filterUsers());
    }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    UserModel user = data.getValue(UserModel.class);
                    if (user != null) {
                        user.setId(data.getKey());
                        allUsers.add(user);
                    }
                }
                filterUsers();
                updateStatistics();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserManagementActivity.this,
                    "Lỗi: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterUsers() {
        String searchQuery = binding.etSearch.getText().toString().toLowerCase();
        Chip selectedChip = findViewById(binding.chipGroup.getCheckedChipId());
        String filter = selectedChip != null ? selectedChip.getText().toString() : "Tất cả";

        filteredUsers.clear();
        filteredUsers.addAll(allUsers.stream()
            .filter(user -> user.getName().toLowerCase().contains(searchQuery))
            .filter(user -> {
                switch (filter) {
                    case "Đang hoạt động":
                        return !user.isBlocked();
                    case "Đã khóa":
                        return user.isBlocked();
                    default:
                        return true;
                }
            })
            .collect(Collectors.toList()));

        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        binding.tvEmpty.setVisibility(filteredUsers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateStatistics() {
        int totalUsers = allUsers.size();
        int activeUsers = (int) allUsers.stream().filter(user -> !user.isBlocked()).count();
        int blockedUsers = totalUsers - activeUsers;

        binding.tvTotalUsers.setText(String.valueOf(totalUsers));
        binding.tvActiveUsers.setText(String.valueOf(activeUsers));
        binding.tvBlockedUsers.setText(String.valueOf(blockedUsers));
    }

    private void toggleUserStatus(UserModel user) {
        binding.progressBar.setVisibility(View.VISIBLE);
        user.setBlocked(!user.isBlocked());
        
        userRef.child(user.getId()).setValue(user)
            .addOnSuccessListener(unused -> {
                String message = user.isBlocked() ? "Đã khóa người dùng" : "Đã mở khóa người dùng";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            });
    }

    private void showDeleteConfirmation(UserModel user) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa người dùng")
            .setMessage("Bạn có chắc chắn muốn xóa người dùng này?")
            .setPositiveButton("Xóa", (dialog, which) -> deleteUser(user))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteUser(UserModel user) {
        binding.progressBar.setVisibility(View.VISIBLE);
        userRef.child(user.getId()).removeValue()
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            });
    }
} 