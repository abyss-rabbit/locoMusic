package com.course.locomusic.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.room.Room;

import com.course.locomusic.db.AppDatabase;
import com.course.locomusic.db.DaoMusic;
import com.course.locomusic.db.DaoMusicList;
import com.course.locomusic.db.DaoMusicToList;
import com.course.locomusic.db.DaoMusicToListView;
import com.course.locomusic.db.EntMusic;
import com.course.locomusic.db.EntMusicList;
import com.course.locomusic.db.EntMusicToList;
import com.course.locomusic.model.PCallBack;
import com.course.locomusic.model.Util;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 数据库操作
 */
public class SDb extends Service {
    public class SDbMBinder extends Binder {
        public SDb getService() {
            return SDb.this;
        }
    }

    private DaoMusicList daoMusicList;
    private DaoMusicToList daoMusicToList;
    private DaoMusic daoMusic;
    private DaoMusicToListView daoMusicToListView;


    @Override
    public IBinder onBind(Intent intent) {
        return new SDbMBinder();
    }

    @Override
    public void onCreate(){
        super.onCreate();

        //创建数据库
        AppDatabase appDatabase = Room.databaseBuilder(this, AppDatabase.class, "locomusic").build();
        daoMusicList = appDatabase.daoMusicList();
        daoMusicToList = appDatabase.daoMusicToList();
        daoMusic = appDatabase.daoMusic();
        daoMusicToListView = appDatabase.daoMusicToListView();
    }


    /**
     * 插入新的MusicList
     * @param name 歌单名字
     * @param callBack 回调
     */
    public void insertMusicList(String name,  PCallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                EntMusicList entMusicList = new EntMusicList();
                entMusicList.setName(name);
                entMusicList.setLen(0);

                SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日");

                entMusicList.setCreateDate(sdf.format(new Date(System.currentTimeMillis())));
                long l = daoMusicList.insertMusicList(entMusicList);
                entMusicList.setTid(l);
                callBack.send(entMusicList);
            }}).start();
    }

    /**
     * 删除音乐列表
     * @param tid 列表ID
     * @param callBack 回调函数
     */
    public void delMusicList(long tid,PCallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoMusicList.deleteByTid(tid);
                daoMusicToList.delMusicList(tid);
                callBack.send(tid);
            }}).start();
    }

    public void renameMusicList(long tid, String name,PCallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoMusicList.updateName(name,tid);
                callBack.send(tid);
            }}).start();
    }

    /**
     * 查询全部播放列表(不包括喜欢的)
     * @param callBack 回调函数
     */
    public void selectAllMusicList(PCallBack callBack){

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<EntMusicList> list = daoMusicList.selectAll();
                callBack.send(list);
            }}).start();
    }

    public void insertMusic(Uri Uri, String path, PCallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                EntMusic entMusic = new EntMusic();
                String title = path.split("/")[path.split("/").length - 1];
                title = title.substring(0, title.lastIndexOf("."));
                entMusic.setMusicName(title);
                entMusic.setPath(path);
                entMusic.setIsFavourite(0);
                if(Uri == null){
                    entMusic.setUri("null");
                }
                else{
                    entMusic.setUri(Uri.toString());
                }
                long l = daoMusic.insertMusic(entMusic);

                entMusic.setTid(l);

                callBack.send(entMusic);
            }
        }).start();
    }

    public void delMusic(long mtid,PCallBack callBack) {
        //删除音乐,要同时删除To表的关联,更新List表的数量
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoMusic.delete(mtid);
                daoMusicToList.delMusic(mtid);
                //不想改了(
                List<EntMusicList> entMusicLists = daoMusicList.selectAll();
                for(EntMusicList entMusicList : entMusicLists){
                    int count = daoMusicToList.count(entMusicList.getTid());
                    daoMusicList.updateLen(count,entMusicList.getTid());
                }
                callBack.send(mtid);
            }
        }).start();
    }

    public void insertMusicToList(long tid, long mid,PCallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                EntMusicToList entMusicToList = new EntMusicToList();
                entMusicToList.setMtid(mid);
                entMusicToList.setTtid(tid);
                daoMusicToList.insert(entMusicToList);
                callBack.send(tid);
            }}).start();
    }

    public void setMusicFavourite(long mid,int favourite,PCallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoMusic.updateFavourite(mid,favourite);
                callBack.send(mid);
            }}).start();
    }

    public void delMusicFromList(long tid, long mid,PCallBack callBack){

        new Thread(new Runnable() {
            @Override
            public void run() {
                daoMusicToList.delMusicInMusicList(mid,tid);
                int count = daoMusicToList.count(tid);
                daoMusicList.updateLen(count,tid);
                callBack.send(tid);
            }}).start();
    }

    public void selectAllMusic(long tid,PCallBack callBack){
        Log.d("tid",tid+"");
        if(tid==Util.SPECIAL.LIST_ALL){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<EntMusic> list = daoMusic.select();
                    callBack.send(list);
                }}).start();
        }
        else if(tid==Util.SPECIAL.LIST_FAVUORITE){
            selectFavouriteMusic(callBack);
        }
        else{
            selectAllMusicByTTid(tid,callBack);
        }
    }

    public void selectAllMusicCount(PCallBack callBack){

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = daoMusic.count();
                callBack.send(count);
            }}).start();
    }

    public void selectFavouriteMusicCount(PCallBack callBack){

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = daoMusic.countFavourite();
                callBack.send(count);
            }}).start();
    }

    public void selectFavouriteMusic(PCallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<EntMusic> list = daoMusic.selectFavourite();
                callBack.send(list);
            }}).start();
    }

    public void selectAllMusicByTTid(long ttid,PCallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<EntMusic> select = daoMusicToListView.select(ttid);
                callBack.send(select);
            }}).start();
    }

    public void updateLen(long tid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<EntMusicList> entMusicLists = daoMusicList.selectByTid(tid);
                for(EntMusicList entMusicList : entMusicLists){
                    int count = daoMusicToList.count(entMusicList.getTid());
                    daoMusicList.updateLen(count,entMusicList.getTid());
                }
            }
        }).start();

    }
}