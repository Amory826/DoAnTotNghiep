package dev.edu.doctorappointment.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Screen.Home.DetailBookingDoctorActivity;

public class AdapterAppointmentDoctor extends RecyclerView.Adapter<AdapterAppointmentDoctor.AppointmentViewHolder> {

    private List<AppointmentModel> appointments;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public AdapterAppointmentDoctor(List<AppointmentModel> appointments) {
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment_doctor, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        AppointmentModel appointment = appointments.get(position);
        
        // Set appointment time and status
        holder.tvAppointmentTime.setText(appointment.getAppointmentTime() + " " + appointment.getAppointmentSlot());
        holder.tvAppointmentStatus.setText(appointment.getStatus());
        
        // Set status color and button visibility based on appointment status
        if ("Đã xác nhận".equals(appointment.getStatus())) {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.btnConfirm.setVisibility(View.GONE);
            holder.btnRefund.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DetailBookingDoctorActivity.class);
                intent.putExtra("appointmentId", appointment.getAppointmentId());
                v.getContext().startActivity(intent);
            });            // Add click listener for refund button
            holder.btnRefund.setOnClickListener(v -> {
                // TODO: Implement refund functionality
                Toast.makeText(v.getContext(), "Chức năng hoàn tiền đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        } else if ("Đã thanh toán".equals(appointment.getStatus()) || "Đã trả kết quả".equals(appointment.getStatus())) {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.btnConfirm.setVisibility(View.VISIBLE);
            holder.btnRefund.setVisibility(View.GONE);
        } else if ("Đã hủy".equals(appointment.getStatus()) || "Cancelled".equals(appointment.getStatus())) {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.btnConfirm.setVisibility(View.GONE);
            holder.btnRefund.setVisibility(View.GONE);
        } else {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
            holder.btnConfirm.setVisibility(View.VISIBLE);
            holder.btnRefund.setVisibility(View.GONE);
        }
        
        // Load patient information
        DatabaseReference usersRef = database.getReference("Users");
        usersRef.child(appointment.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null) {
                        holder.tvPatientName.setText(user.getName());
                        holder.tvPatientPhone.setText(user.getPhone());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
        
        // Set button actions
        holder.btnConfirm.setOnClickListener(v -> updateAppointmentStatus(appointment, "Đã xác nhận"));
        holder.btnCancel.setOnClickListener(v -> updateAppointmentStatus(appointment, "Đã hủy"));

        // Handle examination results display
        if ("Đã trả kết quả".equals(appointment.getStatus())) {
            holder.btnConfirm.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnEditResults.setVisibility(View.VISIBLE);
            
            // Show examination results
            holder.layoutExaminationResults.setVisibility(View.VISIBLE);
            
            // Load examination results from ReturnResults
            DatabaseReference appointmentRef = database.getReference("Appointments").child(appointment.getAppointmentId());
            appointmentRef.child("ReturnResults").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String diagnosis = snapshot.child("diagnosis").getValue(String.class);
                        String prescription = snapshot.child("prescription").getValue(String.class);
                        String treatmentGuide = snapshot.child("treatmentGuide").getValue(String.class);
                        
                        if (diagnosis != null) {
                            holder.tvDiagnosis.setText("Chẩn đoán: " + diagnosis);
                        }
                        if (prescription != null) {
                            holder.tvPrescription.setText("Đơn thuốc: " + prescription);
                        }
                        if (treatmentGuide != null) {
                            holder.tvTreatmentGuide.setText("Hướng dẫn điều trị: " + treatmentGuide);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });

            // Set click listener for edit results button
            holder.btnEditResults.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DetailBookingDoctorActivity.class);
                intent.putExtra("appointmentId", appointment.getAppointmentId());
                intent.putExtra("isEditing", true);
                v.getContext().startActivity(intent);
            });
        } else {
            holder.layoutExaminationResults.setVisibility(View.GONE);
            holder.btnEditResults.setVisibility(View.GONE);
        }
    }

    private void updateAppointmentStatus(AppointmentModel appointment, String status) {
        DatabaseReference appointmentRef = database.getReference("Appointments")
                .child(appointment.getAppointmentId());
        
        appointment.setStatus(status);
        appointmentRef.setValue(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivServiceImage;
        TextView tvPatientName, tvPatientPhone, tvAppointmentTime, tvAppointmentStatus;
        TextView tvDiagnosis, tvPrescription, tvTreatmentGuide;
        Button btnConfirm, btnCancel, btnEditResults, btnRefund;
        LinearLayout layoutExaminationResults, layoutButtons;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvPatientPhone = itemView.findViewById(R.id.tv_patient_phone);
            tvAppointmentTime = itemView.findViewById(R.id.tv_appointment_time);
            tvAppointmentStatus = itemView.findViewById(R.id.tv_appointment_status);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnEditResults = itemView.findViewById(R.id.btn_edit_results);
            btnRefund = itemView.findViewById(R.id.btn_refund);
            
            // Examination results views
            layoutExaminationResults = itemView.findViewById(R.id.layout_examination_results);
            tvDiagnosis = itemView.findViewById(R.id.tv_diagnosis);
            tvPrescription = itemView.findViewById(R.id.tv_prescription);
            tvTreatmentGuide = itemView.findViewById(R.id.tv_treatment_guide);
            layoutButtons = itemView.findViewById(R.id.layout_buttons);
        }
    }
} 