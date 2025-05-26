package dev.edu.doctorappointment.Screen.Home;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import dev.edu.doctorappointment.Adapter.AdapterWorking;
import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.MessengerModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ActivityDetailBinding;
import dev.edu.doctorappointment.databinding.DialogAppointmentBinding;

public class DetailActivity extends AppCompatActivity {
    ActivityDetailBinding binding;
    DoctorsModel doctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.ivBack.setOnClickListener(v -> finish()); // back

        binding.cvSend.setOnClickListener(v -> {
            String message = binding.etMessage.getText().toString();
            if (message.isEmpty()) {
                TipDialog.show(this, "Please enter message", TipDialog.TYPE.ERROR);
                return;
            }
            WaitDialog.show(this, "Sending...", 0.1f);
            WaitDialog.show(this, "Sending...", 0.3f);
            WaitDialog.show(this, "Sending...", 0.5f);
            WaitDialog.show(this, "Sending...", 0.7f);
            WaitDialog.show(this, "Sending...", 0.9f);
            MessengerModel messenger = new MessengerModel();
            messenger.setIdMessage(FirebaseDatabase.getInstance().getReference("Messages").push().getKey());
            messenger.setIdSender(new UserData(this).getData("id"));
            messenger.setIdReceiver(doctor.getDoctorId());
            messenger.setMessage(message);
            messenger.setTimestamp(String.valueOf(System.currentTimeMillis()));
            messenger.setType("text");
            FirebaseDatabase.getInstance().getReference("Messages")
                    .child(messenger.getIdMessage())
                    .setValue(messenger).addOnCompleteListener(task -> {
                        WaitDialog.dismiss();
                        if (task.isSuccessful()) {
                            TipDialog.show(this, "Message sent", TipDialog.TYPE.SUCCESS);
                        } else {
                            TipDialog.show(this, "Message failed", TipDialog.TYPE.ERROR);
                        }
                    });
        });

