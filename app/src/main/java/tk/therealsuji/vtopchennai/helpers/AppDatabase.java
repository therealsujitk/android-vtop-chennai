package tk.therealsuji.vtopchennai.helpers;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import tk.therealsuji.vtopchennai.interfaces.AssignmentsDao;
import tk.therealsuji.vtopchennai.interfaces.AttendanceDao;
import tk.therealsuji.vtopchennai.interfaces.CoursesDao;
import tk.therealsuji.vtopchennai.interfaces.MarksDao;
import tk.therealsuji.vtopchennai.interfaces.ReceiptsDao;
import tk.therealsuji.vtopchennai.interfaces.SpotlightDao;
import tk.therealsuji.vtopchennai.interfaces.StaffDao;
import tk.therealsuji.vtopchennai.interfaces.TimetableDao;
import tk.therealsuji.vtopchennai.models.Assignment;
import tk.therealsuji.vtopchennai.models.Attachment;
import tk.therealsuji.vtopchennai.models.Attendance;
import tk.therealsuji.vtopchennai.models.Course;
import tk.therealsuji.vtopchennai.models.CumulativeMark;
import tk.therealsuji.vtopchennai.models.Mark;
import tk.therealsuji.vtopchennai.models.Receipt;
import tk.therealsuji.vtopchennai.models.Slot;
import tk.therealsuji.vtopchennai.models.Spotlight;
import tk.therealsuji.vtopchennai.models.Staff;
import tk.therealsuji.vtopchennai.models.Timetable;

@Database(
        entities = {
                Assignment.class,
                Attachment.class,
                Attendance.class,
                Course.class,
                CumulativeMark.class,
                Mark.class,
                Receipt.class,
                Slot.class,
                Spotlight.class,
                Staff.class,
                Timetable.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "vtop")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }

    public abstract AssignmentsDao assignmentsDao();

    public abstract AttendanceDao attendanceDao();

    public abstract CoursesDao coursesDao();

    public abstract MarksDao marksDao();

    public abstract ReceiptsDao receiptsDao();

    public abstract SpotlightDao spotlightDao();

    public abstract StaffDao staffDao();

    public abstract TimetableDao timetableDao();
}
