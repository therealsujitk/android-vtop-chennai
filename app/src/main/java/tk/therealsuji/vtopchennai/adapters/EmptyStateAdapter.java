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
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_NO_DATA = 2;
    public static final int TYPE_NO_PERFORMANCE = 3;
    public static final int TYPE_NO_TIMETABLE = 4;

    final int type;
    final String message;

    public EmptyStateAdapter(int type, String message) {
        this.type = type;
        this.message = message;
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
        switch (this.type) {
            case TYPE_ERROR:
                holder.setImage(R.drawable.image_error);
                break;
            case TYPE_NO_PERFORMANCE:
                holder.setImage(R.drawable.image_no_marks);
                holder.setText(R.string.no_marks);
                break;
            case TYPE_NO_TIMETABLE:
                holder.setImage(R.drawable.image_no_classes);
                holder.setText(R.string.no_classes);
                break;
            default:
                holder.setImage(R.drawable.image_no_data);
                holder.setText(R.string.no_data);
        }

        if (this.message != null) {
            holder.setText(message);
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

        public void setText(String text) {
            this.noData.setText(text);
        }
    }
}
