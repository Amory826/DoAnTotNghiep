package dev.edu.doctorappointment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemWorkingHoursBinding;

public class WorkingHoursAdapter extends RecyclerView.Adapter<WorkingHoursAdapter.MyViewHolder> {
    private List<String> workingHours;
    private OnWorkingHoursListener listener;

    public interface OnWorkingHoursListener {
        void onDeleteClick(int position);
    }

    public WorkingHoursAdapter(List<String> workingHours, OnWorkingHoursListener listener) {
        this.workingHours = workingHours;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_working_hours, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String workingHour = workingHours.get(position);
        holder.binding.tvWorkingHours.setText(workingHour);
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workingHours.size();
    }

    public void updateData(List<String> newWorkingHours) {
        this.workingHours = newWorkingHours;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemWorkingHoursBinding binding;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemWorkingHoursBinding.bind(itemView);
        }
    }
} 