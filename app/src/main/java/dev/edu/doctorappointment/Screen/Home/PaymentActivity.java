package dev.edu.doctorappointment.Screen.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
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
import org.json.JSONObject;
import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.CreateOrder;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.PaymentModel;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Utils.AppInfo;
import dev.edu.doctorappointment.databinding.ActivityPaymentBinding;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";
    AppointmentModel appointmentModel;
    ActivityPaymentBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ServicesRef = database.getReference("Services");
    DatabaseReference myRef = database.getReference("Appointments");
    DatabaseReference myRefpm = database.getReference("Payments");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ZaloPay SDK
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Log.d(TAG, "Initializing ZaloPay SDK with APP_ID: " + AppInfo.APP_ID);
        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent().hasExtra("appointment")) {
            appointmentModel = (AppointmentModel) getIntent().getSerializableExtra("appointment");
            if (appointmentModel == null) {
                Log.e(TAG, "AppointmentModel is null after retrieval from Intent.");
                TipDialog.show(this, "Lỗi dữ liệu cuộc hẹn", TipDialog.TYPE.ERROR);
                finish();
                return;
            }
            Log.d(TAG, "AppointmentModel loaded: " + appointmentModel.getAppointmentId());
            loadServiceAndDoctorDetails();
            binding.btnPay.setOnClickListener(v -> processPayment());
        } else {
            Log.e(TAG, "Intent does not have 'appointment' extra.");
            TipDialog.show(this, "Không có thông tin cuộc hẹn", TipDialog.TYPE.ERROR);
            finish();
        }
    }

    private void processPayment() {
        WaitDialog.show(this, "Đang xử lý thanh toán...");
        Log.d(TAG, "Starting ZaloPay payment process.");

        // Validate amount
        String amountStr = binding.tvMoney.getText().toString().trim()
                .replace(" VND", "").replace(",", "");
        if (amountStr.isEmpty()) {
            WaitDialog.dismiss();
            TipDialog.show(this, "Số tiền không hợp lệ", TipDialog.TYPE.ERROR);
            Log.e(TAG, "Amount is empty or invalid.");
            return;
        }

        try {
            long amount = Long.parseLong(amountStr);
            if (amount <= 0) {
                WaitDialog.dismiss();
                TipDialog.show(this, "Số tiền phải lớn hơn 0", TipDialog.TYPE.ERROR);
                Log.e(TAG, "Invalid amount: " + amount);
                return;
            }
            Log.d(TAG, "Valid amount: " + amount);

            // Create ZaloPay order
            CreateOrder orderApi = new CreateOrder();
            JSONObject data = orderApi.createOrder(String.valueOf(amount));
            Log.d(TAG, "ZaloPay createOrder response: " + data.toString());

            String code = data.getString("return_code");
            if ("1".equals(code)) { // Success as per ZaloPay docs
                String token = data.getString("zp_trans_token");
                Log.d(TAG, "ZaloPay order created successfully. Token: " + token);

                // Proceed to ZaloPay payment
                ZaloPaySDK.getInstance().payOrder(this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                        Log.d(TAG, "ZaloPay Payment Succeeded. TransactionID: " + transactionId);
                        savePaymentAndAppointment(transactionId, transToken);
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransToken, String appTransID) {
                        WaitDialog.dismiss();
                        Log.w(TAG, "ZaloPay Payment Canceled. Token: " + zpTransToken);
                        TipDialog.show(PaymentActivity.this, "Thanh toán đã bị hủy", TipDialog.TYPE.WARNING);
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                        WaitDialog.dismiss();
                        Log.e(TAG, "ZaloPay Payment Error: " + zaloPayError + ". Token: " + zpTransToken);
                        TipDialog.show(PaymentActivity.this, "Lỗi thanh toán: " + zaloPayError, TipDialog.TYPE.ERROR);
                    }
                });
            } else {
                WaitDialog.dismiss();
                String returnMessage = data.optString("return_message", "Lỗi không xác định từ ZaloPay");
                Log.e(TAG, "Failed to create ZaloPay order. Code: " + code + ", Message: " + returnMessage);
                TipDialog.show(this, "Lỗi tạo đơn hàng: " + returnMessage, TipDialog.TYPE.ERROR);
            }
        } catch (NumberFormatException e) {
            WaitDialog.dismiss();
            Log.e(TAG, "Invalid amount format: " + amountStr, e);
            TipDialog.show(this, "Định dạng số tiền không hợp lệ", TipDialog.TYPE.ERROR);
        } catch (Exception e) {
            WaitDialog.dismiss();
            Log.e(TAG, "Exception during ZaloPay payment process: " + e.getMessage(), e);
            TipDialog.show(this, "Lỗi xử lý thanh toán", TipDialog.TYPE.ERROR);
        }
    }

    private void loadServiceAndDoctorDetails() {
        ServicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean serviceFound = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ServiceModel serviceModel = dataSnapshot.getValue(ServiceModel.class);
                    if (serviceModel != null && serviceModel.getName().equals(appointmentModel.getServiceId())) {
                        serviceFound = true;
                        binding.tvAmount.setText(serviceModel.getPrice() + " VND");
                        binding.tvMoney.setText(serviceModel.getPrice() + " VND");
                        binding.tvAddress.setText(appointmentModel.getClinicName());
                        binding.tvService.setText(appointmentModel.getServiceId());
                        binding.tvDate.setText(appointmentModel.getAppointmentTime() + ", " + appointmentModel.getAppointmentSlot());
                        binding.tvTotal.setText(serviceModel.getPrice() + " VND");
                        binding.tvBooking.setText(new UserData(PaymentActivity.this).getData("name"));

                        FirebaseDatabase.getInstance().getReference("Doctors")
                                .child(appointmentModel.getDoctorId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot doctorSnapshot) {
                                        if (doctorSnapshot.exists()) {
                                            DoctorsModel doctorsModel = doctorSnapshot.getValue(DoctorsModel.class);
                                            if (doctorsModel != null) {
                                                binding.tvName.setText(doctorsModel.getName());
                                                if (doctorsModel.getProfilePicture() != null && !doctorsModel.getProfilePicture().isEmpty()) {
                                                    Picasso.get().load(doctorsModel.getProfilePicture()).into(binding.ivAvatar);
                                                } else {
                                                    if ("Nam".equals(doctorsModel.getGender())) {
                                                        binding.ivAvatar.setImageResource(R.drawable.nam);
                                                    } else {
                                                        binding.ivAvatar.setImageResource(R.drawable.nu);
                                                    }
                                                }
                                            } else {
                                                Log.e(TAG, "DoctorsModel is null for ID: " + appointmentModel.getDoctorId());
                                            }
                                        } else {
                                            Log.e(TAG, "Doctor not found with ID: " + appointmentModel.getDoctorId());
                                            binding.tvName.setText("Không tìm thấy bác sĩ");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "Firebase error loading doctor details: " + error.getMessage());
                                        TipDialog.show(PaymentActivity.this, "Lỗi tải thông tin bác sĩ", TipDialog.TYPE.ERROR);
                                    }
                                });
                        break;
                    }
                }
                if (!serviceFound) {
                    Log.e(TAG, "Service not found: " + appointmentModel.getServiceId());
                    TipDialog.show(PaymentActivity.this, "Không tìm thấy dịch vụ", TipDialog.TYPE.ERROR);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error loading services: " + error.getMessage());
                TipDialog.show(PaymentActivity.this, "Lỗi tải thông tin dịch vụ", TipDialog.TYPE.ERROR);
            }
        });
    }

    private void savePaymentAndAppointment(String zaloTransactionId, String zaloTransToken) {
        Log.d(TAG, "Starting savePaymentAndAppointment...");
        appointmentModel.setStatus("Đã thanh toán");

        PaymentModel paymentModel = new PaymentModel();
        String paymentId = myRefpm.push().getKey();
        if (paymentId == null) {
            WaitDialog.dismiss();
            Log.e(TAG, "Failed to generate paymentId (push key is null)");
            TipDialog.show(PaymentActivity.this, "Lỗi tạo mã thanh toán", TipDialog.TYPE.ERROR);
            return;
        }
        paymentModel.setPaymentId(paymentId);
        paymentModel.setUserId(new UserData(PaymentActivity.this).getData("id"));
        paymentModel.setAppointmentId(appointmentModel.getAppointmentId());
        paymentModel.setAmount(binding.tvAmount.getText().toString());
        paymentModel.setStatus("Success");
        paymentModel.setTimestamp(String.valueOf(System.currentTimeMillis()));

        Log.d(TAG, "Saving payment details to Firebase: " + paymentModel.getPaymentId());
        myRefpm.child(paymentModel.getPaymentId()).setValue(paymentModel).addOnCompleteListener(paymentTask -> {
            if (paymentTask.isSuccessful()) {
                Log.d(TAG, "Payment details saved successfully.");
                Log.d(TAG, "Updating appointment status in Firebase: " + appointmentModel.getAppointmentId());
                myRef.child(appointmentModel.getAppointmentId()).setValue(appointmentModel).addOnCompleteListener(appointmentUpdateTask -> {
                    if (appointmentUpdateTask.isSuccessful()) {
                        Log.d(TAG, "Appointment status updated successfully.");
                        updateDoctorBookingCount(appointmentModel.getDoctorId(),
                                appointmentModel.getAppointmentTime(),
                                appointmentModel.getAppointmentSlot());
                        sendNotifications();
                        WaitDialog.dismiss();
                        TipDialog.show(PaymentActivity.this, "Thanh toán thành công!", TipDialog.TYPE.SUCCESS);

                        Log.d(TAG, "Navigating to Payment2Activity.");
                        Intent intent = new Intent(PaymentActivity.this, Payment2Activity.class);
                        intent.putExtra("doctor", binding.tvName.getText().toString());
                        intent.putExtra("date", binding.tvDate.getText().toString());
                        startActivity(intent);
                        finish();
                    } else {
                        WaitDialog.dismiss();
                        Log.e(TAG, "Error saving appointment update to Firebase.", appointmentUpdateTask.getException());
                        TipDialog.show(this, "Lỗi khi cập nhật cuộc hẹn", TipDialog.TYPE.ERROR);
                    }
                });
            } else {
                WaitDialog.dismiss();
                Log.e(TAG, "Error saving payment details to Firebase.", paymentTask.getException());
                TipDialog.show(this, "Lỗi khi lưu thông tin thanh toán", TipDialog.TYPE.ERROR);
            }
        });
    }

    public void back(View view) {
        finish();
    }

    private void updateDoctorBookingCount(String doctorId, String date, String timeSlot) {
        String safeDate = date.replace("/", "_").replace(".", "_");
        Log.d(TAG + "_BookingUpdate", "Updating booking count for doctor: " + doctorId + ", date: " + safeDate + ", slot: " + timeSlot);
        updateBookingCountInTransaction(doctorId, safeDate, timeSlot);
    }

    private void updateBookingCountInTransaction(String doctorId, String safeDate, String timeSlot) {
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Doctors").child(doctorId);
        Log.d(TAG + "_BookingUpdate", "Starting transaction for doctorId: " + doctorId);

        doctorRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                DoctorsModel doctor = mutableData.getValue(DoctorsModel.class);
                if (doctor == null) {
                    Log.w(TAG + "_BookingUpdate", "Doctor data is null during transaction, aborting for doctorId: " + doctorId);
                    return Transaction.abort();
                }
                Log.d(TAG + "_BookingUpdate", "Transaction: Got doctor data: " + doctor.getName());

                Map<String, Map<String, Integer>> bookingCounts = doctor.getBookingCountByDateTime();
                if (bookingCounts == null) {
                    bookingCounts = new HashMap<>();
                    Log.d(TAG + "_BookingUpdate", "Transaction: Initialized new booking counts map");
                }

                Map<String, Integer> timeSlots = bookingCounts.get(safeDate);
                if (timeSlots == null) {
                    timeSlots = new HashMap<>();
                    Log.d(TAG + "_BookingUpdate", "Transaction: Initialized new time slots map for date: " + safeDate);
                }

                Integer count = timeSlots.get(timeSlot);
                timeSlots.put(timeSlot, (count == null ? 0 : count) + 1);
                Log.d(TAG + "_BookingUpdate", "Transaction: Incremented count for slot " + timeSlot + " to " + timeSlots.get(timeSlot));

                bookingCounts.put(safeDate, timeSlots);
                doctor.setBookingCountByDateTime(bookingCounts);
                mutableData.setValue(doctor);
                Log.d(TAG + "_BookingUpdate", "Transaction: Updated doctor data in transaction");
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG + "_BookingUpdate", "Firebase transaction failed for doctor " + doctorId + ": " + error.getMessage(), error.toException());
                } else {
                    Log.d(TAG + "_BookingUpdate", "Firebase transaction " + (committed ? "successful" : "aborted") + " for doctor " + doctorId);
                }
            }
        });
    }

    private void sendNotifications() {
        Log.d(TAG, "Preparing to send notifications.");
        if (appointmentModel == null) {
            Log.e(TAG, "Cannot send notifications, appointmentModel is null.");
            return;
        }

        String userId = appointmentModel.getUserId();
        String patientName = new UserData(PaymentActivity.this).getData("name");
        String serviceName = appointmentModel.getServiceId();
        String appointmentTime = appointmentModel.getAppointmentTime();
        String appointmentSlot = appointmentModel.getAppointmentSlot();

        if (userId != null && !userId.isEmpty()) {
            String titleUser = "Đặt lịch thành công";
            String messageBodyUser = "Bạn đã đặt lịch khám thành công vào " + appointmentTime +
                    " lúc " + appointmentSlot + " cho dịch vụ " + serviceName + ".";
            Log.d(TAG, "Sending notification to User ID: " + userId + " Title: " + titleUser);
            dev.edu.doctorappointment.Utils.NotificationHelper.sendAppointmentNotification(
                    userId,
                    titleUser,
                    messageBodyUser,
                    "user",
                    getBaseContext()
            );
        } else {
            Log.w(TAG, "User ID is null or empty, cannot send user notification.");
        }

        String doctorId = appointmentModel.getDoctorId();
        if (doctorId != null && !doctorId.isEmpty()) {
            String doctorTitle = "Lịch hẹn mới";
            String doctorMessage = "Bạn có lịch hẹn mới với " + patientName +
                    " vào " + appointmentTime + " lúc " + appointmentSlot +
                    " cho dịch vụ " + serviceName + ".";
            Log.d(TAG, "Sending notification to Doctor ID: " + doctorId + " Title: " + doctorTitle);
            dev.edu.doctorappointment.Utils.NotificationHelper.sendAppointmentNotification(
                    doctorId,
                    doctorTitle,
                    doctorMessage,
                    "doctor",
                    getBaseContext()
            );
        } else {
            Log.w(TAG, "Doctor ID is null or empty, cannot send doctor notification.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called.");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}