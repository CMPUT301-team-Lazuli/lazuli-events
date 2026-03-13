package com.example.lazuli_events.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lazuli_events.R;
import com.example.lazuli_events.notifications.UserNotification;
import java.util.ArrayList;

/**
 * Adapter for browsing Notification Logs in the Admin Dashboard via a RecyclerView.
 * Fulfills US 03.08.01.
 */
public class AdminNotificationListAdapter extends RecyclerView.Adapter<AdminNotificationListAdapter.LogViewHolder> {

    private ArrayList<UserNotification> notificationList;

    public AdminNotificationListAdapter(ArrayList<UserNotification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        UserNotification notification = notificationList.get(position);
        String logText = "To: " + notification.getRecipientId() + "\n"
                + notification.getTitle() + ": " + notification.getMessage();
        holder.tvLogDetails.setText(logText);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogDetails;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogDetails = itemView.findViewById(R.id.notification_list_textView);
        }
    }
}