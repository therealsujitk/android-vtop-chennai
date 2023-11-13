package tk.therealsuji.vtopchennai.adapters;

import android.Manifest;
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
import java.util.Objects;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.fragments.AssignmentViewFragment;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.models.Attachment;

/**
 * ┬─── Assignment Attachments Hierarchy
 * ├─ {@link tk.therealsuji.vtopchennai.fragments.AssignmentsFragment}
 * ├─ {@link AssignmentViewFragment}
 * ╰→ {@link AttachmentsItemAdapter}     - RecyclerView (Current File)
 */
public class AttachmentsItemAdapter extends RecyclerView.Adapter<AttachmentsItemAdapter.ViewHolder> {
    List<Attachment> attachments;

    public AttachmentsItemAdapter(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    @NonNull
    @Override
    public AttachmentsItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout attachment = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_attachments, parent, false);

        return new ViewHolder(attachment);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setAttachment(this.attachments.get(position));
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

        public void setAttachment(Attachment attachment) {
            TextView fileName = this.attachment.findViewById(R.id.text_view_file_name);
            TextView fileSize = this.attachment.findViewById(R.id.text_view_file_size);

            float fileSizeFloat = attachment.size;
            String fileSizeString = String.format(Locale.ENGLISH, "%.1f B", fileSizeFloat);

            fileSizeFloat /= 1000;

            if (fileSizeFloat > 1) {
                fileSizeString = String.format(Locale.ENGLISH, "%.1f KB", fileSizeFloat);

                fileSizeFloat /= 1000;

                if (fileSizeFloat > 1) {
                    fileSizeString = String.format(Locale.ENGLISH, "%.1f MB", fileSizeFloat);
                }
            }

            fileName.setText(attachment.name);
            fileSize.setText(fileSizeString);

            if (attachment.url == null) {
                this.attachment.findViewById(R.id.image_button_download).setVisibility(View.GONE);
                this.attachment.findViewById(R.id.progress_bar_uploading).setVisibility(View.VISIBLE);
                return;
            }

            this.attachment.findViewById(R.id.image_button_download).setOnClickListener(view -> {
                Context context = this.attachment.getContext();
                Context applicationContext = context.getApplicationContext();

                if (!SettingsRepository.hasFileWritePermission(context)) {
                    ((MainActivity) context).getRequestPermissionLauncher().launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    return;
                }

                String moodleToken = Objects.requireNonNull(SettingsRepository
                                .getEncryptedSharedPreferences(applicationContext))
                        .getString("moodleToken", null);
                Uri uri = Uri.parse(attachment.url)
                        .buildUpon()
                        .appendQueryParameter("token", moodleToken)
                        .build();

                SettingsRepository.downloadFile(applicationContext, "Moodle", attachment.name, attachment.mimetype, uri, null);
            });
        }
    }
}
