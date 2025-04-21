package dev.edu.app.Screen.Home;

import android.os.Bundle;
import android.view.View;

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

import dev.edu.app.Adapter.AdapterPayment;
import dev.edu.app.Model.PaymentModel;
import dev.edu.app.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityPayment3Binding;

public class Payment3Activity extends AppCompatActivity {
    ActivityPayment3Binding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Payments");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPayment3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }
                List<PaymentModel> paymentModels = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PaymentModel paymentModel = dataSnapshot.getValue(PaymentModel.class);
                    assert paymentModel != null;
                    if (paymentModel.getUserId().equals(new UserData(Payment3Activity.this).getData("id"))) {
                        paymentModels.add(paymentModel);
                    }

                    //sắp xếp theo thời gian
                    paymentModels.sort((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));

                    AdapterPayment adapterPayment = new AdapterPayment(paymentModels);
                    binding.rvPayment.setAdapter(adapterPayment);
                    binding.rvPayment.setHasFixedSize(true);
                    binding.rvPayment.setLayoutManager(new LinearLayoutManager(Payment3Activity.this));
                    adapterPayment.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void back(View view) {
        finish();
    }
}