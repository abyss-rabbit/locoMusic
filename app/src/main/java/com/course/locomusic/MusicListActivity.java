package com.course.locomusic;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.course.locomusic.adapter.MusicListAdapter;
import com.course.locomusic.db.EntMusic;
import com.course.locomusic.fragment.MPlay;
import com.course.locomusic.model.PCallBack;
import com.course.locomusic.model.Util;
import com.course.locomusic.model.console;
import com.course.locomusic.service.SDb;
import com.course.locomusic.service.SMediaPlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class MusicListActivity extends AppCompatActivity {
    private SDb.SDbMBinder binder; //获取服务
    private SMediaPlay.MBinder mediaBinder;


    private List<EntMusic> musicList=new ArrayList<>();
    private MusicListAdapter musicListAdapter;
    private RecyclerView recyclerView;

    private long tid= Util.SPECIAL.LIST_ALL; //-1:全部


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.musiclist);

        int commit = getSupportFragmentManager().beginTransaction().replace(R.id.MPlayTg, new MPlay()).commit();

        tid = getIntent().getLongExtra("tid",-1);


        createActionBar();
        createService();
        createScarchBar();
//

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    private void createService (){
        //服务状态控制
        ServiceConnection dbConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (SDb.SDbMBinder) service;
                createAdapter();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };


        Intent openIntent = new Intent(this.getApplicationContext(), SDb.class);
        bindService(openIntent, dbConn, BIND_AUTO_CREATE);

        ServiceConnection MediaConn=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mediaBinder = (SMediaPlay.MBinder) service;
                if(tid==Util.SPECIAL.LIST_NOW_PLAY){
                    //处理显示的内容
                    musicList.clear();
                    int size = mediaBinder.getService().getMusicList().size();
                    musicList.addAll(mediaBinder.getService().getMusicList());
                    runOnUiThread(()->musicListAdapter.notifyDataSetChanged());
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };


        Intent openMediaIntent = new Intent(this.getApplicationContext(), SMediaPlay.class);
        bindService(openMediaIntent, MediaConn, BIND_AUTO_CREATE);
    }


    private void createAdapter(){
        this.musicListAdapter = new MusicListAdapter(musicList,tid, this,o->{
            if(tid==Util.SPECIAL.LIST_NOW_PLAY){
                finish();
            }

            return null;
        });
        this.recyclerView=findViewById(R.id.musicList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.recyclerView.setLayoutManager(layoutManager);
        this.recyclerView.setAdapter(musicListAdapter);

        setData();
    }

    private void setData(){
        musicList.clear();

        EditText inp=findViewById(R.id.MusicSearchinp);
        if(!inp.getText().toString().equals("")){

            runOnUiThread(()->musicListAdapter.notifyDataSetChanged());
            return;
        }

        if(tid==Util.SPECIAL.LIST_NOW_PLAY){ //正在播放
            if(mediaBinder!=null&&musicListAdapter!=null){
                musicList.addAll(mediaBinder.getService().getMusicList());
                runOnUiThread(()->musicListAdapter.notifyDataSetChanged());
            }
            return;
        }


        binder.getService().selectAllMusic(tid,o->{
            if(o==null) return null;
            musicList.addAll((List<EntMusic>)o);
            runOnUiThread(()->musicListAdapter.notifyDataSetChanged());
            return null;
        });
    }


    protected void createActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar !=null ){
            String title = getIntent().getStringExtra("title");
            if(tid==Util.SPECIAL.LIST_NOW_PLAY){
                title="正在播放";
            }
            actionBar.setTitle(title);
//            supportActionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }




    protected void createScarchBar(){
        EditText inp=findViewById(R.id.MusicSearchinp);
        if(!inp.getText().toString().equals("")){

        }
        inp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<EntMusic> fMusicList=new ArrayList<>();

                if(inp.getText().toString().equals("")){
                    musicList.clear();
                    setData();
                }
                else{
                    for (EntMusic music:musicList){
                        if(music.getMusicName().contains(inp.getText().toString())){
                            fMusicList.add(music);
                        }
                    }
                    musicList.clear();
                    musicList.addAll(fMusicList);
                    musicListAdapter.notifyDataSetChanged();
                    fMusicList.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    /*
顶部菜单栏操作
 */
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        if(tid!=Util.SPECIAL.LIST_NOW_PLAY) { // 对于目前播放不能更能操作
            getMenuInflater().inflate(R.menu.musiclist, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        if (item.getItemId() == R.id.playAll) {
            if(musicList.size()==0){
                Toast.makeText(this, "没有歌曲可以播放", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
            mediaBinder.getService().setMusicList(musicList, this);
            mediaBinder.getService().play();

            //跳转播放页面
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("tid",tid);
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.reNameMusicList){
            if(tid==Util.SPECIAL.LIST_NOW_PLAY) {
                Toast.makeText(this, "不能重命名正在播放的歌单", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
            else if(tid==Util.SPECIAL.LIST_ALL) {
                Toast.makeText(this, "不能重命名该歌单", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
            else if(tid==Util.SPECIAL.LIST_FAVUORITE) {
                Toast.makeText(this, "不能重命名该歌单", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }

            showInputDialog("重命名", o->{
                if(Objects.equals((String) o, "")) {
                    Toast.makeText(this, "歌单名不能为空", Toast.LENGTH_SHORT).show();
                    return null;
                }
                
                binder.getService().renameMusicList(tid,(String)o,c->{
                    runOnUiThread(()->{
                        Objects.requireNonNull(getSupportActionBar()).setTitle((String)o);
                    });
                    return null;
                });
                return null;
            });
        }
        return super.onOptionsItemSelected(item);

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

}

