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
 * ┬─── Profile Announcements Hierarchy
 * ├─ {@link ProfileFragment}
 * ╰→ {@link AnnouncementItemAdapter}    - RecyclerView (Current File)
 */
public class AnnouncementItemAdapter extends RecyclerView.Adapter<AnnouncementItemAdapter.ViewHolder> {
    ProfileFragment.ItemData[] announcementItems;

    public AnnouncementItemAdapter(ProfileFragment.ItemData[] announcementItems) {
        this.announcementItems = announcementItems;
    }

    @NonNull
    @Override
    public AnnouncementItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout announcementItem = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_announcement, parent, false);

        return new ViewHolder(announcementItem);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementItemAdapter.ViewHolder holder, int position) {
        holder.setItem(this.announcementItems[position]);

        if (position == this.getItemCount() - 1) {
            holder.setMargins(20, 10, 20, 10);
        }
    }

    @Override
    public int getItemCount() {
        return this.announcementItems.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout announcementItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.announcementItem = (LinearLayout) itemView;
        }

        public void setItem(ProfileFragment.ItemData announcementItem) {
            ImageView icon = this.announcementItem.findViewById(R.id.image_view_icon);
            TextView title = this.announcementItem.findViewById(R.id.text_view_title);
            TextView description = this.announcementItem.findViewById(R.id.text_view_description);

            icon.setImageDrawable(ContextCompat.getDrawable(this.announcementItem.getContext(), announcementItem.iconId));
            title.setText(announcementItem.title);
            description.setText(announcementItem.description);

            if (announcementItem.onClickListener != null) {
                this.announcementItem.setClickable(true);
                this.announcementItem.setFocusable(true);
                this.announcementItem.setOnClickListener(view -> announcementItem.onClickListener.onClick(this.announcementItem.getContext()));
            }
        }

        public void setMargins(int start, int top, int end, int bottom) {
            float pixelDensity = this.announcementItem.getContext().getResources().getDisplayMetrics().density;

            start = (int) (start * pixelDensity);
            top = (int) (top * pixelDensity);
            end = (int) (end * pixelDensity);
            bottom = (int) (bottom * pixelDensity);

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) this.announcementItem.getLayoutParams();
            layoutParams.setMargins(0, top, 0, bottom);
            layoutParams.setMarginStart(start);
            layoutParams.setMarginEnd(end);

            this.announcementItem.setLayoutParams(layoutParams);
        }
    }
}
