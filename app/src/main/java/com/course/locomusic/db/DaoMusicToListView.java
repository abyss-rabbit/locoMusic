package com.course.locomusic.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoMusicToListView {
    @Query("SELECT a.mtid AS tid,a.music_name AS music_name,a.uri AS uri,a.is_favourite AS is_favourite,a.path AS path FROM EntMusicToListView AS a WHERE ttid=:ttid")
    public List<EntMusic> select(long ttid);
}
