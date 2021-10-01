package tk.therealsuji.vtopchennai.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tk.therealsuji.vtopchennai.widgets.ProfileItem;

public class ProfileItemAdapter extends RecyclerView.Adapter<ProfileItemAdapter.ViewHolder> {
    int profileGroupIndex;

    public ProfileItemAdapter(int profileGroupIndex) {
        this.profileGroupIndex = profileGroupIndex;
    }

    @NonNull
    @Override
    public ProfileItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProfileItem profileItem = new ProfileItem(parent.getContext());
        return new ViewHolder(profileItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileItemAdapter.ViewHolder holder, int position) {
        holder.initializeProfileItem(this.profileGroupIndex, position);
    }

    @Override
    public int getItemCount() {
        return ProfileItem.PROFILE_ITEMS[profileGroupIndex].length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ProfileItem profileItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.profileItem = (ProfileItem) itemView;
        }

        public void initializeProfileItem(int profileGroupIndex, int profileItemIndex) {
            this.profileItem.initializeProfileItem(profileGroupIndex, profileItemIndex);
        }
    }
}
