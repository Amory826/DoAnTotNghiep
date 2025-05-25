package dev.edu.doctorappointment.Screen.Admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdapterUser;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityUserManagementBinding;

public class UserManagementActivity extends AppCompatActivity {
    private ActivityUserManagementBinding binding;
    private AdapterUser adapter;
    private List<UserModel> userList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize RecyclerView
        userList = new ArrayList<>();
        adapter = new AdapterUser(userList, this::showUserOptions);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);

        // Setup search functionality
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup chip group filters
        binding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                loadUsers();
            } else if (checkedId == R.id.chipActive) {
//                filterByStatus(true);
            } else if (checkedId == R.id.chipBlocked) {
//                filterByStatus(false);
            }
        });

        // Load initial data
        loadUsers();
    }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                int totalUsers = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null) {
                        user.setKeyID(snapshot.getKey());
                        userList.add(user);
                        totalUsers++;
                    }
                }

                // Update statistics
                binding.tvTotalUsers.setText(String.valueOf(totalUsers));

                adapter.notifyDataSetChanged();
                updateEmptyState();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserManagementActivity.this,
                        "Lỗi: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterUsers(String query) {
        List<UserModel> filteredList = new ArrayList<>();
        for (UserModel user : userList) {
            if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                    user.getPhone().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        adapter.setFilteredList(filteredList);
        updateEmptyState();
    }

//    private void filterByStatus(boolean isActive) {
//        List<UserModel> filteredList = new ArrayList<>();
//        for (UserModel user : userList) {
//            if (user.isActive() == isActive) {
//                filteredList.add(user);
//            }
//        }
//        adapter.setFilteredList(filteredList);
//        updateEmptyState();
//    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvUsers.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvUsers.setVisibility(View.VISIBLE);
        }
    }

    private void showUserOptions(UserModel user, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.admin_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
//            if (itemId == R.id.action_delete) {
//                confirmDeleteUser(user);
//                return true;
//            }
            return false;
        });

        popup.show();
    }

    private void confirmDeleteUser(UserModel user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUser(UserModel user) {
        usersRef.child(user.getKeyID()).removeValue()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Đã xóa người dùng",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}