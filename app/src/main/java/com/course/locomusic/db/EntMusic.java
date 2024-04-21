package com.course.locomusic.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_music",indices = {@androidx.room.Index(value = "path",unique = true)})
public class EntMusic {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tid")
    private long tid;

    @ColumnInfo(name = "music_name")
    private String musicName;

    @ColumnInfo(name = "path")
    private String path;

    @ColumnInfo(name = "uri")
    private String uri;

    @ColumnInfo(name = "is_favourite")
    private int isFavourite;


    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public int getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(int isFavourite) {
        this.isFavourite = isFavourite;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
