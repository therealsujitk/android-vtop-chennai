package tk.therealsuji.vtopchennai.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.models.Spotlight;
import tk.therealsuji.vtopchennai.widgets.SpotlightItem;

public class SpotlightItemAdapter extends RecyclerView.Adapter<SpotlightItemAdapter.ViewHolder> {
    private final float pixelDensity;
    private List<Spotlight> announcements;

    public SpotlightItemAdapter(Context context) {
        this.pixelDensity = context.getResources().getDisplayMetrics().density;
    }

    public void setAnnouncements(List<Spotlight> announcements) {
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SpotlightItem spotlightItem = new SpotlightItem(parent.getContext());

        return new ViewHolder(spotlightItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.initializeAnnouncement(announcements.get(position));

        int left = (int) (20 * this.pixelDensity);
        int top = (int) (2 * this.pixelDensity);
        int right = (int) (20 * this.pixelDensity);
        int bottom = (int) (2 * this.pixelDensity);

        if (announcements.size() == 1) {
            holder.setSingle();
            bottom = (int) (20 * this.pixelDensity);
        } else if (position == 0) {
            holder.setFirst();
        } else if (position == announcements.size() - 1) {
            holder.setLast();
            bottom = (int) (20 * this.pixelDensity);
        }

        holder.setMargins(left, top, right, bottom);
    }

    @Override
    public int getItemCount() {
        if (announcements == null) {
            return 0;
        }

        return announcements.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        SpotlightItem spotlightItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.spotlightItem = (SpotlightItem) itemView;
        }

        public void initializeAnnouncement(Spotlight announcement) {
            this.spotlightItem.setAnnouncement(announcement.announcement);
            this.spotlightItem.setLink(announcement.link);
        }

        public void setMargins(int left, int top, int right, int bottom) {
            this.spotlightItem.setMargins(left, top, right, bottom);
        }

        public void setFirst() {
            this.spotlightItem.setFirst();
        }

        public void setLast() {
            this.spotlightItem.setLast();
        }

        public void setSingle() {
            this.spotlightItem.setSingle();
        }
    }
}
