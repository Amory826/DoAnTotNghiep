package dev.edu.doctorappointment.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemServiceAdminBinding;

public class ServiceAdminAdapter extends RecyclerView.Adapter<ServiceAdminAdapter.ViewHolder> {
    private List<ServiceModel> services;
    private final ServiceActionListener editListener;
    private final ServiceActionListener deleteListener;

    public interface ServiceActionListener {
        void onServiceAction(ServiceModel service);
    }

    public ServiceAdminAdapter(List<ServiceModel> services,
                               ServiceActionListener editListener,
                               ServiceActionListener deleteListener) {
        this.services = services;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceAdminBinding binding = ItemServiceAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceModel service = services.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public void updateServices(List<ServiceModel> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemServiceAdminBinding binding;

        public ViewHolder(ItemServiceAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ServiceModel service) {
            // Load service icon using Glide with error handling
            Picasso.get().load(service.getIcon()).into(binding.imgService);
            // Set service information
            binding.tvServiceName.setText(service.getName());
            binding.tvServiceDescription.setText(service.getDescription());
            binding.tvServicePrice.setText(service.getPrice());

            // Set click listeners for buttons
            binding.btnEdit.setOnClickListener(v -> editListener.onServiceAction(service));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onServiceAction(service));
        }
    }
}