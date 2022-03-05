package jp.developer.bbee.minegame;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScoreDao {
    @Query("SELECT * FROM score")
    List<Score> getAll();

    @Query("SELECT * FROM score ORDER BY numScore")
    List<Score> getAllOrdered();

    @Query("SELECT * FROM score WHERE bombNum=:n ORDER BY numScore")
    List<Score> getBombScore(int n);

    @Insert
    void insert(Score score);

    @Delete
    void delete(Score score);
}
