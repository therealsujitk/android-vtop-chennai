package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Spotlight;

@Dao
public interface SpotlightDao {
    @Insert
    Completable insert(List<Spotlight> spotlight);

    @Query("DELETE FROM spotlight")
    Completable deleteAll();

    @Query("SELECT * FROM spotlight")
    Single<List<Spotlight>> getSpotlight();
}
