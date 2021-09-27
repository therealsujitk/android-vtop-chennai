package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Marks;

@Dao
public interface MarksDao {
    @Query("SELECT id, course FROM marks GROUP BY course")
    Single<List<Marks>> getCourses();

    @Query("SELECT * FROM marks WHERE course = :course ORDER BY type DESC, title")
    Single<List<Marks>> getMarks(String course);
}
