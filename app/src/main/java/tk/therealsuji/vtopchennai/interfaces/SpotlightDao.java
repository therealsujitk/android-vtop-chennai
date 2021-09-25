package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Spotlight;

@Dao
public interface SpotlightDao {
    @Query("SELECT * FROM spotlight")
    Single<List<Spotlight>> getSpotlight();
}
