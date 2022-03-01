package tk.therealsuji.vtopchennai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.fragments.ProfileFragment;

/**
 * ┬─── Profile Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.ProfileFragment}
 * ├─ {@link ProfileGroupAdapter}   - RecyclerView
 * ╰→ {@link ProfileItemAdapter}    - RecyclerView (Current File)
 */
public class ProfileItemAdapter extends RecyclerView.Adapter<ProfileItemAdapter.ViewHolder> {
    ProfileFragment.ItemData[] profileItems;

    public ProfileItemAdapter(ProfileFragment.ItemData[] profileItems) {
        this.profileItems = profileItems;
    }

    @NonNull
    @Override
    public ProfileItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout profileItem = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_profile, parent, false);

        return new ViewHolder(profileItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileItemAdapter.ViewHolder holder, int position) {
        holder.setProfileItem(this.profileItems[position]);
    }

    @Override
    public int getItemCount() {
        return this.profileItems.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout profileItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.profileItem = (LinearLayout) itemView;
        }

        public void setProfileItem(ProfileFragment.ItemData profileItem) {
            ImageView icon = this.profileItem.findViewById(R.id.image_view_icon);
            TextView title = this.profileItem.findViewById(R.id.text_view_title);

            icon.setImageDrawable(ContextCompat.getDrawable(this.profileItem.getContext(), profileItem.iconId));
            title.setText(profileItem.titleId);
            this.profileItem.setOnClickListener(view -> profileItem.onClickListener.onClick(this.profileItem.getContext()));

            if (profileItem.onInitListener != null) {
                profileItem.onInitListener.onInit(this.profileItem);
            }
        }
    }
}
