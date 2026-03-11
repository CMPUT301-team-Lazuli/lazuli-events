package com.example.lazuli_events.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lazuli_events.R;

import java.util.ArrayList;

public class NotificationListAdapter extends ArrayAdapter<UserNotification> {

    public NotificationListAdapter(Context context, ArrayList<UserNotification> notifications) {
        super(context, 0, notifications);
    }

    @NonNull
    @Override
    @SuppressLint("ViewHolder")
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = LayoutInflater.from(getContext()).inflate(R.layout.notification_list_item, parent, false);

        UserNotification notification = getItem(position);

        TextView notificationTextView = listItem.findViewById(R.id.notification_list_textView);

        if (notification != null) {
            notificationTextView.setText(notification.getTitle() + ": " + notification.getMessage());
        }

        return listItem;
    }
}