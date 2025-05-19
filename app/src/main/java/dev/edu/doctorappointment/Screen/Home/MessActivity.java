package dev.edu.doctorappointment.Screen.Home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdapterUserChat;
import dev.edu.doctorappointment.Model.MessengerModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityMessBinding;

public class MessActivity extends AppCompatActivity {
    ActivityMessBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Message");
    DatabaseReference myRef1 = database.getReference("Doctor");
    private UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userData = new UserData(this);
        init();
        fetchMessage();
    }


    private void fetchMessage() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Messages");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }
                String currentUserId = userData.getData("id");
                List<String> chatUsers = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    MessengerModel messenger = data.getValue(MessengerModel.class);

                    if (messenger != null &&
                            (messenger.getIdSender().equals(currentUserId) || messenger.getIdReceiver().equals(currentUserId))) {
                        String chatUserId = messenger.getIdSender().equals(currentUserId) ? messenger.getIdReceiver() : messenger.getIdSender();
                        if (!chatUsers.contains(chatUserId)) {
                            chatUsers.add(chatUserId);
                        }
                    }
                }

                // Here you can update your UI with the list of chat users
                updateChatUserList(chatUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updateChatUserList(List<String> chatUsers) {
        // Update your UI with the list of chat users
        AdapterUserChat adapter = new AdapterUserChat(chatUsers);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setNestedScrollingEnabled(false);
        binding.recyclerView.setClipToPadding(false);
        binding.recyclerView.setClipChildren(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setItemAnimator(null);
    }

    // Helper method to navigate to the appropriate home screen based on user type
    private void navigateToHome() {
        String userType = userData.getData("userType");
        Intent intent;
        
        if (userType != null && userType.equals("doctor")) {
            intent = new Intent(this, HomeDoctorActivity.class);
        } else {
            intent = new Intent(this, HomeActivity.class);
        }
        
        startActivity(intent);
    }

    private void init() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        binding.main.startAnimation(animation);
        binding.booking.setOnClickListener(v -> {
            // Check user type to determine which booking activity to navigate to
            String userType = userData.getData("userType");
            Intent intent;
            
            if (userType != null && userType.equals("doctor")) {
                intent = new Intent(MessActivity.this, BookingDoctorActivity.class);
            } else {
                intent = new Intent(MessActivity.this, BookingActivity.class);
            }
            
            startActivity(intent);
        });
        binding.home.setOnClickListener(v -> {
            navigateToHome();
        });
        binding.profile.setOnClickListener(v -> {
            String userType = userData.getData("userType");
            Intent intent;
            
            if (userType != null && userType.equals("doctor")) {
                intent = new Intent(MessActivity.this, ProfileDoctorActivity.class);
            } else {
                intent = new Intent(MessActivity.this, ProfileActivity.class);
            }
            
            startActivity(intent);
        });
    }
}