package tk.therealsuji.vtopchennai.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.Staff;

/**
 * ┬─── Spotlight Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.RecyclerViewFragment}
 * ├─ {@link StaffAdapter}      - ViewPager2
 * ╰→ {@link StaffItemAdapter}  - RecyclerView (Current File)
 */
public class StaffItemAdapter extends RecyclerView.Adapter<StaffItemAdapter.ViewHolder> {
    List<Staff> staff;

    public StaffItemAdapter(List<Staff> staff) {
        this.staff = staff;
    }

    @NonNull
    @Override
    public StaffItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout staffItem = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_staff, parent, false);

        return new ViewHolder(staffItem);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffItemAdapter.ViewHolder holder, int position) {
        holder.setStaffItem(staff.get(position));
    }

    @Override
    public int getItemCount() {
        return staff.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout staffItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.staffItem = (LinearLayout) itemView;
        }

        public void setStaffItem(Staff staffItem) {
            TextView key = this.staffItem.findViewById(R.id.text_view_key);
            TextView value = this.staffItem.findViewById(R.id.text_view_value);
            ImageView icon = this.staffItem.findViewById(R.id.image_view_icon);

            key.setText(staffItem.key);
            value.setText(staffItem.value);

            if (staffItem.key.toLowerCase().contains("mobile") || staffItem.key.toLowerCase().contains("phone")) {
                this.staffItem.setClickable(true);
                this.staffItem.setFocusable(true);

                icon.setImageDrawable(ContextCompat.getDrawable(this.staffItem.getContext(), R.drawable.ic_phone));
                icon.setVisibility(View.VISIBLE);

                this.staffItem.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + staffItem.value));
                    view.getContext().startActivity(intent);
                });

                this.staffItem.setOnLongClickListener(view -> {
                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Mobile number", staffItem.value);
                    clipboard.setPrimaryClip(clipData);
                    Toast.makeText(view.getContext(), "Mobile number copied to clipboard", Toast.LENGTH_SHORT).show();
                    return true;
                });
            } else if (staffItem.key.toLowerCase().contains("mail")) {
                this.staffItem.setClickable(true);
                this.staffItem.setFocusable(true);

                icon.setImageDrawable(ContextCompat.getDrawable(this.staffItem.getContext(), R.drawable.ic_email));
                icon.setVisibility(View.VISIBLE);

                this.staffItem.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + staffItem.value));
                    view.getContext().startActivity(intent);
                });

                this.staffItem.setOnLongClickListener(view -> {
                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Email", staffItem.value);
                    clipboard.setPrimaryClip(clipData);
                    Toast.makeText(view.getContext(), "Email copied to clipboard", Toast.LENGTH_SHORT).show();
                    return true;
                });
            }
        }
    }
}
