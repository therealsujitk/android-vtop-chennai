package tk.therealsuji.vtopchennai.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.models.Attachment;

public class AttachmentItemAdapter extends RecyclerView.Adapter<AttachmentItemAdapter.ViewHolder> {
    List<Attachment> attachments;

    public AttachmentItemAdapter(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    @NonNull
    @Override
    public AttachmentItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout attachment = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_attachment, parent, false);

        return new ViewHolder(attachment);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setReceipt(this.attachments.get(position));
    }

    @Override
    public int getItemCount() {
        return this.attachments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout attachment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.attachment = (LinearLayout) itemView;
        }

        public void setReceipt(Attachment attachment) {
            TextView fileName = this.attachment.findViewById(R.id.text_view_file_name);
            TextView fileSize = this.attachment.findViewById(R.id.text_view_file_size);

            float fileSizeFloat = attachment.size;
            String fileSizeString = String.format(Locale.getDefault(), "%.1f B", fileSizeFloat);

            fileSizeFloat /= 1000;

            if (fileSizeFloat > 1) {
                fileSizeString = String.format(Locale.getDefault(), "%.1f KB", fileSizeFloat);

                fileSizeFloat /= 1000;

                if (fileSizeFloat > 1) {
                    fileSizeString = String.format(Locale.getDefault(), "%.1f MB", fileSizeFloat);
                }
            }

            fileName.setText(attachment.name);
            fileSize.setText(fileSizeString);

            this.attachment.findViewById(R.id.image_button_download).setOnClickListener(view -> {
                Context applicationContext = this.attachment.getContext().getApplicationContext();
                String moodleToken = SettingsRepository
                        .getSharedPreferences(applicationContext)
                        .getString("moodleToken", "");
                Uri uri = Uri.parse(attachment.url)
                        .buildUpon()
                        .appendQueryParameter("token", moodleToken)
                        .build();

                SettingsRepository.downloadFile(applicationContext, attachment.name, attachment.mimetype, uri);
            });
        }
    }
}
