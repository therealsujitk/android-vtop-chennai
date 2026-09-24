package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Timetable;

@Dao
public interface TimetableDao {
    default Single<List<Timetable.AllData>> get(int day) {
        switch (day) {
            case 1:
                return getMonday();
            case 2:
                return getTuesday();
            case 3:
                return getWednesday();
            case 4:
                return getThursday();
            case 5:
                return getFriday();
            case 6:
                return getSaturday();
            default:
                return getSunday();
        }
    }

    default List<Timetable.AllData> getForWidgetSync(int day) {
        switch (day) {
            case 1:
                return getMondayForWidget();
            case 2:
                return getTuesdayForWidget();
            case 3:
                return getWednesdayForWidget();
            case 4:
                return getThursdayForWidget();
            case 5:
                return getFridayForWidget();
            case 6:
                return getSaturdayForWidget();
            default:
                return getSundayForWidget();
        }
    }

    default Single<Timetable.AllData> getOngoing(int day, String currentTime) {
        switch (day) {
            case 1:
                return getMondayOngoing(currentTime);
            case 2:
                return getTuesdayOngoing(currentTime);
            case 3:
                return getWednesdayOngoing(currentTime);
            case 4:
                return getThursdayOngoing(currentTime);
            case 5:
                return getFridayOngoing(currentTime);
            case 6:
                return getSaturdayOngoing(currentTime);
            default:
                return getSundayOngoing(currentTime);
        }
    }

    default Single<Timetable.AllData> getUpcoming(int day, String currentTime, String futureTime) {
        switch (day) {
            case 1:
                return getMondayUpcoming(currentTime, futureTime);
            case 2:
                return getTuesdayUpcoming(currentTime, futureTime);
            case 3:
                return getWednesdayUpcoming(currentTime, futureTime);
            case 4:
                return getThursdayUpcoming(currentTime, futureTime);
            case 5:
                return getFridayUpcoming(currentTime, futureTime);
            case 6:
                return getSaturdayUpcoming(currentTime, futureTime);
            default:
                return getSundayUpcoming(currentTime, futureTime);
        }
    }

    @Insert
    Completable insert(List<Timetable> timetable);

    @Query("DELETE FROM timetable")
    Completable deleteAll();

    @Query("SELECT * FROM timetable")
    Single<List<Timetable>> getTimetable();

    /*
        Get timetable for a particular day
     */
    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, percentage AS attendancePercentage " +
            "FROM timetable, slots, courses, attendance WHERE sunday = slots.id AND slots.course_id = courses.id AND attendance.course_id = courses.id")
    Single<List<Timetable.AllData>> getSunday();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, percentage AS attendancePercentage " +
            "FROM timetable, slots, courses, attendance WHERE monday = slots.id AND slots.course_id = courses.id AND attendance.course_id = courses.id")
    Single<List<Timetable.AllData>> getMonday();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, percentage AS attendancePercentage " +
            "FROM timetable, slots, courses, attendance WHERE tuesday = slots.id AND slots.course_id = courses.id AND attendance.course_id = courses.id")
    Single<List<Timetable.AllData>> getTuesday();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, percentage AS attendancePercentage " +
            "FROM timetable, slots, courses, attendance WHERE wednesday = slots.id AND slots.course_id = courses.id AND attendance.course_id = courses.id")
    Single<List<Timetable.AllData>> getWednesday();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, percentage AS attendancePercentage " +
            "FROM timetable, slots, courses, attendance WHERE thursday = slots.id AND slots.course_id = courses.id AND attendance.course_id = courses.id")
    Single<List<Timetable.AllData>> getThursday();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, percentage AS attendancePercentage " +
            "FROM timetable, slots, courses, attendance WHERE friday = slots.id AND slots.course_id = courses.id AND attendance.course_id = courses.id")
    Single<List<Timetable.AllData>> getFriday();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, percentage AS attendancePercentage " +
            "FROM timetable, slots, courses, attendance WHERE saturday = slots.id AND slots.course_id = courses.id AND attendance.course_id = courses.id")
    Single<List<Timetable.AllData>> getSaturday();


    /*
        Get the ongoing class for a particular day
     */
    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :currentTime AND end_time > :currentTime AND sunday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getSundayOngoing(String currentTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :currentTime AND end_time > :currentTime AND monday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getMondayOngoing(String currentTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :currentTime AND end_time > :currentTime AND tuesday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getTuesdayOngoing(String currentTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :currentTime AND end_time > :currentTime AND wednesday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getWednesdayOngoing(String currentTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :currentTime AND end_time > :currentTime AND thursday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getThursdayOngoing(String currentTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :currentTime AND end_time > :currentTime AND friday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getFridayOngoing(String currentTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :currentTime AND end_time > :currentTime AND saturday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getSaturdayOngoing(String currentTime);


    /*
        Get the upcoming class for a particular day
     */
    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :futureTime AND start_time > :currentTime AND sunday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getSundayUpcoming(String currentTime, String futureTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :futureTime AND start_time > :currentTime AND monday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getMondayUpcoming(String currentTime, String futureTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :futureTime AND start_time > :currentTime AND tuesday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getTuesdayUpcoming(String currentTime, String futureTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :futureTime AND start_time > :currentTime AND wednesday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getWednesdayUpcoming(String currentTime, String futureTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :futureTime AND start_time > :currentTime AND thursday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getThursdayUpcoming(String currentTime, String futureTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :futureTime AND start_time > :currentTime AND friday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getFridayUpcoming(String currentTime, String futureTime);

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle  " +
            "FROM timetable, slots, courses WHERE start_time <= :futureTime AND start_time > :currentTime AND saturday = slots.id AND course_id = courses.id")
    Single<Timetable.AllData> getSaturdayUpcoming(String currentTime, String futureTime);


    /*
        Get timetable for a particular day (for widget - synchronous with venue)
     */
    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle, venue " +
            "FROM timetable, slots, courses WHERE sunday = slots.id AND slots.course_id = courses.id ORDER BY start_time")
    List<Timetable.AllData> getSundayForWidget();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle, venue " +
            "FROM timetable, slots, courses WHERE monday = slots.id AND slots.course_id = courses.id ORDER BY start_time")
    List<Timetable.AllData> getMondayForWidget();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle, venue " +
            "FROM timetable, slots, courses WHERE tuesday = slots.id AND slots.course_id = courses.id ORDER BY start_time")
    List<Timetable.AllData> getTuesdayForWidget();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle, venue " +
            "FROM timetable, slots, courses WHERE wednesday = slots.id AND slots.course_id = courses.id ORDER BY start_time")
    List<Timetable.AllData> getWednesdayForWidget();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle, venue " +
            "FROM timetable, slots, courses WHERE thursday = slots.id AND slots.course_id = courses.id ORDER BY start_time")
    List<Timetable.AllData> getThursdayForWidget();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle, venue " +
            "FROM timetable, slots, courses WHERE friday = slots.id AND slots.course_id = courses.id ORDER BY start_time")
    List<Timetable.AllData> getFridayForWidget();

    @Query("SELECT slots.id AS slotId, courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode, title AS courseTitle, venue " +
            "FROM timetable, slots, courses WHERE saturday = slots.id AND slots.course_id = courses.id ORDER BY start_time")
    List<Timetable.AllData> getSaturdayForWidget();
}
