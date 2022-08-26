package com.ashish.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import com.ashish.vtopchennai.models.Attendance;

@Dao
public interface AttendanceDao {
    @Insert
    Completable insert(List<Attendance> attendance);

    @Query("DELETE FROM assignments")
    Completable delete();
}
