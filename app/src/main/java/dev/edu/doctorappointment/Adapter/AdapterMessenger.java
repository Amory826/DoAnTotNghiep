package dev.edu.doctorappointment.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import dev.edu.doctorappointment.Model.MessengerModel;
import dev.edu.doctorappointment.Model.UserData;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemMessengerBinding;

public class AdapterMessenger extends RecyclerView.Adapter<AdapterMessenger.MyViewHolder> {

    private List<MessengerModel> messengerModels;
    private Context context;

    public AdapterMessenger(List<MessengerModel> messengerModels, Context context) {
        this.messengerModels = messengerModels;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterMessenger.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messenger, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterMessenger.MyViewHolder holder, int position) {
        MessengerModel messengerModel = messengerModels.get(position);

        if (messengerModel.getIdSender().equals(new UserData(context).getData("id"))) {
            holder.binding.receiver.setVisibility(View.GONE);
            holder.binding.sender.setVisibility(View.VISIBLE);
            holder.binding.tvSender.setText(messengerModel.getMessage());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            holder.binding.tvTimeSender.setText(simpleDateFormat.format(Long.parseLong(messengerModel.getTimestamp())));
        } else {
            holder.binding.receiver.setVisibility(View.VISIBLE);
            holder.binding.sender.setVisibility(View.GONE);
            holder.binding.tvReceiver.setText(messengerModel.getMessage());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            holder.binding.tvTimeReceiver.setText(simpleDateFormat.format(Long.parseLong(messengerModel.getTimestamp())));
        }
    }

    @Override
    public int getItemCount() {
        return messengerModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemMessengerBinding binding;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemMessengerBinding.bind(itemView);
        }
    }
}
