package dev.edu.doctorappointment.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemAppointmentAdminBinding;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {
    private List<AppointmentModel> appointments;
    private final Context context;
    private final AppointmentActionListener actionListener;
    private final DoctorInfoLoader doctorInfoLoader;
    private final UserInfoLoader userInfoLoader;

    public interface AppointmentActionListener {
        void onCancel(AppointmentModel appointment);
        void onConfirm(AppointmentModel appointment);
        void onPayment(AppointmentModel appointment);
    }

    public interface DoctorInfoLoader {
        void loadDoctorInfo(AppointmentModel appointment, DoctorInfoCallback callback);
    }

    public interface UserInfoLoader {
        void loadUserInfo(String userId, UserInfoCallback callback);
    }

    public interface DoctorInfoCallback {
        void onDoctorInfoLoaded(DoctorsModel doctor);
    }

    public interface UserInfoCallback {
        void onUserInfoLoaded(UserModel user);
    }

    public AdminAppointmentAdapter(Context context,
                                   List<AppointmentModel> appointments,
                                   AppointmentActionListener actionListener,
                                   DoctorInfoLoader doctorInfoLoader,
                                   UserInfoLoader userInfoLoader) {
        this.context = context;
        this.appointments = appointments;
        this.actionListener = actionListener;
        this.doctorInfoLoader = doctorInfoLoader;
        this.userInfoLoader = userInfoLoader;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppointmentAdminBinding binding = ItemAppointmentAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel appointment = appointments.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateAppointments(List<AppointmentModel> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppointmentAdminBinding binding;

        public ViewHolder(ItemAppointmentAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AppointmentModel appointment) {
            // Load User Info
            userInfoLoader.loadUserInfo(appointment.getUserId(), user -> {
                binding.tvPatientName.setText(user.getName());
                binding.tvPatientPhone.setText(user.getPhone());
            });

            // Appointment Info
            binding.tvAppointmentDate.setText(appointment.getAppointmentTime());
            binding.tvAppointmentTime.setText(appointment.getAppointmentSlot());
            binding.tvServiceInfo.setText("Dịch vụ: " + appointment.getServiceId());

            // Status Chip
            setupStatusChip(appointment.getStatus());

            // Load Doctor Info
            doctorInfoLoader.loadDoctorInfo(appointment, doctor -> {
                binding.tvDoctorName.setText(doctor.getName());
                binding.tvClinicInfo.setText(doctor.getClinicName());
            });

            // Examination Results
            if (appointment.getStatus().equals("completed") && appointment.getExaminationResults() != null) {
                binding.layoutExaminationResults.setVisibility(View.VISIBLE);
                binding.tvDiagnosis.setText("Chẩn đoán: " + appointment.getExaminationResults().get("diagnosis"));
                binding.tvPrescription.setText("Đơn thuốc: " + appointment.getExaminationResults().get("prescription"));
                binding.tvNote.setText("Ghi chú: " + appointment.getExaminationResults().get("note"));
            } else {
                binding.layoutExaminationResults.setVisibility(View.GONE);
            }

            // Button Visibility and Click Listeners
            setupButtons(appointment);
        }

        private void setupStatusChip(String status) {
            Chip chip = binding.chipStatus;
            int bgColor;
            String statusText;

            switch (status) {
                case "Đã thanh toán":
                    statusText = "Đã thanh toán";
                    bgColor = R.color.confirmed;
                    break;
                case "Đã xác nhận":
                    statusText = "Đã xác nhận";
                    bgColor = R.color.confirmed;
                    break;
                case "Đã trả kết quả":
                    statusText = "Đã trả kết quả";
                    bgColor = R.color.confirmed;
                    break;
                case "Đã hủy":
                    statusText = "Đã hủy";
                    bgColor = R.color.cancelled;
                    break;
                default:
                    statusText = "Không xác định";
                    bgColor = android.R.color.darker_gray;
            }

            chip.setText(statusText);
            chip.setChipBackgroundColorResource(bgColor);
            chip.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }

        private void setupButtons(AppointmentModel appointment) {
            String status = appointment.getStatus();

            // Hide all buttons by default
            binding.btnCancel.setVisibility(View.GONE);
            binding.btnConfirm.setVisibility(View.GONE);
            binding.btnPayment.setVisibility(View.GONE);


            switch (status) {
                case "Đã thanh toán":
                    // Show Cancel and Confirm buttons
                    binding.btnCancel.setVisibility(View.VISIBLE);
                    binding.btnConfirm.setVisibility(View.VISIBLE);
                    binding.btnCancel.setOnClickListener(v -> actionListener.onCancel(appointment));
                    binding.btnConfirm.setOnClickListener(v -> actionListener.onConfirm(appointment));
                    break;

                case "Đã xác nhận":
                    // Show Cancel and Payment buttons
                    binding.btnCancel.setVisibility(View.VISIBLE);
                    binding.btnPayment.setVisibility(View.VISIBLE);
                    binding.btnCancel.setOnClickListener(v -> actionListener.onCancel(appointment));
                    binding.btnPayment.setOnClickListener(v -> actionListener.onPayment(appointment));
                    break;

                case "Đã hủy":
                    // Show Cancel and Payment buttons
                    binding.btnConfirm.setVisibility(View.VISIBLE);
                    binding.btnPayment.setVisibility(View.VISIBLE);
                    binding.btnPayment.setOnClickListener(v -> actionListener.onPayment(appointment));
                    binding.btnConfirm.setOnClickListener(v -> actionListener.onConfirm(appointment));

                    break;

                case "Đã trả kết quả":
                    // No additional buttons shown
                    break;
            }
        }
    }
}