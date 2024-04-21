package com.course.locomusic.fragment;

import static android.content.Context.BIND_AUTO_CREATE;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.course.locomusic.PlayerActivity;
import com.course.locomusic.R;
import com.course.locomusic.model.Util;
import com.course.locomusic.service.SMediaPlay;

import java.util.Timer;
import java.util.TimerTask;

public class MPlay extends Fragment {

    private PlayerActivity playerActivity;
    private SMediaPlay.MBinder mediaBinder;

    private FragmentActivity activity;
    private View fragmentView;

    private Timer timer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = getActivity();
        fragmentView = inflater.inflate(R.layout.mplayer,container,false);
        if(activity != null){
            createSMediaPlayLink();
            createListen();
            createTimeThread();
        }



        return fragmentView;


    }


    private void createTimeThread() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                tSetTitle();
                setControl();
            }
        };
        timer.schedule(task, 0, 1000);
    }

    private void createListen() {
        ImageButton imgBut = fragmentView.findViewById(R.id.mplayControl);
        imgBut.setOnClickListener(v -> {
            if (mediaBinder.getService().getPlayState()) {
                mediaBinder.getService().pause();
            } else {
                mediaBinder.getService().start();
            }
        });

        ImageButton last=fragmentView.findViewById(R.id.mplayLast);
        last.setOnClickListener(v -> mediaBinder.getService().last());
        ImageButton next=fragmentView.findViewById(R.id.mplayNext);
        next.setOnClickListener(v -> mediaBinder.getService().next());
        fragmentView.findViewById(R.id.mplayAll).setOnClickListener(v->{
            Log.d("suc","suc");
            Intent intent = new Intent(activity.getApplicationContext(), PlayerActivity.class);
            startActivity(intent);
        });
    }

    private void createSMediaPlayLink() {
        ServiceConnection MusicConnect = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mediaBinder = (SMediaPlay.MBinder) service;

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent intent = new Intent(activity.getApplicationContext(), SMediaPlay.class);
        activity.bindService(intent, MusicConnect, BIND_AUTO_CREATE);
    }


    private void tSetTitle() {
        if(activity == null)return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaBinder == null)return;
                TextView textView = fragmentView.findViewById(R.id.mPlayTitle);
                textView.setText(mediaBinder.getService().getMusicName());
            }
        });
    }

    private void setControl(){
        if(activity == null)return;
        if(mediaBinder == null)return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaBinder == null)return;
                ImageButton imgBut = fragmentView.findViewById(R.id.mplayControl);
                if(mediaBinder.getService().getPlayState()){
                    imgBut.setImageResource(R.mipmap.ic_night_pause);
                }else{
                    imgBut.setImageResource(R.mipmap.ic_night_play);
                }
            }
        });
    }
}
