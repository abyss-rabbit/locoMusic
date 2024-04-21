package com.course.locomusic.model;

import android.util.Log;

public class console {
    public static void log(Object o){
        Log.d("log",o.toString());
    }

    public static void log(Object o,String tag){
        Log.d(tag,o.toString());
    }
}
