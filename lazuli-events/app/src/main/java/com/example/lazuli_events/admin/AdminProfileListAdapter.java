package com.example.lazuli_events.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lazuli_events.R;
import com.example.lazuli_events.profile.Profile;
import java.util.ArrayList;

/**
 * Adapter for the RecyclerView in the Admin Dashboard for browsing profiles.
 * Fulfills US 03.02.01, 03.05.01, 03.07.01.
 */
public class AdminProfileListAdapter extends RecyclerView.Adapter<AdminProfileListAdapter.ProfileViewHolder> {

    private ArrayList<Profile> profileList;
    private OnProfileDeleteListener deleteListener;

    public interface OnProfileDeleteListener {
        void onDeleteClick(Profile profile, int position);
    }

    public AdminProfileListAdapter(ArrayList<Profile> profileList, OnProfileDeleteListener deleteListener) {
        this.profileList = profileList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_profile_list_item, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        String name = (profile.getName() != null && !profile.getName().isEmpty()) ? profile.getName() : "Unknown User";
        holder.tvName.setText(name);
        holder.tvEmail.setText(profile.getEmail());
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(profile, position);
        });
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        ImageButton btnDelete;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAdminProfileName);
            tvEmail = itemView.findViewById(R.id.tvAdminProfileEmail);
            btnDelete = itemView.findViewById(R.id.btnDeleteProfile);
        }
    }
}