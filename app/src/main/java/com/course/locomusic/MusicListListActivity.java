package com.course.locomusic;

import android.Manifest;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.course.locomusic.adapter.MusicListListAdapter;
import com.course.locomusic.db.EntMusicList;
import com.course.locomusic.fragment.MPlay;
import com.course.locomusic.model.PCallBack;
import com.course.locomusic.service.SDb;
import com.course.locomusic.service.SFile;
import com.course.locomusic.service.SMediaPlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MusicListListActivity extends AppCompatActivity {
    private SDb.SDbMBinder binder;
    private SFile.MBinder binderFile;
    private SMediaPlay.MBinder binderMedia;
    private ServiceConnection dbConn;
    private ServiceConnection fileConn;
    private ServiceConnection mediaConn;
    private ActivityResultLauncher<Uri> activityResultLauncher;

    private MusicListListAdapter musicListListAdapter;
    private RecyclerView recyclerView;
    private List<EntMusicList> musicLists=new ArrayList<>();


    private int count=0;
    private int favouriteCount=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.musiclist_list);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


        int commit = getSupportFragmentManager().beginTransaction().replace(R.id.MPlayTg, new MPlay()).commit();

        stateService();
        createFileAdd();
        createListen();
        createActivityJump();
        goManagerFileAccess(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(binder!=null){
            setData();
            createAdapter();
        }
    }

    private void goManagerFileAccess(AppCompatActivity activity) {
        // Android 11 (Api 30)或更高版本的写文件权限需要特殊申请，需要动态申请管理所有文件的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()) {
                Toast.makeText(activity, "需要访问全部文件权限", Toast.LENGTH_LONG).show();
                Intent appIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                appIntent.setData(Uri.parse("package:" + getPackageName()));
                //appIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                try {
                    activity.startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    ex.printStackTrace();
                    Intent allFileIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    activity.startActivity(allFileIntent);
                }
            }
        }
    }



    private void createActivityJump() {
        View allMusicList = findViewById(R.id.allMusicList);
        View favouriteMusicList = findViewById(R.id.favouriteMusicList);
        //针对特殊情况的列表跳转
        //-1:全部,-2:收藏,-3:正在播放
        allMusicList.setOnClickListener(v->{
            Intent intent = new Intent(MusicListListActivity.this,MusicListActivity.class);
            intent.putExtra("tid",(long)-1);
            intent.putExtra("title","全部");
            startActivity(intent);
        });
        favouriteMusicList.setOnClickListener(v->{
            Intent intent = new Intent(MusicListListActivity.this,MusicListActivity.class);
            intent.putExtra("tid",(long)-2);
            intent.putExtra("title","收藏");
            startActivity(intent);
        });
    }

    private void stateService() {
        this.dbConn =new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder=(SDb.SDbMBinder)service;
                setData();
                createAdapter();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        this.fileConn=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binderFile=(SFile.MBinder)service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };


        //服务链接保活?
        //这里要是不创建链接在跳转页面时会重复创建
        this.mediaConn=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binderMedia=(SMediaPlay.MBinder)service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        //启动
        Intent openIntent=new Intent(this.getApplicationContext(), SDb.class);
        bindService(openIntent, dbConn, BIND_AUTO_CREATE);
        Intent openIntentFile=new Intent(this.getApplicationContext(), SFile.class);
        bindService(openIntentFile, fileConn, BIND_AUTO_CREATE);
        Intent openIntentMedia=new Intent(this.getApplicationContext(), SMediaPlay.class);
        bindService(openIntentMedia, mediaConn, BIND_AUTO_CREATE);
    }


    private void setData() {
        binder.getService().selectAllMusicCount(o->{
            if(o==null) return null;
            runOnUiThread(()->{
                count=(int)o;
                TextView allMusicCount = findViewById(R.id.allMusicCount);
                allMusicCount.setText(count+" 首");
            });
            return null;
        });
        binder.getService().selectFavouriteMusicCount(o->{
            if(o==null) return null;
            runOnUiThread(()->{
                favouriteCount=(int)o;
                TextView favouriteMusicCount = findViewById(R.id.favouriteCount);
                favouriteMusicCount.setText(favouriteCount+" 首");
            });
            return null;
        });
    }


    private void createAdapter() {
        this.musicListListAdapter=new MusicListListAdapter(musicLists,this);

        this.recyclerView = (RecyclerView)findViewById(R.id.musicListList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.recyclerView.setLayoutManager(layoutManager);
        this.recyclerView.setAdapter(musicListListAdapter);

        binder.getService().selectAllMusicList(o->{
            musicLists.clear();
            musicLists.addAll((List<EntMusicList>)o);
            runOnUiThread(()->musicListListAdapter.notifyDataSetChanged());
            return null;
        });
    }

    /*
    添加歌单
     */
    private void createListen() {
        View musicListAdd = findViewById(R.id.musicListListAdd);
        musicListAdd.setOnClickListener(v->{
            showInputDialog("输入歌单名称", o->{
                if(Objects.equals((String) o, "")) {
                    Toast.makeText(this, "歌单名不能为空", Toast.LENGTH_SHORT).show();
                    return null;
                }

                binder.getService().insertMusicList((String)o,c->{
                    musicLists.add((EntMusicList) c);
                    runOnUiThread(()->musicListListAdapter.notifyItemInserted(musicLists.size()-1));
                    return  null;
                });
                return null;
            });
        });


    }

    private void showInputDialog(String title, PCallBack callBack) {
        /*@setView 装入一个EditView
         */
        EditText editText = new EditText(this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(this);
        inputDialog.setTitle(title).setView(editText);
        inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callBack.send(editText.getText().toString());
            }
        }).show();
    }


    /*
    扫描新音乐
     */
    private void createFileAdd() {
        this.activityResultLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if(result==null) return;
                    binderFile.getService().selectMusicOnPath(result, o -> {
                        if(o==null) return null;
                        count=(int)o;
                        runOnUiThread(()->{
                            TextView allMusicCount = findViewById(R.id.allMusicCount);
                            allMusicCount.setText(count+" 首");
                        });
                        return null;
                    });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.musiclistlist, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        //添加目录
        if (item.getItemId() == R.id.FileAdd) {
            activityResultLauncher.launch(null);
        }

        return super.onOptionsItemSelected(item);
    }



}
