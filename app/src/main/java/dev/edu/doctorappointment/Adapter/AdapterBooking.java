package dev.edu.doctorappointment.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Screen.Home.DetailBookingActivity;
import dev.edu.doctorappointment.Screen.Home.MessengerActivity;
import dev.edu.doctorappointment.databinding.ItemBookingBinding;

public class AdapterBooking extends RecyclerView.Adapter<AdapterBooking.MyViewHolder> {

    private List<AppointmentModel> appointmentModels;
    private List<DoctorsModel> doctorsModels;
    private List<ServiceModel> serviceModels;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public AdapterBooking(List<AppointmentModel> appointmentModels, List<DoctorsModel> doctorsModels, List<ServiceModel> serviceModels) {
        this.appointmentModels = appointmentModels;
        this.doctorsModels = doctorsModels;
        this.serviceModels = serviceModels;
    }

    @NonNull
    @Override
    public AdapterBooking.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterBooking.MyViewHolder holder, int position) {
        AppointmentModel appointmentModel = appointmentModels.get(position);
        for (int i = 0; i < doctorsModels.size(); i++) {
            if (doctorsModels.get(i).getDoctorId().equals(appointmentModel.getDoctorId())) {
                holder.binding.tvName.setText(doctorsModels.get(i).getName());
                if (!doctorsModels.get(i).getProfilePicture().isEmpty()) {
                    Log.d("TAGAPI", "onBindViewHolder: " + doctorsModels.get(i).getProfilePicture());
                    Picasso.get().load(doctorsModels.get(i).getProfilePicture()).into(holder.binding.ivAvatar);
                } else {
                    if (doctorsModels.get(i).getGender().equals("Nam")) {
                        holder.binding.ivAvatar.setImageResource(R.drawable.nam);
                    } else {
                        holder.binding.ivAvatar.setImageResource(R.drawable.nu);
                    }
                }
            }
        }
        for (int i = 0; i < serviceModels.size(); i++) {
            if (serviceModels.get(i).getName().equals(appointmentModel.getServiceId())) {
                holder.binding.tvService.setText(serviceModels.get(i).getName());
                holder.binding.tvPrice.setText(MessageFormat.format("{0} VND", serviceModels.get(i).getPrice()));
            }
        }
        holder.binding.tvDate.setText(appointmentModel.getAppointmentTime());
        holder.binding.tvTime.setText(appointmentModel.getAppointmentSlot());
        holder.binding.tvStatus.setText(appointmentModel.getStatus());

        holder.tvAppointmentStatus.setText(appointmentModel.getStatus());

        // Set status color based on appointment status
        if ("Đã xác nhận".equals(appointmentModel.getStatus()) || "Đã thanh toán".equals(appointmentModel.getStatus()) || "Đã trả kết quả".equals(appointmentModel.getStatus())) {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else if ("Đã hủy".equals(appointmentModel.getStatus()) || "Cancelled".equals(appointmentModel.getStatus())) {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
        }

        // Handle examination results display
        if ("Đã trả kết quả".equals(appointmentModel.getStatus())) {
            holder.layoutExaminationResults.setVisibility(View.VISIBLE);
            
            // Load examination results from ReturnResults
            DatabaseReference appointmentRef = database.getReference("Appointments").child(appointmentModel.getAppointmentId());
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
        } else {
            holder.layoutExaminationResults.setVisibility(View.GONE);
        }

        // Thêm sự kiện click vào item để chuyển đến màn hình chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailBookingActivity.class);
            intent.putExtra("appointmentId", appointmentModel.getAppointmentId());
            intent.putExtra("doctorId", appointmentModel.getDoctorId());
            intent.putExtra("serviceId", appointmentModel.getServiceId());
            ((Activity) v.getContext()).startActivityForResult(intent, 1);
        });

        // Sự kiện click vào nút chat
        holder.binding.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MessengerActivity.class);
            intent.putExtra("userId", appointmentModel.getDoctorId());
            intent.putExtra("name", holder.binding.tvName.getText().toString());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return appointmentModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemBookingBinding binding;
        TextView tvAppointmentStatus, tvDiagnosis, tvPrescription, tvTreatmentGuide;
        LinearLayout layoutExaminationResults;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemBookingBinding.bind(itemView);
            tvAppointmentStatus = itemView.findViewById(R.id.tv_appointment_status);
            layoutExaminationResults = itemView.findViewById(R.id.layout_examination_results);
            tvDiagnosis = itemView.findViewById(R.id.tv_diagnosis);
            tvPrescription = itemView.findViewById(R.id.tv_prescription);
            tvTreatmentGuide = itemView.findViewById(R.id.tv_treatment_guide);
        }
    }
}
