package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import tk.therealsuji.vtopchennai.models.Staff;

@Dao
public interface StaffDao {
    @Insert
    Completable insert(List<Staff> staff);

    @Query("DELETE FROM staff")
    Completable deleteAll();
}
