package dev.edu.doctorappointment.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import dev.edu.doctorappointment.Model.ServiceModel;
import dev.edu.doctorappointment.databinding.ItemServiceBinding;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    private List<ServiceModel> services;
    private final ServiceActionListener editListener;
    private final ServiceActionListener deleteListener;

    public interface ServiceActionListener {
        void onServiceAction(ServiceModel service);
    }

    public ServiceAdapter(List<ServiceModel> services,
                         ServiceActionListener editListener,
                         ServiceActionListener deleteListener) {
        this.services = services;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceBinding binding = ItemServiceBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceModel service = services.get(position);
//        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemServiceBinding binding;

        public ViewHolder(ItemServiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

//        public void bind(ServiceModel service) {
//            binding.tvServiceName.setText(service.getName());
//            binding.tvServiceDescription.setText(service.getDescription());
//
//            // Format price with currency
//            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//            String formattedPrice = formatter.format(service.getPrice());
//            binding.tvServicePrice.setText(formattedPrice);
//
//            // Click listeners
//            binding.btnEdit.setOnClickListener(v -> editListener.onServiceAction(service));
//            binding.btnDelete.setOnClickListener(v -> deleteListener.onServiceAction(service));
//        }
    }
} 