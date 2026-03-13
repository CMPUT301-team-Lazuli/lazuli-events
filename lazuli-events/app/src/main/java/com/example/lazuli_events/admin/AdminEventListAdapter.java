package com.example.lazuli_events.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lazuli_events.R;
import com.example.lazuli_events.model.Event;
import java.util.ArrayList;

/**
 * Adapter for browsing Events and Images in the Admin Dashboard.
 * Fulfills US 03.01.01, 03.03.01, 03.06.01.
 */
public class AdminEventListAdapter extends RecyclerView.Adapter<AdminEventListAdapter.EventViewHolder> {

    private ArrayList<Event> eventList;
    private OnAdminEventActionListener actionListener;

    public interface OnAdminEventActionListener {
        void onDeleteEventClick(Event event, int position);
        void onDeleteImageClick(Event event, int position);
    }

    public AdminEventListAdapter(ArrayList<Event> eventList, OnAdminEventActionListener actionListener) {
        this.eventList = eventList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvName.setText(event.getName());
        holder.tvDetails.setText("Organizer ID: " + event.getOrganizerId());

        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            holder.tvHasImage.setVisibility(View.VISIBLE);
            holder.btnDeleteImage.setVisibility(View.VISIBLE);
        } else {
            holder.tvHasImage.setVisibility(View.GONE);
            holder.btnDeleteImage.setVisibility(View.GONE);
        }

        holder.btnDeleteEvent.setOnClickListener(v -> actionListener.onDeleteEventClick(event, position));
        holder.btnDeleteImage.setOnClickListener(v -> actionListener.onDeleteImageClick(event, position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvHasImage;
        Button btnDeleteImage, btnDeleteEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAdminEventName);
            tvDetails = itemView.findViewById(R.id.tvAdminEventDetails);
            tvHasImage = itemView.findViewById(R.id.tvHasImageLabel);
            btnDeleteImage = itemView.findViewById(R.id.btnAdminDeleteImage);
            btnDeleteEvent = itemView.findViewById(R.id.btnAdminDeleteEvent);
        }
    }
}