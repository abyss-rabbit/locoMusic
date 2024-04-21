package com.course.locomusic.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_music_list_to_list")
public class EntMusicToList {
    @PrimaryKey
    @ColumnInfo(name="mtid")
    private long mtid;

    @ColumnInfo(name = "ttid")
    private long ttid;


    public long getMtid() {
        return mtid;
    }

    public void setMtid(long mtid) {
        this.mtid = mtid;
    }

    public long getTtid() {
        return ttid;
    }

    public void setTtid(long ttid) {
        this.ttid = ttid;
    }

}
