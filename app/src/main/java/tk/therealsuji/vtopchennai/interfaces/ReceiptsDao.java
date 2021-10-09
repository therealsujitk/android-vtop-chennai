package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import tk.therealsuji.vtopchennai.models.Receipt;

@Dao
public interface ReceiptsDao {
    @Insert
    Completable insert(List<Receipt> receipts);

    @Query("DELETE FROM receipts")
    Completable deleteAll();
}
