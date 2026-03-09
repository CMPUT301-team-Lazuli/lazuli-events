package com.example.lazuli_events.notifications;

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

/**
 * This is an ArrayAdapter that handles the display of a notification list item to a ListView.
 * It sets the list item's layout to display the attributes of each notification object.
 */
public class NotificationListAdapter extends ArrayAdapter<String> {
    public NotificationListAdapter(Context context, ArrayList<String> eventList) {
        super(context, 0, eventList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        // create the list item view for a notification, to be added to the listview
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.notification_list_item, parent, false);
        }

        // TODO:
        // set the notification view to display the attributes of the notification object
        // such as its image, title, and subtitle
        String notificationText = getItem(position);
        TextView textView = itemView.findViewById(R.id.notification_list_textView);
        if (notificationText != null) {
            textView.setText(notificationText);
        }

        return itemView;
    }
}
