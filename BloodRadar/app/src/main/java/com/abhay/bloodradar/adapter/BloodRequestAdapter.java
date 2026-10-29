package com.abhay.bloodradar.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.abhay.bloodradar.ChatActivity;
import com.abhay.bloodradar.R;
import com.abhay.bloodradar.model.BloodRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.ViewHolder> {
    private List<BloodRequest> requests;
    private Context context;

    public BloodRequestAdapter(Context context, List<BloodRequest> requests) {
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blood_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodRequest request = requests.get(position);

        holder.tvBloodGroup.setText(request.getBloodGroup());
        holder.tvUserName.setText(request.getUserName());
        holder.tvCity.setText(request.getCity());
        holder.tvMessage.setText(request.getMessage());
        holder.tvUrgency.setText(request.getUrgency().toUpperCase());

        // Set urgency circle background color based on level
        int urgencyColor;
        switch (request.getUrgency().toLowerCase()) {
            case "high":
                urgencyColor = android.graphics.Color.parseColor("#D32F2F"); // Red
                break;
            case "medium":
                urgencyColor = android.graphics.Color.parseColor("#FF9800"); // Orange
                break;
            default: // low
                urgencyColor = android.graphics.Color.parseColor("#4CAF50"); // Green
                break;
        }

        // Apply color tint to the circular background
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            holder.tvUrgency.setBackgroundTintList(android.content.res.ColorStateList.valueOf(urgencyColor));
        }

        // Phone click listener
        holder.tvPhone.setText("📞 Call");
        holder.tvPhone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + request.getUserPhone()));
            context.startActivity(intent);
        });

        // Chat button click listener
        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", request.getUserId());
            intent.putExtra("userName", request.getUserName());
            intent.putExtra("bloodGroup", request.getBloodGroup());
            intent.putExtra("city", request.getCity());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateData(List<BloodRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloodGroup, tvUserName, tvCity, tvMessage, tvUrgency, tvPhone, btnChat;

        ViewHolder(View itemView) {
            super(itemView);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}

