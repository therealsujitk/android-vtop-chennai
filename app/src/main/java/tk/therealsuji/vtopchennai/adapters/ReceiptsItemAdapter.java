package tk.therealsuji.vtopchennai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.Receipt;

/**
 * ┬─── Receipts Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.RecyclerViewFragment}
 * ╰→ {@link ReceiptsItemAdapter}   - RecyclerView (Current File)
 */
public class ReceiptsItemAdapter extends RecyclerView.Adapter<ReceiptsItemAdapter.ViewHolder> {
    List<Receipt> receipts;

    public ReceiptsItemAdapter(List<Receipt> receipts) {
        this.receipts = receipts;
    }

    @NonNull
    @Override
    public ReceiptsItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout receipt = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_receipts, parent, false);

        return new ViewHolder(receipt);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptsItemAdapter.ViewHolder holder, int position) {
        holder.setReceipt(this.receipts.get(position));
    }

    @Override
    public int getItemCount() {
        return receipts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout receipt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.receipt = (LinearLayout) itemView;
        }

        public void setReceipt(Receipt receipt) {
            TextView number = this.receipt.findViewById(R.id.text_view_receipt_number);
            TextView amount = this.receipt.findViewById(R.id.text_view_amount);
            TextView date = this.receipt.findViewById(R.id.text_view_date);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

            number.setText(String.valueOf(receipt.number));
            amount.setText(new DecimalFormat("₹ #.00/-").format(receipt.amount));
            date.setText(dateFormat.format(receipt.date).toUpperCase(Locale.ENGLISH));
        }
    }
}
