package com.course.locomusic.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.course.locomusic.R;

public class Util {
    /*
    特殊歌单类型
    */

    public static final class SPECIAL{
        public final static int LIST_ALL =-1;
        public final static int LIST_FAVUORITE =-2;
        public final static int LIST_NOW_PLAY =-3;

        public final static int MUSIC_FAVOURITE=1;
        public final static int MUSIC_UNFAVOURITE=0;
    }

    public static final class PLAY_MODE{
        public static final int LOOP=0;
        public static final int RANDOM=1;
        public static final int SINGLE=2;
    }



    /**
     * 过长字符串截断
     *
     * @param str    字符串
     * @param length 目标长度
     * @return 处理后字符串
     */
    public static String truncate(String str, int length) {
        if (str == null) {
            return null;
        }
        if (str.length() > length) {
            return str.substring(0, length - 1) + "...";
        }
        return str;
    }

    /**
     * 时间转换
     *
     * @param time 时间,毫秒
     * @return 时间字符串
     */
    public static String timeCal(int time) {
        time=time/1000;
        int minute;
        int second;
        if (time >= 60) {
            minute = time / 60;
            second = time % 60;
            //分钟在0~9
            if (minute < 10) {
                //判断秒
                if (second < 10) {
                    return "0" + minute + ":" + "0" + second;
                } else {
                    return "0" + minute + ":" + second;
                }
            } else {
                //分钟大于10再判断秒
                if (second < 10) {
                    return minute + ":" + "0" + second;
                } else {
                    return minute + ":" + second;
                }
            }
        } else {
            second = time;
            if (second >= 0 && second < 10) {
                return "00:" + "0" + second;
            } else {
                return "00:" + second;
            }
        }

    }

    /**
     * 讲byte数组转换为bitmaps
     * @param bytes
     * @param opts
     * @return
     */
    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }

    public static int getModeImage(int mode){
        if(mode==Util.PLAY_MODE.LOOP){
            return R.mipmap.ic_night_bright;
        }
        else if(mode==Util.PLAY_MODE.RANDOM){
            return R.mipmap.ic_night_shuffleone;
        }
        else{
            return R.drawable.ic_night_replay;

        }
    }


}
