package jp.developer.bbee.minegame;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Score {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "bombNum")
    public int bombNum;

    @ColumnInfo(name = "strScore")
    public String strScore;

    @ColumnInfo(name = "numScore")
    public int numScore;

    public Score(int bombNum, @NonNull String strScore) {
        this.bombNum = bombNum;
        this.strScore = strScore;
        try {
            this.numScore = Integer.parseInt(strScore.replace(":", "")
                    .replace(".", ""));
        } catch (NumberFormatException e) {
            this.numScore = -1;
        }
    }

    @Override
    public String toString() {
        return "Score{" +
                "id=" + id +
                ", bombNum=" + bombNum +
                ", strScore='" + strScore + '\'' +
                ", numScore=" + numScore +
                '}';
    }
}
