package dev.edu.doctorappointment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.edu.doctorappointment.Model.UserModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemUserBinding;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.ViewHolder> {
    private List<UserModel> userList;
    private List<UserModel> filteredList;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(UserModel user);
    }

    public AdapterUser(List<UserModel> userList, OnDeleteClickListener listener) {
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList);
        this.deleteClickListener = listener;
    }

    public void setFilteredList(List<UserModel> filteredList) {
        this.filteredList = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserBinding binding;

        public ViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UserModel user) {
            // Set user info
            binding.tvName.setText(user.getName());
            binding.tvEmail.setText(user.getEmail());
            binding.tvPhone.setText(user.getPhone());

            // Set default avatar
            binding.ivAvatar.setImageResource(R.drawable.nam);

            // Set click listener for delete
            binding.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(user));
        }
    }
}