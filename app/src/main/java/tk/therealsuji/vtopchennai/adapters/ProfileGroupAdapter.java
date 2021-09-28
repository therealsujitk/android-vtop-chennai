package tk.therealsuji.vtopchennai.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tk.therealsuji.vtopchennai.widgets.ProfileGroup;

public class ProfileGroupAdapter extends RecyclerView.Adapter<ProfileGroupAdapter.ViewHolder> {

    @NonNull
    @Override
    public ProfileGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProfileGroup profileGroup = new ProfileGroup(parent.getContext());
        return new ViewHolder(profileGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileGroupAdapter.ViewHolder holder, int position) {
        holder.initializeProfileGroup(position);
    }

    @Override
    public int getItemCount() {
        return ProfileGroup.PROFILE_GROUPS.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ProfileGroup profileGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.profileGroup = (ProfileGroup) itemView;
        }

        public void initializeProfileGroup(int profileGroupIndex) {
            this.profileGroup.initializeProfileGroup(profileGroupIndex);
        }
    }
}
