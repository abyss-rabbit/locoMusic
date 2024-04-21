package com.course.locomusic;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.course.locomusic.model.RetInfoPojo;
import com.course.locomusic.model.Util;
import com.course.locomusic.service.SMediaPlay;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity {
    private Timer timer;
    private SMediaPlay.MBinder mediaBinder;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        createActionBar();
        createSMediaPlayLink(this);
        createListen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaBinder!=null){
            tSetTitleAndImage();
        }
    }


    private void createSMediaPlayLink(PlayerActivity playerActivity) {
        ServiceConnection MusicConnect = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mediaBinder = (SMediaPlay.MBinder) service;
                //绑定callBack函数
                mediaBinder.getService().setInfoCallBack(o->{
                    tSetMode();
                    tSetTitleAndImage();//标题图片刷新
                    return null;
                });

                createThread();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent intent = new Intent(this.getApplicationContext(), SMediaPlay.class);
        playerActivity.bindService(intent, MusicConnect, BIND_AUTO_CREATE);
    }
    protected void createActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar !=null){
            actionBar.hide();
        }
    }

    private void createListen() {
        //播放按钮
        ImageButton play = findViewById(R.id.playControl);
        play.setOnClickListener(v -> {
            if (mediaBinder.getService().getPlayState()) {
                mediaBinder.getService().pause();
            } else {
                mediaBinder.getService().start();
            }
        });

        //下一个
        ImageButton next = findViewById(R.id.playNext);
        next.setOnClickListener(v -> mediaBinder.getService().next());

        //上一个
        ImageButton pre = findViewById(R.id.playLast);
        pre.setOnClickListener(v -> mediaBinder.getService().last());

        //进度条控制
        SeekBar seekBar = findViewById(R.id.playSeekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaBinder.getService().jumpTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //循环模式
        ImageButton loop = findViewById(R.id.playMode);
        loop.setOnClickListener(v -> {
            int mode = mediaBinder.getService().getMode();
            mode=(mode+1)%3;
            mediaBinder.getService().setNextMode(mode);
            loop.setImageResource(Util.getModeImage(mode));
        });

        //播放列表
        ImageButton list = findViewById(R.id.playListNow);
        list.setOnClickListener(v -> {
            Intent intent = new Intent(PlayerActivity.this, MusicListActivity.class);
            intent.putExtra("tid",(long)Util.SPECIAL.LIST_NOW_PLAY);
            startActivity(intent);
        });

    }

    private void createThread() {
        //监听并变更UI
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tSetSeek();//进度刷新
                        tSetState();//播放控制刷新
                    }
                });
            }
        }, 0, 500);
    }

    private void tSetSeek() {
        runOnUiThread(() -> {
            List<Number> progress = mediaBinder.getService().getProgress();
            int currentPosition = progress.get(0).intValue();
            int duration = progress.get(1).intValue();
            SeekBar seekBar = findViewById(R.id.playSeekBar);
            seekBar.setMax(duration);
            seekBar.setProgress(currentPosition);

            TextView textView = findViewById(R.id.playTime);
            textView.setText(Util.timeCal(currentPosition)+ " / " + Util.timeCal(duration));
        });
    }

    private void tSetState() {
        runOnUiThread(() -> {
            ImageButton imgBut = findViewById(R.id.playControl);
            if (mediaBinder.getService().getPlayState()) {
                imgBut.setImageResource(R.mipmap.ic_night_pause);
            } else {
                imgBut.setImageResource(R.mipmap.ic_night_play);
            }
        });
    }

    private void tSetMode() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageButton imgBut = findViewById(R.id.playMode);
                imgBut.setImageResource(Util.getModeImage(mediaBinder.getService().getMode()));
            }
        });
    }


    private void tSetTitleAndImage() {
        runOnUiThread(() -> {
            TextView textView = findViewById(R.id.playTitle);
            Log.d("Name",mediaBinder.getService().getMusicName());
            textView.setText(mediaBinder.getService().getMusicName());
            ImageView imageView= findViewById(R.id.playMusicImg);
            byte[] picture = mediaBinder.getService().getPicture();
            if (picture != null) {
                imageView.setImageBitmap(Util.getPicFromBytes(picture,null));
            }
            else{
                imageView.setImageResource(R.drawable.test_1);
            }
        });
    }

}