        if (getIntent().hasExtra("doctor")) {
            doctor = (DoctorsModel) getIntent().getSerializableExtra("doctor");

            // Debug the doctor model
            Log.d("DoctorModel", "Loaded doctor: " + doctor.getName() + ", ID: " + doctor.getDoctorId());
            Log.d("DoctorModel", "MaxBookingsPerSlot: " + doctor.getMaxBookingsPerSlot());

            // Debug bookingCountByDateTime
            AtomicReference<Map<String, Map<String, Integer>>> bookingCounts = new AtomicReference<>(doctor.getBookingCountByDateTime());
            if (bookingCounts.get() != null) {
                Log.d("DoctorModel", "BookingCountByDateTime size: " + bookingCounts.get().size());
                for (String date : bookingCounts.get().keySet()) {
                    Map<String, Integer> timeSlots = bookingCounts.get().get(date);
                    if (timeSlots != null) {
                        for (String time : timeSlots.keySet()) {
                            Log.d("DoctorModel", "Date: " + date + ", Time: " + time + ", Count: " + timeSlots.get(time));
                        }
                    }
                }
            } else {
                Log.d("DoctorModel", "BookingCountByDateTime is null");
            }

            binding.tvDoctorName.setText(doctor.getName());
            String services = "Chuyên khoa: ";
            for (String serviceId : doctor.getServices()) {
                services += serviceId + ", ";
            }
            services = services.replaceAll(", $", "");
            Log.d("12313", services);
            if (!doctor.getProfilePicture().isEmpty()) {
                Picasso.get().load(doctor.getProfilePicture()).into(binding.ivProfilePicture);
            } else {
                if (doctor.getGender().equals("Nam")) {
                    binding.ivProfilePicture.setImageResource(R.drawable.nam);
                } else {
                    binding.ivProfilePicture.setImageResource(R.drawable.nu);
                }
            }
            binding.tvSpecialty.setText(services);
            binding.tvBirthYear.setText(String.format("Năm sinh: %d", doctor.getBirthYear()));
            binding.tvGender.setText(String.format("Giới tính: %s", doctor.getGender()));
            binding.tvClinicName.setText(String.format("Vị trí: %s", doctor.getClinicName()));
            AdapterWorking adapterWorking = new AdapterWorking(doctor.getWorkingHours());
            binding.recyclerViewWorkingHours.setAdapter(adapterWorking);
            binding.recyclerViewWorkingHours.setHasFixedSize(true);
            binding.recyclerViewWorkingHours.setNestedScrollingEnabled(false);
            binding.recyclerViewWorkingHours.setClipToPadding(false);
            binding.recyclerViewWorkingHours.setClipChildren(false);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
            binding.recyclerViewWorkingHours.setLayoutManager(linearLayoutManager);
            binding.tvAboutDoctor.setText(doctor.getDescription());

//            if(doctor.isFavorite()) {
//                binding.ivFavorites.setImageResource(R.drawable.favoritesblue);
//            } else {
//                binding.ivFavorites.setImageResource(R.drawable.favoritesblue2);
//            }

//            binding.ivFavorites.setOnClickListener(v -> {
//                doctor.setFavorite(!doctor.isFavorite());
//                if(doctor.isFavorite()) {
//                    binding.ivFavorites.setImageResource(R.drawable.favoritesblue);
//                    //add
//                    FirebaseDatabase.getInstance().getReference().child("Users")
//                            .child(new UserData(this).getData("id"))
//                            .child("favoriteDoctors")
//                            .child(doctor.getDoctorId())
//                            .setValue(doctor);
//                } else {
//                    binding.ivFavorites.setImageResource(R.drawable.favoritesblue2);
//                    //remove
//                    FirebaseDatabase.getInstance().getReference().child("Users")
//                            .child(new UserData(this).getData("id"))
//                            .child("favoriteDoctors")
//                            .child(doctor.getDoctorId())
//                            .removeValue();
//                }
//            });


            binding.btnBooking.setOnClickListener(v -> {
                Dialog dialog = new Dialog(this);
                DialogAppointmentBinding dialogBinding = DialogAppointmentBinding.inflate(getLayoutInflater());
                dialog.setContentView(dialogBinding.getRoot());
                ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, doctor.getWorkingHours());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dialogBinding.spnTime.setAdapter(adapter);
                dialogBinding.spnTime.setSelection(0);
                DatePickerDialog datePickerDialog = getDatePickerDialog(dialogBinding, this);
                dialogBinding.tvDate.setOnClickListener(v1 -> datePickerDialog.show());
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctor.getServices());
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dialogBinding.spnService.setAdapter(adapter1);
                dialogBinding.spnService.setSelection(0);
                dialogBinding.btnBook.setOnClickListener(v1 -> {
                    String time = dialogBinding.spnTime.getSelectedItem().toString();
                    String date = dialogBinding.tvDate.getText().toString();

                    if (date.isEmpty() || date.equals("Choose Date")) {
                        dialogBinding.tvDate.setError("Vui lòng chọn ngày khám");
                        return;
                    }

                    if (time.isEmpty()) {
                        TipDialog.show(this, "Vui lòng chọn thời gian thực hiện", TipDialog.TYPE.ERROR);
                        return;
                    }

                    // Replace invalid characters for Firebase key
                    String safeDate = date.replace("/", "_").replace(".", "_");

                    // Check if the doctor has reached the maximum bookings for this time slot
                    bookingCounts.set(doctor.getBookingCountByDateTime());
                    if (bookingCounts.get() == null) {
                        bookingCounts.set(new HashMap<>());
                        doctor.setBookingCountByDateTime(bookingCounts.get());
                    }

                    Map<String, Integer> timeSlots = bookingCounts.get().get(safeDate);
                    int currentBookings = 0;

                    if (timeSlots != null && timeSlots.containsKey(time)) {
                        currentBookings = timeSlots.get(time);
                    }

                    if (currentBookings >= doctor.getMaxBookingsPerSlot()) {
                        dialog.dismiss();
                        TipDialog.show(this, "Số lượng đặt đã đạt số lượng đặt tối đa! Vui lòng chọn vào thời gian khác!”", TipDialog.TYPE.ERROR);
                        return;
                    }

                    WaitDialog.show(this, "Booking...", 0.1f);
                    WaitDialog.show(this, "Booking...", 0.3f);
                    WaitDialog.show(this, "Booking...", 0.5f);
                    WaitDialog.show(this, "Booking...", 0.7f);
                    WaitDialog.show(this, "Booking...", 0.9f);

                    // Remove the code that updates booking counts here
                    // The booking count will only be updated after successful payment

                    AppointmentModel appointment = new AppointmentModel();
                    appointment.setAppointmentId(FirebaseDatabase.getInstance().getReference("Appointments").push().getKey());
                    appointment.setUserId(new UserData(this).getData("id"));
                    appointment.setDoctorId(doctor.getDoctorId());
                    appointment.setClinicName(doctor.getClinicName());
                    appointment.setServiceId(dialogBinding.spnService.getSelectedItem().toString());
                    appointment.setAppointmentSlot(time);
                    appointment.setAppointmentTime(dialogBinding.tvDate.getText().toString());
                    appointment.setStatus("Pending");
                    
                    Log.d("DetailActivity", "Created appointment with ID: " + appointment.getAppointmentId());
                    Log.d("DetailActivity", "Passing appointment to PaymentActivity (not saved to Firebase yet)");
                    
                    Intent intent = new Intent(this, PaymentActivity.class);
                    intent.putExtra("appointment", appointment);
                    startActivity(intent);
                    finish();
                });
                dialogBinding.btnCancel.setOnClickListener(v1 -> {
                    dialog.dismiss();
                });
                dialog.show();
            });

        }
    }

    @NonNull
    private static DatePickerDialog getDatePickerDialog(DialogAppointmentBinding
                                                                dialogBinding, DetailActivity detailActivity) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(detailActivity,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    dialogBinding.tvDate.setText(selectedDate);
                }, year, month, day);
        
        // Set minimum date to tomorrow
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        datePickerDialog.getDatePicker().setMinDate(tomorrow.getTimeInMillis());
        
        return datePickerDialog;
    }
}