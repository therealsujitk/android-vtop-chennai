package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Spotlight;

@Dao
public interface SpotlightDao {
    default Completable insert(Map<Integer, Spotlight> spotlight) {

        return this.getRead()
                .concatMapCompletable(readSpotlight -> {
                    for (Spotlight spotlightItem : readSpotlight) {
                        if (spotlight.containsKey(spotlightItem.signature)) {
                            Objects.requireNonNull(spotlight.get(spotlightItem.signature)).isRead = true;
                        }
                    }

                    return this.delete().andThen(this.insert(new ArrayList<>(spotlight.values())));
                });
    }

    @Insert
    Completable insert(List<Spotlight> spotlight);

    @Query("UPDATE spotlight SET is_read = 1 WHERE is_read IS 0")
    Completable setRead();

    @Query("DELETE FROM spotlight")
    Completable delete();

    @Query("SELECT id, signature FROM spotlight WHERE is_read IS 1")
    Single<List<Spotlight>> getRead();

    @Query("SELECT COUNT(id) FROM spotlight WHERE is_read IS 0")
    Single<Integer> getUnreadCount();

    @Query("SELECT id, announcement, category, link, is_read FROM spotlight")
    Single<List<Spotlight>> get();
}
