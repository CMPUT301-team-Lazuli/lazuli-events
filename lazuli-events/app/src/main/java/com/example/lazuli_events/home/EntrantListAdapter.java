package com.example.lazuli_events.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.lazuli_events.R;

import com.example.lazuli_events.MainActivity;

import java.util.ArrayList;

import javax.annotation.Nullable;

/**
 * This is an ArrayAdapter that handles the display of an entrant list item to a ListView.
 * It sets the list item's layout to display the attributes of each entrant object.
 */
public class EntrantListAdapter extends ArrayAdapter<String> {

    private boolean showDropButton = false; // determine if drop button should be visible
    public EntrantListAdapter(Context context, ArrayList<String> entrantList) {
        super(context, 0, entrantList);
    }

    // updates the adapter to show or hide the drop button
    public void setShowDropButton(boolean show) {
        this.showDropButton = show;
        notifyDataSetChanged();
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

        // get the drop button
        ImageButton dropButton = itemView.findViewById(R.id.entrant_drop_button);
        if (showDropButton) {
            dropButton.setVisibility(View.VISIBLE);
        } else {
            dropButton.setVisibility(View.GONE);
        }

        // handle drop button clicks
        dropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                // get the activity to access the global database
                MainActivity mainActivity = (MainActivity) getContext();

                // remove from Firestore
                mainActivity.firebaseDB.dropEntrantFromEvent("sample_event_id", entrantText);

                // remove from the UI
                remove(entrantText);
                notifyDataSetChanged();

                // notifications for the organizers
                Toast.makeText(getContext(), entrantText + " has been dropped.", Toast.LENGTH_SHORT).show();
            }
        });

        return itemView;
    }
}
