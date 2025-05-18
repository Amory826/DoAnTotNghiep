package dev.edu.doctorappointment.Screen.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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

import java.util.HashMap;
import java.util.Map;

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
                appointmentModel.setStatus("Đã thanh toán");
                PaymentModel paymentModel = new PaymentModel();
                DatabaseReference myRefpm = FirebaseDatabase.getInstance().getReference("Payments");
                paymentModel.setPaymentId(myRefpm.push().getKey());
                paymentModel.setUserId(new UserData(PaymentActivity.this).getData("id"));
                paymentModel.setAppointmentId(appointmentModel.getAppointmentId());
                paymentModel.setAmount(binding.tvAmount.getText().toString());
                paymentModel.setStatus("Success");
                paymentModel.setTimestamp(String.valueOf(System.currentTimeMillis()));
                
                // First, save payment to database
                myRefpm.child(paymentModel.getPaymentId()).setValue(paymentModel).addOnCompleteListener(paymentTask -> {
                    if (paymentTask.isSuccessful()) {
                        Log.d("PaymentActivity", "Payment saved successfully");
                        
                        // Then, save appointment to database
                        myRef.child(appointmentModel.getAppointmentId()).setValue(appointmentModel).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("PaymentActivity", "Appointment saved successfully");
                                
                                // Only update booking count after appointment is saved
                                updateDoctorBookingCount(appointmentModel.getDoctorId(), 
                                        appointmentModel.getAppointmentTime(),
                                        appointmentModel.getAppointmentSlot());
                                
                                WaitDialog.dismiss();
                                TipDialog.show(this, "Thanh toán thành công", TipDialog.TYPE.SUCCESS);
                                new Handler().postDelayed(() -> {
                                    Intent intent = new Intent(PaymentActivity.this, Payment2Activity.class);
                                    intent.putExtra("doctor", binding.tvName.getText().toString());
                                    intent.putExtra("date", binding.tvDate.getText().toString());
                                    startActivity(intent);
                                    finish();
                                }, 2000L);
                            } else {
                                WaitDialog.dismiss();
                                TipDialog.show(this, "Lỗi khi lưu cuộc hẹn", TipDialog.TYPE.ERROR);
                                Log.e("PaymentActivity", "Failed to save appointment", task.getException());
                            }
                        });
                    } else {
                        WaitDialog.dismiss();
                        TipDialog.show(this, "Lỗi khi lưu thanh toán", TipDialog.TYPE.ERROR);
                        Log.e("PaymentActivity", "Failed to save payment", paymentTask.getException());
                    }
                });
            });
        }
    }

    public void back(View view) {
        finish();
    }

    private void updateDoctorBookingCount(String doctorId, String date, String timeSlot) {
        // Replace invalid characters in date for Firebase key (/, ., #, $, [, or ])
        String safeDate = date.replace("/", "_").replace(".", "_");

        Log.d("BookingUpdate", "Updating booking count for doctor: " + doctorId + ", date: " + safeDate + ", slot: " + timeSlot);

        // Skip unnecessary check and directly update the booking count
        // Since we're already in the payment completion flow, we should always update the count
        updateBookingCountInTransaction(doctorId, safeDate, timeSlot);
    }

    private void updateBookingCountInTransaction(String doctorId, String safeDate, String timeSlot) {
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Doctors").child(doctorId);

        Log.d("BookingUpdate", "Starting transaction for doctorId: " + doctorId);

        // Use a transaction to ensure data consistency
        doctorRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                DoctorsModel doctor = mutableData.getValue(DoctorsModel.class);

                if (doctor == null) {
                    Log.e("BookingUpdate", "Doctor data is null, transaction aborted");
                    return Transaction.success(mutableData);
                }

                Log.d("BookingUpdate", "Got doctor data: " + doctor.getName());

                // Get or initialize the bookingCountByDateTime map
                Map<String, Map<String, Integer>> bookingCounts = doctor.getBookingCountByDateTime();
                if (bookingCounts == null) {
                    bookingCounts = new HashMap<>();
                    Log.d("BookingUpdate", "Initialized new booking counts map");
                }

                // Get or initialize the time slots map for this date
                Map<String, Integer> timeSlots = bookingCounts.get(safeDate);
                if (timeSlots == null) {
                    timeSlots = new HashMap<>();
                    Log.d("BookingUpdate", "Initialized new time slots map for date: " + safeDate);
                }

                // For this date and time slot, set to 1 if it doesn't exist, otherwise increment
                Integer count = timeSlots.get(timeSlot);
                if (count == null) {
                    // First booking for this slot
                    timeSlots.put(timeSlot, 1);
                    Log.d("BookingUpdate", "First booking for this slot. Set count to 1");
                } else {
                    // Increment existing count
                    timeSlots.put(timeSlot, count + 1);
                    Log.d("BookingUpdate", "Incremented count from " + count + " to " + (count + 1));
                }

                // Update the nested maps
                bookingCounts.put(safeDate, timeSlots);
                doctor.setBookingCountByDateTime(bookingCounts);

                // Update the entire doctor object
                mutableData.setValue(doctor);
                Log.d("BookingUpdate", "Updated doctor data in transaction");

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    Log.e("BookingUpdate", "Firebase transaction failed: " + error.getMessage(), error.toException());
                } else {
                    Log.d("BookingUpdate", "Firebase transaction " + (committed ? "successful" : "aborted"));
                    if (committed) {
                        // Show success notification on the UI thread
                        runOnUiThread(() -> {
                        });
                    }
                }
            }
        });
    }
}