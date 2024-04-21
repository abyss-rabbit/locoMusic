package com.course.locomusic.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.course.locomusic.db.EntMusic;
import com.course.locomusic.model.PCallBack;
import com.course.locomusic.model.Util;

import java.util.ArrayList;
import java.util.List;



public class SMediaPlay extends Service {



    MediaPlayer mediaPlayer;
    Context context;
    List<EntMusic> musicList=new ArrayList<>();
    int index=0;
    int mode= Util.PLAY_MODE.LOOP;

    PCallBack pCallBack;

    IntentFilter intentFilter;

    public class MBinder extends Binder {
        public  SMediaPlay getService() {
            return SMediaPlay.this;
        }
    }

    public IBinder onBind(Intent intent) {
        this.mediaPlayer = new MediaPlayer();


        //注册耳机事件监听
        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(broadcastReceiver,intentFilter, Context.RECEIVER_NOT_EXPORTED);
        return new MBinder();
    }


    //处理耳机控制事件
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
                pause();
            }
            if(action.equals(Intent.ACTION_MEDIA_BUTTON)){
                KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            }

        }
    };

    //打印报错信息
    private void logErr(int code,Exception e){
        Log.d("size",musicList.size()+" "+index);
        if(this.context!=null){
            Toast.makeText(this.context,String.format("%d:播放失败",code),Toast.LENGTH_SHORT).show();
        }
        Log.e("MediaPlay",e.toString());
    }

    //播放列表控制
    public void setMusicList(List<EntMusic> list, Context context){
        this.musicList.clear();
        this.musicList.addAll(list);
        this.index=0;
        this.context=context;
    }

    public List<EntMusic> getMusicList(){
        return this.musicList;
    }

    public void addMusic(EntMusic music,Context context){
        boolean flag=false;
        for(int i=0;i<this.musicList.size();i++){
            if(musicList.get(i).getMusicName().equals(music.getMusicName())){
                this.index=i;
                flag=true;
                break;
            }
        }
        if(!flag){
            this.musicList.add(index,music);
        }

        this.context=context;
        play();
    }

    public void delMusic(int i){

        if(i<index){
            this.musicList.remove(i);
            index--;
        }
        else if(i==index){
            //播放下一首并删除条目,如果count为0停止播放
            this.musicList.remove(i);
            if(this.musicList.size()==0){
                this.pause();
            }
            else{
                play();
            }
        }
        else{
            this.musicList.remove(i);
            if(this.musicList.size()==0){
                this.pause();
                this.mediaPlayer.reset();
                this.musicList.clear();
            }
        }
    }

    public int getCount(){
        return this.musicList.size();
    }

    public void setIndex(int i){
        this.index=i;
        if(i!=index){
            play();
        }
    }


    public void play(){
        try {
            this.mediaPlayer.reset();
            if(this.musicList.get(this.index).getUri()==null){
                logErr(-3,null);
                return;
            }
            this.mediaPlayer.setDataSource(this.musicList.get(this.index).getPath());
            this.mediaPlayer.prepare();
            this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    callBack();
                }
            });
            this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    systemNext();
                }
            });
        }
        catch (Exception e) {
            logErr(-2,e);
            systemNext();
        }
    }

    public void pause(){
        try {
            this.mediaPlayer.pause();
        }
        catch (Exception e){
            logErr(-3,e);
        }
    }

    public void start(){
        try {
            this.mediaPlayer.start();
        }
        catch (Exception e){
            logErr(-4,e);
        }
    }

    public int getNextMode(){
        return this.mode;
    }

    public void setNextMode(int code){
        if(code>=0&&code<3){
            this.mode=code;
        }
    }

    public void next(){
        if(this.musicList.isEmpty())return;

        int code=this.mode;
        int i=this.index;
        if(code==Util.PLAY_MODE.RANDOM){//随机
            i=randomChoice(i);
        }
        else{
            i=(i+1)%this.getCount();
        }
        this.index=i;
        play();

    }

    public void systemNext(){
        if(this.musicList.isEmpty())return;

        int code=this.mode;
        int i=this.index;
        if(code==Util.PLAY_MODE.LOOP){//循环
            i=(index+1)%this.getCount();
        }
        else if(code==Util.PLAY_MODE.RANDOM){//随机
            i=randomChoice(i);
        }
        this.index=i;
        play();
    }

    private int randomChoice(int i){
        int to=(int)(Math.random()*this.getCount());
        if(to==i){
            to=(to+1)%this.getCount();
        }
        return to;
    }

    public int  getMode(){
        return this.mode;
    }

    public boolean getPlayState(){
        return this.mediaPlayer.isPlaying();
    }

    public EntMusic getNowPlay(){
        return this.musicList.get(this.index);
    }



    public void last() {
        if(this.musicList.isEmpty())return;

        int code=this.mode;
        int i=this.index;
        if(code==0){//循环
            i=(this.getCount()+(index-1))%this.getCount();
        }
        else if(code==1){//随机
            i=(int)(Math.random()*this.getCount());
        }
        this.index=i;
        play();

    }

    public void jumpTo(int time){
        this.mediaPlayer.seekTo(time);
    }

    public List<Number> getProgress(){
        int currentPosition = this.mediaPlayer.getCurrentPosition();
        int duration = this.mediaPlayer.getDuration();
        List<Number> list=new ArrayList<>();
        list.add(currentPosition);
        list.add(duration);
        return list;
    }
//
    public byte[] getPicture(){
        if(this.musicList.size()==0){
            return null;
        }
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(this.musicList.get(this.index).getPath());
            return mediaMetadataRetriever.getEmbeddedPicture();
        }
        catch (Exception e){
            logErr(-3,e);
            return null;
        }
    }

    public String getMusicName(){
        if(this.musicList.isEmpty()){
            return "";
        }
        return this.musicList.get(this.index).getMusicName();
    }

    public void setInfoCallBack(PCallBack pc){
        this.pCallBack=pc;
        callBack();
    }

    public void callBack(){
        if(pCallBack!=null){
            pCallBack.send(null);
        }
    }

    @Override
    public void onDestroy(){
        if(this.mediaPlayer!=null){
            this.mediaPlayer.release();
            this.mediaPlayer=null;
        }
    }

    @Override
    public boolean onUnbind(Intent intent){
        if(this.mediaPlayer!=null){
            this.mediaPlayer.release();
            this.mediaPlayer=null;
        }
        return false;
    }


}
