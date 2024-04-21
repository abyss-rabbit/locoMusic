package com.course.locomusic;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.course.locomusic.adapter.TestAdapter;
import com.course.locomusic.service.SMediaPlay;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    RecyclerView fileList;
    TestAdapter tadapter;
    LinearLayoutManager layoutManager;
    List<String> list = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, MusicListListActivity.class));
        finish();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Intent intent = new Intent(MainActivity.this, SMediaPlay.class);
        stopService(intent);
    }



}