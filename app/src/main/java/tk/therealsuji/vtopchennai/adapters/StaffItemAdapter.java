package tk.therealsuji.vtopchennai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.Staff;

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

            key.setText(staffItem.key);
            value.setText(staffItem.value);
        }
    }
}
