package com.example.lazuli_events.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.lazuli_events.R;

import java.util.ArrayList;

import javax.annotation.Nullable;

/**
 * This is an ArrayAdapter that handles the display of an entrant list item to a ListView.
 * It sets the list item's layout to display the attributes of each entrant object.
 */
public class EntrantListAdapter extends ArrayAdapter<String> {
    public EntrantListAdapter(Context context, ArrayList<String> entrantList) {
        super(context, 0, entrantList);
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // create the list item view for an entrant, to be added to the listView
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.entrant_list_item, parent, false);
        }

        // set the entrant item view to display the attributes of each entrant profile
        // displays their profile image and name
        String entrantText = getItem(position);
        TextView textView = itemView.findViewById(R.id.entrant_list_textView);
        if (entrantText != null) {
            textView.setText(entrantText);
        }

        return itemView;
    }
}
