package tk.therealsuji.vtopchennai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import tk.therealsuji.vtopchennai.R;

public class EmptyStateAdapter extends RecyclerView.Adapter<EmptyStateAdapter.ViewHolder> {
    public static final int TYPE_TIMETABLE = 1;
    public static final int TYPE_PERFORMANCE = 2;

    final int type;

    public EmptyStateAdapter(int type) {
        this.type = type;
    }

    @NonNull
    @Override
    public EmptyStateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View noData = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_empty_state, parent, false);

        return new ViewHolder(noData);
    }

    @Override
    public void onBindViewHolder(@NonNull EmptyStateAdapter.ViewHolder holder, int position) {
        if (this.type == TYPE_TIMETABLE) {
            holder.setImage(R.drawable.image_no_classes);
            holder.setText(R.string.no_classes);
        } else if (this.type == TYPE_PERFORMANCE) {
            holder.setImage(R.drawable.image_no_marks);
            holder.setText(R.string.no_marks);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView noData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.noData = itemView.findViewById(R.id.text_view_no_data);
        }

        public void setImage(@DrawableRes int drawableId) {
            this.noData.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    drawableId,
                    0,
                    0
            );
        }

        public void setText(@StringRes int stringId) {
            this.noData.setText(stringId);
        }
    }
}
