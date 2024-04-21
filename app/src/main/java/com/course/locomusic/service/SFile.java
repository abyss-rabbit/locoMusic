package com.course.locomusic.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;
import androidx.room.Room;

import com.course.locomusic.db.AppDatabase;
import com.course.locomusic.db.DaoMusic;
import com.course.locomusic.model.FileUtil;
import com.course.locomusic.model.PCallBack;

public class SFile extends Service {

    private DaoMusic daoMusic;
    private ServiceConnection dbConn;
    private SDb.SDbMBinder binder;

    public class MBinder extends Binder { public  SFile getService() {
        return SFile.this;
    }}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //创建数据库
        AppDatabase appDatabase = Room.databaseBuilder(this, AppDatabase.class, "locomusic").build();
        daoMusic=appDatabase.daoMusic();

        //绑定SDb服务
        stateService();
    }

    private void stateService() {
        this.dbConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (SDb.SDbMBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent openIntent=new Intent(this.getApplicationContext(), SDb.class);
        bindService(openIntent, this.dbConn, BIND_AUTO_CREATE);
    }



    /**
     * 搜索路径下的音乐
     * @param filePaths 文件路径
     * @param callBack 回调函数
     */
    public void selectMusicOnPath(Uri filePaths,PCallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {

                DocumentFile[] files = DocumentFile.fromTreeUri(getApplicationContext(),filePaths).listFiles();
                for(DocumentFile file:files){
                    if(file.getType()==null)continue;
                    if(file.getType().startsWith("audio")){
                        String realPathFromUri = FileUtil.getRealPathFromUri(getApplicationContext(), file.getUri());
                        Uri uri=FileUtil.queryUriforAudio(getApplicationContext(),realPathFromUri);
                        binder.getService().insertMusic(uri,realPathFromUri,(o)->{
                            return null;});
                    }
                }
                binder.getService().selectAllMusicCount(callBack);
            }
        }).start();
    }

}

