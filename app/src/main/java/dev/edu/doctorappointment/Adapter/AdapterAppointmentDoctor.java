package dev.edu.doctorappointment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
        
        // Set status color based on appointment status
        if ("Đã xác nhận".equals(appointment.getStatus()) || "Đã thanh toán".equals(appointment.getStatus())) {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else if ("Đã hủy".equals(appointment.getStatus()) || "Cancelled".equals(appointment.getStatus())) {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
        }
        
        // Load service image if available
//        if (appointment.getServiceImage() != null && !appointment.getServiceImage().isEmpty()) {
//            Picasso.get().load(appointment.getServiceImage()).into(holder.ivServiceImage);
//        } else {
//            // Load a default image or service-specific image based on serviceId
//            setDefaultServiceImage(holder, appointment.getServiceId());
//        }
        
        // Load patient information
        loadPatientInfo(holder, appointment.getUserId());
        
        // Set button actions
        holder.btnConfirm.setOnClickListener(v -> updateAppointmentStatus(appointment, "Đã xác nhận"));
        holder.btnCancel.setOnClickListener(v -> updateAppointmentStatus(appointment, "Đã hủy"));
    }

    private void loadPatientInfo(AppointmentViewHolder holder, String userId) {
        DatabaseReference userRef = database.getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
    }
    
    private void updateAppointmentStatus(AppointmentModel appointment, String status) {
        DatabaseReference appointmentRef = database.getReference("Appointments")
                .child(appointment.getAppointmentId());
        
        appointment.setStatus(status);
        appointmentRef.setValue(appointment);
    }

//    private void setDefaultServiceImage(AppointmentViewHolder holder, String serviceId) {
//        // Set a default image based on service type if available
//        if (serviceId != null) {
//            if (serviceId.contains("Da liễu")) {
//                holder.ivServiceImage.setImageResource(R.drawable.ic_dermatology);
//            } else if (serviceId.contains("Tim mạch")) {
//                holder.ivServiceImage.setImageResource(R.drawable.ic_cardiology);
//            } else if (serviceId.contains("Nhi khoa")) {
//                holder.ivServiceImage.setImageResource(R.drawable.ic_pediatric);
//            } else {
//                // Default image for other services
//                holder.ivServiceImage.setImageResource(R.drawable.ic_doctor);
//            }
//        } else {
//            // Fallback to a generic doctor/appointment icon
//            holder.ivServiceImage.setImageResource(R.drawable.ic_appointment);
//        }
//    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivServiceImage;
        TextView tvPatientName, tvPatientPhone, tvAppointmentTime, tvAppointmentStatus;
        Button btnConfirm, btnCancel;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
//            ivServiceImage = itemView.findViewById(R.id.iv_service_image);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvPatientPhone = itemView.findViewById(R.id.tv_patient_phone);
            tvAppointmentTime = itemView.findViewById(R.id.tv_appointment_time);
            tvAppointmentStatus = itemView.findViewById(R.id.tv_appointment_status);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }
    }
} 