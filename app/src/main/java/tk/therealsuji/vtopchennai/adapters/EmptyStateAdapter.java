package tk.therealsuji.vtopchennai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import tk.therealsuji.vtopchennai.R;

public class EmptyStateAdapter extends RecyclerView.Adapter<EmptyStateAdapter.ViewHolder> {
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_FETCHING_ASSIGNMENTS = 2;
    public static final int TYPE_NO_ASSIGNMENTS = 3;
    public static final int TYPE_NO_DATA = 4;
    public static final int TYPE_NO_PERFORMANCE = 5;
    public static final int TYPE_NO_TIMETABLE = 6;
    public static final int TYPE_NOT_AUTHENTICATED = 7;

    final int type;
    ButtonAttributes buttonAttributes;
    String message;

    public EmptyStateAdapter(int type) {
        this.type = type;
    }

    public EmptyStateAdapter(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public EmptyStateAdapter(int type, String message, ButtonAttributes buttonAttributes) {
        this.type = type;
        this.message = message;
        this.buttonAttributes = buttonAttributes;
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
            case TYPE_FETCHING_ASSIGNMENTS:
                holder.setImage(R.drawable.image_fetching_assignments);
                holder.setText(R.string.fetch_assignments);
                break;
            case TYPE_NO_ASSIGNMENTS:
                holder.setImage(R.drawable.image_no_assignments);
                holder.setText(R.string.no_assignments);
                break;
            case TYPE_NO_PERFORMANCE:
                holder.setImage(R.drawable.image_no_marks);
                holder.setText(R.string.no_marks);
                break;
            case TYPE_NO_TIMETABLE:
                holder.setImage(R.drawable.image_no_classes);
                holder.setText(R.string.no_classes);
                break;
            case TYPE_NOT_AUTHENTICATED:
                holder.setImage(R.drawable.image_not_authenticated);
                holder.setText(R.string.not_authenticated);
                break;
            default:
                holder.setImage(R.drawable.image_no_data);
                holder.setText(R.string.no_data);
        }

        if (this.message != null) {
            holder.setText(message);
        }

        holder.setButton(this.buttonAttributes);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public interface ButtonAttributes {
        void onClick();

        int getButtonTextId();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button button;
        TextView noData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.button = itemView.findViewById(R.id.button);
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

        public void setButton(ButtonAttributes buttonAttributes) {
            if (buttonAttributes == null) {
                return;
            }

            this.button.setOnClickListener(view -> buttonAttributes.onClick());
            this.button.setText(buttonAttributes.getButtonTextId());
            this.button.setVisibility(View.VISIBLE);
        }
    }
}
