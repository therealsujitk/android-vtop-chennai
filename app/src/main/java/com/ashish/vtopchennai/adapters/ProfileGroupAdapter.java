package com.ashish.vtopchennai.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.ashish.vtopchennai.fragments.ProfileFragment;
import com.ashish.vtopchennai.widgets.ProfileGroup;

/**
 * ┬─── Profile Hierarchy
 * ├─ {@link com.ashish.vtopchennai.fragments.ProfileFragment}
 * ├─ {@link ProfileGroupAdapter}   - RecyclerView (Current File)
 * ╰→ {@link ProfileItemAdapter}    - RecyclerView
 */
public class ProfileGroupAdapter extends RecyclerView.Adapter<ProfileGroupAdapter.ViewHolder> {
    int[] profileGroups;
    ProfileFragment.ItemData[][] profileItems;

    public ProfileGroupAdapter(int[] profileGroups, ProfileFragment.ItemData[][] profileItems) {
        this.profileGroups = profileGroups;
        this.profileItems = profileItems;
    }

    @NonNull
    @Override
    public ProfileGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProfileGroup profileGroup = new ProfileGroup(parent.getContext());
        return new ViewHolder(profileGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileGroupAdapter.ViewHolder holder, int position) {
        holder.initializeProfileGroup(this.profileGroups[position], this.profileItems[position]);
    }

    @Override
    public int getItemCount() {
        return this.profileGroups.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ProfileGroup profileGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.profileGroup = (ProfileGroup) itemView;
        }

        public void initializeProfileGroup(@StringRes int profileGroup, ProfileFragment.ItemData[] profileItems) {
            this.profileGroup.initializeProfileGroup(profileGroup, profileItems);
        }
    }
}
