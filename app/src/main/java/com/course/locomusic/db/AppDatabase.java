package com.course.locomusic.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {EntMusic.class,EntMusicToList.class,EntMusicList.class},
        views = {EntMusicToListView.class},
        version = 1,
        exportSchema=false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "locoMusicDb";
    private static AppDatabase database;
    public static synchronized AppDatabase getDatabaseInstance(Context context) {
        if(database == null) {
            database= Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME).build();
        }
        return database;
    }


    public abstract DaoMusic daoMusic();
    public abstract DaoMusicList daoMusicList();
    public abstract DaoMusicToList daoMusicToList();
    public abstract DaoMusicToListView daoMusicToListView();

}
