package com.course.locomusic.db;

import androidx.room.DatabaseView;

@DatabaseView("SELECT a.mtid,a.ttid,b.music_name,b.path,b.uri,b.is_favourite FROM tb_music_list_to_list AS a JOIN tb_music AS b ON b.tid=a.mtid")
public class EntMusicToListView {

    public long mtid;
    public long ttid;
    public String music_name;
    public String path;
    public String uri;
    public int is_favourite;

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

    public String getMusic_name() {
        return music_name;
    }

    public void setMusic_name(String music_name) {
        this.music_name = music_name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getIs_favourite() {
        return is_favourite;
    }

    public void setIs_favourite(int is_favourite) {
        this.is_favourite = is_favourite;
    }
}
