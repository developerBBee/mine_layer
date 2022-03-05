package jp.developer.bbee.minegame;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Score.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ScoreDao scoreDao();

    private static AppDatabase appDatabase;
    /**
     * Return ROOM database. If the database doesn't exist, create it.
     *
     * @param context
     * @return AppDatabase
     */
    public static AppDatabase getDatabase(final Context context) {
        if (appDatabase == null) {
            synchronized (AppDatabase.class) {
                if (appDatabase == null) {
                    appDatabase = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "score_database")
                            .build();
                }
            }
        }
        return appDatabase;
    }
}