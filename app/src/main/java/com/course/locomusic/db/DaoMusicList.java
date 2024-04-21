package com.course.locomusic.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import java.util.List;

@Dao
public interface DaoMusicList {
    @Query("SELECT * FROM tb_music_list")
    public List<EntMusicList> selectAll();

    @Query("SELECT * FROM tb_music_list WHERE tid=:tid")
    public List<EntMusicList> selectByTid(long tid);

    @Insert
    public long insertMusicList(EntMusicList EntMusic);


    @Query("DELETE FROM tb_music_list WHERE tid=:tid")
    public void deleteByTid(long tid);

    @Query("UPDATE tb_music_list SET name=:name WHERE tid=:tid")
    public void updateName(String name,long tid);


    @Query("UPDATE tb_music_list SET len=:len WHERE tid=:tid")
    public void updateLen(int len,long tid);


}
