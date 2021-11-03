package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Staff;

@Dao
public interface StaffDao {
    @Insert
    Completable insert(List<Staff> staff);

    @Query("DELETE FROM staff")
    Completable deleteAll();

    @Query("SELECT * FROM staff WHERE type = :staffType AND value IS NOT NULL")
    Single<List<Staff>> getStaff(String staffType);

    @Query("SELECT DISTINCT type FROM staff WHERE value IS NOT NULL")
    Single<List<String>> getStaffTypes();
}
