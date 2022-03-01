package tk.therealsuji.vtopchennai.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tk.therealsuji.vtopchennai.models.Spotlight;
import tk.therealsuji.vtopchennai.widgets.SpotlightGroup;

/**
 * ┬─── Spotlight Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.RecyclerViewFragment}
 * ├─ {@link SpotlightGroupAdapter}     - RecyclerView (Current File)
 * ╰→ {@link SpotlightItemAdapter}      - RecyclerView
 */
public class SpotlightGroupAdapter extends RecyclerView.Adapter<SpotlightGroupAdapter.ViewHolder> {
    private final List<String> categories;
    private final Map<String, List<Spotlight>> announcements;

    public SpotlightGroupAdapter(List<Spotlight> spotlight) {
        this.announcements = new HashMap<>();
        this.categories = new ArrayList<>();

        for (int i = 0; i < spotlight.size(); ++i) {
            Spotlight announcement = spotlight.get(i);

            if (!this.announcements.containsKey(announcement.category)) {
                this.announcements.put(announcement.category, new ArrayList<>());
                this.categories.add(announcement.category);
            }

            Objects.requireNonNull(this.announcements.get(announcement.category)).add(announcement);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SpotlightGroup spotlightGroup = new SpotlightGroup(parent.getContext());
        return new ViewHolder(spotlightGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.setSpotlightSection(category, announcements.get(category));

        if (position == this.getItemCount() - 1) {
            holder.setPaddingBottom(0);
        }
    }

    @Override
    public int getItemCount() {
        if (this.categories == null) {
            return 0;
        }

        return this.categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        SpotlightGroup spotlightGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.spotlightGroup = (SpotlightGroup) itemView;
        }

        public void setSpotlightSection(String category, List<Spotlight> announcements) {
            this.spotlightGroup.setSpotlightSection(category, announcements);
        }

        public void setPaddingBottom(int paddingBottom) {
            this.spotlightGroup.setPadding(0, 0, 0, paddingBottom);
        }
    }
}
