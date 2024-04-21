package com.course.locomusic.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoMusicToList {
    @Query("SELECT * FROM tb_music_list_to_list WHERE ttid=:ttid")
    public List<EntMusicToList> select(long ttid);

    @Query("SELECT COUNT(*) FROM tb_music_list_to_list WHERE ttid=:ttid")
    public int count(long ttid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(EntMusicToList entMusicToList);

    @Query("DELETE FROM tb_music_list_to_list WHERE mtid=:mtid")
    public void delMusic(long mtid);

    @Query("DELETE FROM tb_music_list_to_list WHERE ttid=:ttid")
    public void delMusicList(long ttid);

    @Query("DELETE FROM tb_music_list_to_list WHERE mtid=:mtid AND ttid=:ttid")
    public void delMusicInMusicList(long mtid,long ttid);
}
