package com.example.lazuli_events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;


// adapt event thumbnail cards to a listview on the explore page
public class ExploreEventsCardAdapter extends ArrayAdapter<String> {

    public ExploreEventsCardAdapter(Context context, ArrayList<String> eventList) {
        super(context, 0, eventList);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // create a card view for the event, to be added to the listview
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.explore_events_card, parent, false);
        }

        // TODO:
        // set the card view to display the attributes of the event object
        // such as its image, title, date and time
        String cardText = getItem(position);
        TextView textView = itemView.findViewById(R.id.browse_events_card_textView);
        if (cardText != null) {
            textView.setText(cardText);
        }


        // template reference from my emotilog project:

//        Mood mood = getItem(position);
//
//        // set the text and image of the card to the Mood object's attributes
//        TextView textView = itemView.findViewById(R.id.card_text_view);
//        ImageView imageView = itemView.findViewById(R.id.card_image_view);
//
//        if (mood != null) {
//            textView.setText(mood.getMood());
//            imageView.setImageResource(mood.getImage());
//        }

        return itemView;

    }
}
