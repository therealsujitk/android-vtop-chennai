package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
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
}
