package dev.edu.doctorappointment.Screen.Admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class UserManagementActivity extends AppCompatActivity implements AdapterUser.OnDeleteClickListener {
    private static final String TAG = "UserManagementActivity";
    private ActivityUserManagementBinding binding;
    private AdapterUser adapter;
    private List<UserModel> userList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup back button
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");
        Log.d(TAG, "Firebase reference path: " + usersRef.toString());

        // Initialize RecyclerView
        userList = new ArrayList<>();
        adapter = new AdapterUser(userList, this);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Starting to load users...");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Data snapshot received: " + dataSnapshot.getChildrenCount() + " items");
                userList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Log.d(TAG, "Processing user data: " + snapshot.getValue());
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            user.setKeyID(snapshot.getKey());
                            userList.add(user);
                            Log.d(TAG, "Added user: " + user.getName());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing user data: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "Total users loaded: " + userList.size());
                adapter.notifyDataSetChanged();
                updateEmptyState();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
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

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvUsers.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvUsers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteClick(UserModel user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUser(UserModel user) {
        usersRef.child(user.getKeyID()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa người dùng",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User deleted successfully: " + user.getKeyID());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting user: " + e.getMessage());
                });
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