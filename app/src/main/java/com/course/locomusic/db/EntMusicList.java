package com.course.locomusic.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_music_list")
public class EntMusicList {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tid")
    private long tid;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "create_date")
    private String createDate;

    @ColumnInfo(name="len")
    private int len;

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
