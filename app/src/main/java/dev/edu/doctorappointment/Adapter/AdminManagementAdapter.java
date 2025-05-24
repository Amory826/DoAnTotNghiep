package dev.edu.doctorappointment.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.edu.doctorappointment.Model.ManagementItem;
import dev.edu.doctorappointment.databinding.ItemManagementBinding;

public class AdminManagementAdapter extends RecyclerView.Adapter<AdminManagementAdapter.ViewHolder> {
    private List<ManagementItem> items;

    public AdminManagementAdapter(List<ManagementItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagementBinding binding = ItemManagementBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemManagementBinding binding;

        ViewHolder(ItemManagementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ManagementItem item) {
            binding.tvTitle.setText(item.getTitle());
            binding.imgIcon.setImageResource(item.getIconResId());
            binding.getRoot().setOnClickListener(v -> item.getOnClickListener().onClick());
        }
    }
} 