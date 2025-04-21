package dev.edu.doctorappointment.Screen.Home;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Adapter.AdapterMessenger;
import dev.edu.doctorappointment.Model.MessengerModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityMessengerBinding;

public class MessengerActivity extends AppCompatActivity {

    ActivityMessengerBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Messages");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMessengerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        intent.putExtra("userId", user);
//        intent.putExtra("name", holder.binding.tvName.getText().toString());

        String userId = getIntent().getStringExtra("userId");
        String name = getIntent().getStringExtra("name");

        binding.tvName.setText(name);

        binding.back.setOnClickListener(v -> {
            finish();
        });

        binding.btnSend.setOnClickListener(v -> {
            String message = binding.etMessage.getText().toString();
            if (message.isEmpty()) {
                binding.etMessage.setError("Message is empty");
                return;
            }
            String key = myRef.push().getKey();
            MessengerModel _message = new MessengerModel();
            _message.setIdMessage(key);
            _message.setIdSender(new UserData(MessengerActivity.this).getData("id"));
            _message.setIdReceiver(userId);
            _message.setMessage(message);
            _message.setTimestamp(String.valueOf(System.currentTimeMillis()));
            _message.setType("text");
            myRef.child(key).setValue(_message);
            binding.etMessage.setText("");
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MessengerModel> messages = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessengerModel message = dataSnapshot.getValue(MessengerModel.class);
                    if (message.getIdSender().equals(new UserData(MessengerActivity.this).getData("id")) && message.getIdReceiver().equals(userId)) {
                        messages.add(message);
                    }
                    if (message.getIdSender().equals(userId) && message.getIdReceiver().equals(new UserData(MessengerActivity.this).getData("id"))) {
                        messages.add(message);
                    }
                }
                AdapterMessenger adapterMessage = new AdapterMessenger(messages, MessengerActivity.this);
                binding.recyclerView.setAdapter(adapterMessage);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MessengerActivity.this , LinearLayoutManager.VERTICAL, false);
                linearLayoutManager.setStackFromEnd(true);
                binding.recyclerView.setLayoutManager(linearLayoutManager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}