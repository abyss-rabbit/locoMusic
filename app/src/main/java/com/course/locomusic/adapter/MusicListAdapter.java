package com.course.locomusic.adapter;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.course.locomusic.PlayerActivity;
import com.course.locomusic.R;
import com.course.locomusic.db.EntMusic;
import com.course.locomusic.db.EntMusicList;
import com.course.locomusic.model.PCallBack;
import com.course.locomusic.model.Util;
import com.course.locomusic.model.console;
import com.course.locomusic.service.SDb;
import com.course.locomusic.service.SMediaPlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.PViewHolder> {
    private List<EntMusic> list;
    private SDb.SDbMBinder binder;
    private SMediaPlay.MBinder mediaBinder;
    private long tid;
    private Context context;

    private PCallBack pCallBack;


    private boolean lock=false;

    ArrayList<Integer> yourChoices = new ArrayList<>();


    public MusicListAdapter(List<EntMusic> list,long tid, Context context,PCallBack pCallBack) {
        this.list=list;
        this.tid=tid;
        this.context=context;
        this.pCallBack=pCallBack;
        stateSDb(context);
        stateSMediaPlay(context);
    }

    private void stateSDb(Context context){
        ServiceConnection dbConnect = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (SDb.SDbMBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };


        //启动
        Intent openIntent=new Intent(context.getApplicationContext(), SDb.class);
        context.bindService(openIntent, dbConnect, BIND_AUTO_CREATE);
    }

    private void stateSMediaPlay(Context context){
       ServiceConnection MusicConnect = new ServiceConnection() {
           @Override
           public void onServiceConnected(ComponentName name, IBinder service) {
               mediaBinder = (SMediaPlay.MBinder) service;
           }

           @Override
           public void onServiceDisconnected(ComponentName name) {

           }
       };

        //启动
        Intent openIntent=new Intent(context.getApplicationContext(), SMediaPlay.class);

        context.bindService(openIntent,MusicConnect, BIND_AUTO_CREATE);
    }


    @NonNull
    @Override
    public PViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.musiclist_item, parent, false);
        return new PViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull PViewHolder holder, int position) {
        EntMusic music = list.get(position);
        holder.name.setText(music.getMusicName());
        if(music.getIsFavourite()==Util.SPECIAL.MUSIC_FAVOURITE){
            holder.favouriteButton.setImageResource(R.mipmap.ic_night_like_choice);
        }
        else{
            holder.favouriteButton.setImageResource(R.mipmap.ic_night_like);

        }
        //监听绑定
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaBinder!=null){
                    SMediaPlay service = mediaBinder.getService();
                    service.addMusic(music,view.getContext());
                    if(tid==Util.SPECIAL.LIST_NOW_PLAY){
                        pCallBack.send(null);

                    }
                    else{
                        //跳转到播放页
                        Intent intent = new Intent();
                        intent.setClass(view.getContext(), PlayerActivity.class);
                        intent.putExtra("tid",tid);
                        view.getContext().startActivity(intent);
                    }


                }
            }
        });

        holder.itemView.findViewById(R.id.delFromList).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(tid== Util.SPECIAL.LIST_ALL){//从所有歌单删除
                    binder.getService().delMusic(music.getTid(),o->null);
                }
                if(tid== Util.SPECIAL.LIST_FAVUORITE) {//从收藏歌单删除
                    holder.favouriteButton.setImageResource(R.mipmap.ic_night_like);
                    music.setIsFavourite(0);
                    binder.getService().setMusicFavourite(music.getTid(),Util.SPECIAL.MUSIC_UNFAVOURITE,o->null);
                }
                if(tid==Util.SPECIAL.LIST_NOW_PLAY){//从当前播放
                    mediaBinder.getService().delMusic(holder.getAdapterPosition());
                    list.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    return true;
                }


                binder.getService().delMusicFromList(tid,music.getTid(),o->null);
                list.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                return true;
            };

        });

        holder.itemView.findViewById(R.id.delFromList).setOnClickListener(v -> {
            //防抖
            if(!lock){
                lock=true;
                Toast.makeText(v.getContext(), "长按删除", Toast.LENGTH_SHORT).show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        lock=false;
                    }
                }, 3000);
            }
        });

        holder.itemView.findViewById(R.id.addToList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binder.getService().selectAllMusicList(o->{
                    if(o==null)return null;
                    showMultiChoiceDialog((List<EntMusicList>)o,c->{
                            binder.getService().insertMusicToList(((List<EntMusicList>)o).get((int)c).getTid(),music.getTid(),d->{
                            binder.getService().updateLen(((List<EntMusicList>)o).get((int)c).getTid());
                            return null;
                        });
                        return null;
                    });

                    return null;
                });
            }
        });

        holder.itemView.findViewById(R.id.favourite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(music.getIsFavourite()==0){
                    holder.favouriteButton.setImageResource(R.mipmap.ic_night_like_choice);
                    music.setIsFavourite(1);
                    binder.getService().setMusicFavourite(music.getTid(),Util.SPECIAL.MUSIC_FAVOURITE,o->null);
                }
                else{
                    holder.favouriteButton.setImageResource(R.mipmap.ic_night_like);
                    music.setIsFavourite(0);
                    binder.getService().setMusicFavourite(music.getTid(),Util.SPECIAL.MUSIC_UNFAVOURITE,o->null);
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public ImageButton favouriteButton;

        public PViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name=itemView.findViewById(R.id.MusicName);
            this.favouriteButton=itemView.findViewById(R.id.favourite);
        }
    }

    //单选框,加入歌单列表
    int yourChoice;

    private void showMultiChoiceDialog(List<EntMusicList> entMusicLists, PCallBack callBack) {
            //使用主线程的loop
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    final String[] items = new String[entMusicLists.size()];
                    for (int i = 0; i < entMusicLists.size(); i++) {
                        items[i] = entMusicLists.get(i).getName();
                    }
                    yourChoice = -1;
                    AlertDialog.Builder singleChoiceDialog =
                            new AlertDialog.Builder(context);
                    singleChoiceDialog.setTitle("选择需要加入的歌单");
                    // 第二个参数是默认选项，此处设置为0
                    singleChoiceDialog.setSingleChoiceItems(items, 0,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    yourChoice = which;
                                }
                            });
                    singleChoiceDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(yourChoice!=-1){//有选择
                                        callBack.send(yourChoice);
                                    }
                                    if(entMusicLists.size()!=0&&yourChoice==-1){//未选择按确定,默认值
                                        yourChoice=0;
                                        callBack.send(yourChoice);
                                    }
                                }
                            });
                    singleChoiceDialog.show();
                }
            });
    }

}
