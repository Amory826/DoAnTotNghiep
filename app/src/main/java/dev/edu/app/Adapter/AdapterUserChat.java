package dev.edu.app.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.edu.app.Model.DoctorsModel;
import dev.edu.app.Model.UserModel;
import dev.edu.app.Screen.Home.MessengerActivity;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemUserMessBinding;

public class AdapterUserChat extends RecyclerView.Adapter<AdapterUserChat.ViewHolder> {

    private List<String> users;
    private Map<String, UserModel> userCache = new HashMap<>();
    private Map<String, DoctorsModel> doctorCache = new HashMap<>();

    public AdapterUserChat(List<String> users) {
        this.users = users;
        fetchAllUsers();
    }

    @NonNull
    @Override
    public AdapterUserChat.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_mess, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUserChat.ViewHolder holder, int position) {
        String userId = users.get(position);

        // Check cache for user data
        if (userCache.containsKey(userId)) {
            UserModel userModel = userCache.get(userId);
            holder.binding.tvName.setText(userModel.getName());
            // Set profile picture if available
        } else if (doctorCache.containsKey(userId)) {
            DoctorsModel doctorModel = doctorCache.get(userId);
            holder.binding.tvName.setText(doctorModel.getName());
            // Set profile picture if available
            if (!doctorModel.getProfilePicture().isEmpty()) {
                Picasso.get().load(doctorModel.getProfilePicture()).into(holder.binding.imageView);
            } else {
                if ("Nam".equals(doctorModel.getGender())) {
                    holder.binding.imageView.setImageResource(R.drawable.nam);
                } else {
                    holder.binding.imageView.setImageResource(R.drawable.nu);
                }
            }
        }

        holder.itemView.setOnClickListener(v -> {
            // Open chat
            Intent intent = new Intent(holder.itemView.getContext(), MessengerActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("name", holder.binding.tvName.getText().toString());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void fetchAllUsers() {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            if (userModel != null) {
                                userCache.put(userModel.getKeyID(), userModel);
                            }
                        }
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });

        FirebaseDatabase.getInstance().getReference().child("Doctors")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            DoctorsModel doctorModel = dataSnapshot.getValue(DoctorsModel.class);
                            if (doctorModel != null) {
                                doctorCache.put(doctorModel.getDoctorId(), doctorModel);
                            }
                        }
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemUserMessBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemUserMessBinding.bind(itemView);
        }
    }
}
