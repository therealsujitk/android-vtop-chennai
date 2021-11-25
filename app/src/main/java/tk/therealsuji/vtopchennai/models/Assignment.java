package tk.therealsuji.vtopchennai.models;

import java.util.Date;
import java.util.List;

public class Assignment {
    public int activityId;
    public String course;
    public String title;
    public String intro;
    public List<Attachment> introAttachments;
    public Date dueDate;
    public Date cutoffDate;

    public static class Attachment {
        public String name;
        public String url;
        public int size;
    }
}
