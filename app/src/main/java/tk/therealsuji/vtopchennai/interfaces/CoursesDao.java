package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Course;
import tk.therealsuji.vtopchennai.models.Slot;

@Dao
public interface CoursesDao {
    @Insert
    Completable insertCourses(List<Course> courses);

    @Insert
    Completable insertSlots(List<Slot> slots);

    @Query("DELETE FROM courses")
    Completable deleteAll();

    @Query("SELECT title AS courseTitle, code AS courseCode, type AS courseType, faculty, slot, venue, attendance.percentage AS attendance " +
            "FROM slots, courses, attendance WHERE slots.id = :slotId AND courses.id = slots.course_id AND attendance.course_id = courses.id")
    Single<Course.AllData> getCourse(int slotId);
}
