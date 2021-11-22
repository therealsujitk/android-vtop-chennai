package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import tk.therealsuji.vtopchennai.models.Attendance;

@Dao
public interface AttendanceDao {
    @Insert
    Completable insert(List<Attendance> attendance);
}
