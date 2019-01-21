package com.hashtoggle.find_it;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NoteDialog  extends Activity {
    private ImageView view_note;
    private Thread mThread;
    private URL img_url;
    private Bitmap bit_note;

    protected  void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //다이얼로그처럼 보이게 세팅
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams= new WindowManager.LayoutParams();
        getWindow().setAttributes(layoutParams);
        setContentView(R.layout.activity_note);

        Intent intent = getIntent();
        String ID = intent.getStringExtra("note");

        final String url_string = intent.getStringExtra("url");

        view_note = (ImageView)findViewById(R.id.view_note);

        Glide.with(this).load(url_string).into(view_note);

        view_note.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // 버튼에서 손을 떼었을 때
                    finish();
                }
                return true;
            }
        });

    }
}
