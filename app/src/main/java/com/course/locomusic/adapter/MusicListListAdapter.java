package com.course.locomusic.adapter;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.course.locomusic.MainActivity;
import com.course.locomusic.MusicListActivity;
import com.course.locomusic.R;
import com.course.locomusic.db.DaoMusic;
import com.course.locomusic.db.EntMusicList;
import com.course.locomusic.service.SDb;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicListListAdapter extends RecyclerView.Adapter<MusicListListAdapter.PViewHolder> {
    private List<EntMusicList> list;
    private boolean lock=false;
    private DaoMusic daoMusic;

    private ServiceConnection DbConnect;
    private SDb.SDbMBinder binder;



    public MusicListListAdapter(List<EntMusicList> list,Context context) {
        //初始化list
        this.list=list;

        //启动服务
        stateSDb(context);

    }

    private void stateSDb(Context context){
        this.DbConnect=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder=(SDb.SDbMBinder)service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        //启动
        Intent openIntent=new Intent(context.getApplicationContext(), SDb.class);
        context.bindService(openIntent, DbConnect, BIND_AUTO_CREATE);
    }

    @NonNull
    @Override
    public MusicListListAdapter.PViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  inflate= LayoutInflater.from(parent.getContext()).inflate(R.layout.musiclist_list_item, parent, false);
        return new PViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull PViewHolder holder, int position) {
        EntMusicList musicListItem=list.get(position);
        holder.itemView.findViewById(R.id.musicListListDel).setOnClickListener(v -> {
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

        holder.itemView.findViewById(R.id.musicListListDel).setOnLongClickListener(v -> {
            EntMusicList remove =list.remove(position);
            binder.getService().delMusicList(remove.getTid(),o->null);

            notifyItemRemoved(position);
            return true;
        });

        holder.itemView.findViewById(R.id.item).setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MusicListActivity.class);
            intent.putExtra("tid", musicListItem.getTid());
            intent.putExtra("title", musicListItem.getName());
            v.getContext().startActivity(intent);
        });


        holder.name.setText(musicListItem.getName());

        String time=musicListItem.getCreateDate();
        int len=musicListItem.getLen();
        holder.info.setText(String.format("%s创建,共%d首歌曲", time, len));
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public class PViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView info;

        public PViewHolder(@NonNull View itemView) {

            super(itemView);
            this.name=itemView.findViewById(R.id.musicListName);
            this.info=itemView.findViewById(R.id.musicListInfo);
        }
    }
}

