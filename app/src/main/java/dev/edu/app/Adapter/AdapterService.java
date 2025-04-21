package dev.edu.app.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import dev.edu.app.Model.ServiceModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemServiceBinding;

public class AdapterService extends RecyclerView.Adapter<AdapterService.ViewHolder> {
    List<ServiceModel> services; // <---// >

    public AdapterService(List<ServiceModel> services) {
        this.services = services;
    }

    @NonNull
    @Override
    public AdapterService.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterService.ViewHolder holder, int position) {
        ServiceModel service = services.get(position);
        holder.binding.tvService.setText(service.getName());
        Picasso.get().load(service.getIcon()).into(holder.binding.imgService);
    }

    @Override
    public int getItemCount() {
        return Math.min(services.size(), 9);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemServiceBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemServiceBinding.bind(itemView);
        }
    }
}
