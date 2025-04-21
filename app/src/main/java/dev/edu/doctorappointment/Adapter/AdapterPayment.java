package dev.edu.doctorappointment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import dev.edu.doctorappointment.Model.PaymentModel;
import dev.edu.doctorappointment.R;
import dev.edu.doctorappointment.databinding.ItemPaymentBinding;

public class AdapterPayment extends RecyclerView.Adapter<AdapterPayment.ViewHolder>{

    private List<PaymentModel> paymentModelList;

    public AdapterPayment(List<PaymentModel> paymentModelList) {
        this.paymentModelList = paymentModelList;
    }

    @NonNull
    @Override
    public AdapterPayment.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPayment.ViewHolder holder, int position) {
        PaymentModel paymentModel = paymentModelList.get(position);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        // Payment ID bạn đã mua gói dịch vụ này với mã ID là: , số tiền bạn đã thanh toán là: , thời gian thanh toán: , trạng thái thanh toán: // dùng tiếng anh

        String content = "Payment ID: " + paymentModel.getPaymentId() + ", you have purchased this service with ID: " + paymentModel.getAppointmentId() + ", the amount you have paid is: " + paymentModel.getAmount() + ", payment time: " + simpleDateFormat.format(Long.parseLong(paymentModel.getTimestamp()));
        holder.binding.tvContent.setText(content);
        holder.binding.tvStatus.setText("Payment status: " + paymentModel.getStatus());
    }

    @Override
    public int getItemCount() {
        return paymentModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemPaymentBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemPaymentBinding.bind(itemView);
        }
    }
}
