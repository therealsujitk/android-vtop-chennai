package tk.therealsuji.vtopchennai.helpers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import tk.therealsuji.vtopchennai.interfaces.AssignmentsDao;
import tk.therealsuji.vtopchennai.interfaces.AttendanceDao;
import tk.therealsuji.vtopchennai.interfaces.CoursesDao;
import tk.therealsuji.vtopchennai.interfaces.ExamsDao;
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
import tk.therealsuji.vtopchennai.models.Exam;
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
                Exam.class,
                Mark.class,
                Receipt.class,
                Slot.class,
                Spotlight.class,
                Staff.class,
                Timetable.class
        },
        version = 3,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2),
        }
)
public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "vit_student")
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }

    public static synchronized void deleteDatabase(Context context) {
        if (instance != null) {
            instance.close();
        }

        instance = null;
        context.deleteDatabase("vit_student");
        context.deleteDatabase("vtop"); // Delete the deprecated database (used till < v4.0)
    }

    public abstract AssignmentsDao assignmentsDao();

    public abstract AttendanceDao attendanceDao();

    public abstract CoursesDao coursesDao();

    public abstract ExamsDao examsDao();

    public abstract MarksDao marksDao();

    public abstract ReceiptsDao receiptsDao();

    public abstract SpotlightDao spotlightDao();

    public abstract StaffDao staffDao();

    public abstract TimetableDao timetableDao();

    // Manual Migrations
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE receipts RENAME TO receipts_old");
            database.execSQL("CREATE TABLE receipts (number INTEGER NOT NULL PRIMARY KEY, amount REAL, date INTEGER)");
            database.execSQL("INSERT INTO receipts (number, amount) SELECT number, amount FROM receipts_old");
            database.execSQL("UPDATE receipts SET date = 0");
            database.execSQL("DROP TABLE receipts_old");
        }
    };
}
