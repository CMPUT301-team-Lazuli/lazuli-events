package com.example.lazuli_events.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.lazuli_events.R;
import java.util.ArrayList;
import javax.annotation.Nullable;

public class EntrantListAdapter extends ArrayAdapter<Entrant> {

    public EntrantListAdapter(Context context, ArrayList<Entrant> entrantList) {
        super(context, 0, entrantList);
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.entrant_list_item, parent, false);
        }

        Entrant entrant = getItem(position);
        TextView textView = itemView.findViewById(R.id.entrant_list_textView);
        TextView badgeView = itemView.findViewById(R.id.entrant_status_badge);

        if (entrant != null) {
            textView.setText(entrant.getProfile().getName());
            String status = entrant.getStatus();

            // change background color if selected
            if (entrant.isSelected()) {
                itemView.setBackgroundColor(Color.parseColor("#E0E0E0")); // highlight grey
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT); // normal
            }

            if (status != null && !status.isEmpty()) {
                badgeView.setText(status.toLowerCase());
                badgeView.setVisibility(View.VISIBLE);

                if (status.equalsIgnoreCase("accepted")) {
                    badgeView.setTextColor(Color.parseColor("#4CAF50")); // green
                } else if (status.equalsIgnoreCase("cancelled") || status.equalsIgnoreCase("declined")) {
                    badgeView.setTextColor(Color.parseColor("#F44336")); // red
                } else {
                    badgeView.setTextColor(Color.parseColor("#FF9800")); // orange
                }
            } else {
                badgeView.setVisibility(View.GONE);
            }
        }
        return itemView;
    }
}