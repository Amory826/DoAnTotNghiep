//package dev.edu.doctorappointment.Adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.chip.Chip;
//
//import java.util.List;
//
//import dev.edu.doctorappointment.Model.AppointmentModel;
//import dev.edu.doctorappointment.Model.DoctorsModel;
//import dev.edu.doctorappointment.R;
//import dev.edu.doctorappointment.databinding.ItemAppointmentAdminBinding;
//
//public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {
//    private List<AppointmentModel> appointments;
//    private final Context context;
//    private final AppointmentActionListener actionListener;
//    private final DoctorInfoLoader doctorInfoLoader;
//
//    public interface AppointmentActionListener {
//        void onApprove(AppointmentModel appointment);
//        void onReject(AppointmentModel appointment);
//        void onViewDetails(AppointmentModel appointment);
//    }
//
//    public interface DoctorInfoLoader {
//        void loadDoctorInfo(AppointmentModel appointment, DoctorInfoCallback callback);
//    }
//
//    public interface DoctorInfoCallback {
//        void onDoctorInfoLoaded(DoctorsModel doctor);
//    }
//
//    public AdminAppointmentAdapter(Context context,
//                                   List<AppointmentModel> appointments,
//                                   AppointmentActionListener actionListener,
//                                   DoctorInfoLoader doctorInfoLoader) {
//        this.context = context;
//        this.appointments = appointments;
//        this.actionListener = actionListener;
//        this.doctorInfoLoader = doctorInfoLoader;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        ItemAppointmentAdminBinding binding = ItemAppointmentAdminBinding.inflate(
//                LayoutInflater.from(parent.getContext()), parent, false);
//        return new ViewHolder(binding);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        AppointmentModel appointment = appointments.get(position);
//        holder.bind(appointment);
//    }
//
//    @Override
//    public int getItemCount() {
//        return appointments.size();
//    }
//
//    public void updateAppointments(List<AppointmentModel> newAppointments) {
//        this.appointments = newAppointments;
//        notifyDataSetChanged();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        private final ItemAppointmentAdminBinding binding;
//
//        public ViewHolder(ItemAppointmentAdminBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//        }
//
//        public void bind(AppointmentModel appointment) {
//            // Patient Info
//            binding.tvPatientName.setText(appointment.getPatientName());
//            binding.tvPatientPhone.setText(appointment.getPatientPhone());
//
//            // Appointment Info
//            binding.tvAppointmentDate.setText(appointment.getAppointmentTime());
//            binding.tvAppointmentTime.setText(appointment.getAppointmentSlot());
//            binding.tvServiceInfo.setText(appointment.getServiceId());
//
//            // Status Chip
//            setupStatusChip(appointment.getStatus());
//
//            // Load Doctor Info
//            doctorInfoLoader.loadDoctorInfo(appointment, doctor -> {
//                binding.tvDoctorName.setText("Bác sĩ: " + doctor.getName());
//                binding.tvClinicInfo.setText("Phòng khám: " + doctor.getClinicName());
//            });
//
//            // Examination Results
//            if (appointment.getStatus().equals("Đã trả kết quả") && appointment.getExaminationResults() != null) {
//                binding.layoutExaminationResults.setVisibility(View.VISIBLE);
//                binding.tvDiagnosis.setText("Chẩn đoán: " + appointment.getExaminationResults().get(""));
//                binding.tvPrescription.setText("Đơn thuốc: " + appointment.getPrescription());
//                binding.tvNote.setText("Ghi chú: " + appointment.getTreatmentGuide());
//            } else {
//                binding.layoutExaminationResults.setVisibility(View.GONE);
//            }
//
//            // Button Visibility
//            setupButtons(appointment);
//
//            // Click Listeners
//            binding.btnApprove.setOnClickListener(v -> actionListener.onApprove(appointment));
//            binding.btnReject.setOnClickListener(v -> actionListener.onReject(appointment));
//            binding.btnViewDetails.setOnClickListener(v -> actionListener.onViewDetails(appointment));
//        }
//
//        private void setupStatusChip(String status) {
//            Chip chip = binding.chipStatus;
//            switch (status) {
//                case "pending":
//                    chip.setText("Chờ xác nhận");
//                    chip.setChipBackgroundColorResource(android.R.color.holo_orange_light);
//                    break;
//                case "approved":
//                    chip.setText("Đã xác nhận");
//                    chip.setChipBackgroundColorResource(android.R.color.holo_green_light);
//                    break;
//                case "rejected":
//                    chip.setText("Đã từ chối");
//                    chip.setChipBackgroundColorResource(android.R.color.holo_red_light);
//                    break;
//                case "completed":
//                    chip.setText("Đã hoàn thành");
//                    chip.setChipBackgroundColorResource(android.R.color.holo_blue_light);
//                    break;
//            }
//            chip.setTextColor(ContextCompat.getColor(context, android.R.color.white));
//        }
//
//        private void setupButtons(AppointmentModel appointment) {
//            boolean isPending = appointment.getStatus().equals("pending");
//            binding.btnApprove.setVisibility(isPending ? View.VISIBLE : View.GONE);
//            binding.btnReject.setVisibility(isPending ? View.VISIBLE : View.GONE);
//        }
//    }
//}