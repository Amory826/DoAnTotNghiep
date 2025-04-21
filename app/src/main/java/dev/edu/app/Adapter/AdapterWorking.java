package dev.edu.app.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemWorkingBinding;

public class AdapterWorking extends RecyclerView.Adapter<AdapterWorking.MyViewHolder>{

    private List<String> working;

    public AdapterWorking(List<String> working) {
        this.working = working;
    }

    @NonNull
    @Override
    public AdapterWorking.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_working, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterWorking.MyViewHolder holder, int position) {
        String work = working.get(position);
        holder.binding.tvDay.setText(work);
    }

    @Override
    public int getItemCount() {
        return working.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemWorkingBinding binding;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemWorkingBinding.bind(itemView);
        }
    }
}
