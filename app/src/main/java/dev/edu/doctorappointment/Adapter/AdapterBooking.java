package dev.edu.doctorappointment.Adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import dev.edu.doctorappointment.Model.AppointmentModel;
import dev.edu.doctorappointment.Model.DoctorsModel;
import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.Screen.Home.MessengerActivity;
import dev.edu.doctorappointment.databinding.ItemBookingBinding;

public class AdapterBooking extends RecyclerView.Adapter<AdapterBooking.MyViewHolder> {

    private List<AppointmentModel> appointmentModels;
    private List<DoctorsModel> doctorsModels;
    private List<ServiceModel> serviceModels;

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
                        holder.binding.ivAvatar.setImageResource(R.drawable
                                .nam);
                    } else {
                        holder.binding.ivAvatar.setImageResource(R.drawable
                                .nu);
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
        if ("Đã xác nhận".equals(appointmentModel.getStatus()) || "Đã thanh toán".equals(appointmentModel.getStatus())) {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else if ("Đã hủy".equals(appointmentModel.getStatus()) || "Cancelled".equals(appointmentModel.getStatus())) {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvAppointmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
        }
        holder.binding.btnChat.setOnClickListener(v -> {
            // Chat với bác sĩ
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
        TextView tvAppointmentStatus;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemBookingBinding.bind(itemView);
            tvAppointmentStatus = itemView.findViewById(R.id.tv_appointment_status);
        }
    }
}
