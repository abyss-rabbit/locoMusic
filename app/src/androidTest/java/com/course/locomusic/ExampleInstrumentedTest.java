package com.course.locomusic;

import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        UriMatcher myUri= new UriMatcher(UriMatcher.NO_MATCH);
        myUri.addURI("iet.jxufe.cn.providers.myprovider", "person", 1);
        myUri.addURI("iet.jxufe.cn.providers.myprovider", "person/#", 2);
        int result=myUri.match(Uri.parse("content://iet.jxufe.cn.providers.myprovider/person/10"));
        Log.d("res",result+"");


    }
}