package dev.edu.doctorappointment.Screen.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.squareup.picasso.Picasso;

import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.PaymentModel;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityPaymentBinding;

public class PaymentActivity extends AppCompatActivity {
    AppointmentModel appointmentModel;
    ActivityPaymentBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ServicesRef = database.getReference("Services");
    DatabaseReference myRef = database.getReference("Appointments");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent().hasExtra("appointment")) {
            appointmentModel = (AppointmentModel) getIntent().getSerializableExtra("appointment");
            ServicesRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ServiceModel serviceModel = dataSnapshot.getValue(ServiceModel.class);
                        if (serviceModel.getName().equals(appointmentModel.getServiceId())) {
                            binding.tvAmount.setText(serviceModel.getPrice() + " VND");
                            binding.tvMoney.setText(serviceModel.getPrice()  + " VND");
                            binding.tvAddress.setText(appointmentModel.getClinicName());
                            binding.tvService.setText(appointmentModel.getServiceId());
                            binding.tvDate.setText(appointmentModel.getAppointmentTime() + "," + appointmentModel.getAppointmentSlot());
                            binding.tvTotal.setText(serviceModel.getPrice() + " VND");
                            binding.tvBooking.setText(new UserData(PaymentActivity.this).getData("name"));
                            FirebaseDatabase.getInstance().getReference("Doctors").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        DoctorsModel doctorsModel = dataSnapshot.getValue(DoctorsModel.class);
                                        if (doctorsModel.getDoctorId().equals(appointmentModel.getDoctorId())) {
                                            binding.tvName.setText(doctorsModel.getName());
                                            if(!doctorsModel.getProfilePicture().isEmpty()){
                                                Picasso.get().load(doctorsModel.getProfilePicture()).into(binding.ivAvatar);
                                            }else{
                                                if(doctorsModel.getGender().equals("Nam")){
                                                    binding.ivAvatar.setImageResource(R.drawable.nam);
                                                }else{
                                                    binding.ivAvatar.setImageResource(R.drawable.nu);
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            binding.btnPay.setOnClickListener(v -> {
                WaitDialog.show(this, "Processing...");
                appointmentModel.setStatus("Paid");
                PaymentModel paymentModel = new PaymentModel();
                DatabaseReference myRefpm = FirebaseDatabase.getInstance().getReference("Payments");
                paymentModel.setPaymentId(myRefpm.push().getKey());
                paymentModel.setUserId(new UserData(PaymentActivity.this).getData("id"));
                paymentModel.setAppointmentId(appointmentModel.getAppointmentId());
                paymentModel.setAmount(binding.tvAmount.getText().toString());
                paymentModel.setStatus("Success");
                paymentModel.setTimestamp(String.valueOf(System.currentTimeMillis()));
                myRefpm.child(paymentModel.getPaymentId()).setValue(paymentModel);
                myRef.child(appointmentModel.getAppointmentId()).setValue(appointmentModel).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WaitDialog.dismiss();
                        TipDialog.show(this, "Thanh toán thành công", TipDialog.TYPE.SUCCESS);
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(PaymentActivity.this, Payment2Activity.class);
                            intent.putExtra("doctor", binding.tvName.getText().toString());
                            intent.putExtra("date", binding.tvDate.getText().toString());
                            startActivity(intent);
                            finish();
                        }, 2000L);
                    }
                });
            });
        }
    }

    public void back(View view) {
        finish();
    }
}