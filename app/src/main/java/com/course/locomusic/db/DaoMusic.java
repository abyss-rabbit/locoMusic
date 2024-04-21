package com.course.locomusic.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoMusic {

    @Query("SELECT * FROM tb_music")
    public List<EntMusic> select();

    @Query("SELECT * FROM tb_music WHERE is_favourite=1")
    public List<EntMusic> selectFavourite();

    @Query("SELECT * FROM tb_music WHERE tid=:tid")
    public List<EntMusic> select(long tid);

    @Query("SELECT * FROM tb_music WHERE path=:path")
    public List<EntMusic> select(String path);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertMusic(EntMusic entMusic);

    @Query("UPDATE tb_music SET is_favourite=:isFavourite WHERE tid=:tid")
    public void updateFavourite(long tid, int isFavourite);

    @Query("DELETE FROM tb_music WHERE tid=:tid")
    public void delete(long tid);

    @Query("SELECT COUNT(*) FROM tb_music WHERE is_favourite=1")
    public int countFavourite();

    @Query("SELECT COUNT(*) FROM tb_music")
    public int count();
}
