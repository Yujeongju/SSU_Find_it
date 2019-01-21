package com.hashtoggle.find_it;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hashtoggle.find_it.R.color.shadow;

public class PostActivity extends AppCompatActivity {
    private EditText search_text, sub_search;
    private SimpleDateFormat simpleDateFormat;
    private Date now_date;
    private DBHelper dbHelper;

    private String search_key;
    private TextView hashTag_zone, like;
    private ImageView post;
    private HashTagHelper mTextHashTagHelper;

    private int frag=1;
    private Handler handler;
    private String hashtag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        search_text = (EditText) findViewById(R.id.keyword_zone);
        sub_search = (EditText)findViewById(R.id.sub_search);

        dbHelper = new DBHelper((Context) getApplicationContext(), "LASTWORD.db", null, 1);


        // 날짜는 현재 날짜로 고정
        // 현재 시간 구하기
        long now = System.currentTimeMillis();
        now_date = new Date(now);
        // 출력될 포맷 설정
        simpleDateFormat = new SimpleDateFormat("yyyy. MM. dd.");

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String like_Int = intent.getStringExtra("like");
        search_key = intent.getStringExtra("keyword");
        search_text.setText(search_key);
        final String hashTag_String = intent.getStringExtra("hashtag");
        post = (ImageView)findViewById(R.id.post);
        hashTag_zone = (TextView)findViewById(R.id.hash_tag);
        like = (TextView)findViewById(R.id.like_it);

        Glide.with(this).load(""+url).placeholder(R.drawable.loading).into(post);
        hashTag_zone.setText(""+hashTag_String);

        like.setText("좋아요"+like_Int);

        hashTag_act(frag);

        mTextHashTagHelper.handle(hashTag_zone);

    }

    private void hashTag_act(int frag){
        if(frag==1) {
            mTextHashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.colorPrimary), new HashTagHelper.OnHashTagClickListener() {
                @Override
                public void onHashTagClicked(String hashTag) {
                    show(hashTag);
                }
            });

        }
        else if(frag==0){
            mTextHashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.colorPrimary), new HashTagHelper.OnHashTagClickListener() {
                @Override
                public void onHashTagClicked(String hashTag) {
                    String sub_String = sub_search.getText().toString();
                    sub_search.setText(sub_String+" "+hashTag);
                }
            });
        }
    }

    public void onClick_search(View view){
        search_text = (EditText)findViewById(R.id.keyword_zone);

        String item = search_text.getText().toString();
        String date = simpleDateFormat.format(now_date);

        dbHelper.insert(date, item);
        ((MainActivity)MainActivity.CONTEXT).onResume();

        if(view != null){
            //키보드 숨기기
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }

        this.finish();
        Intent intent = new Intent(PostActivity.this, SearchActivity.class);
        intent.putExtra("keyword",search_text.getText().toString());
        startActivity(intent);
    }

    public void onClick_menu(View view) {
        Intent intent = new Intent(PostActivity.this, MainActivityDialog.class);
        startActivity(intent);
    }

    public void onClick_location_search(View view){
        Intent intent = new Intent(PostActivity.this, MyLocationActivity.class);
        intent.putExtra("location", ""+sub_search.getText());
        startActivity(intent);
    }

    public void onClick_Naver(View view){
        Intent intent2 = new Intent(PostActivity.this, WebviewActivity.class);
        intent2.putExtra("search_keyword", ""+sub_search.getText());
        startActivity(intent2);
    }

    public void onClick_get_tag(View view){
        Button get_tag = (Button)findViewById(R.id.get_tag);

        switch (frag){
            default:
            case 0:{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    get_tag.setBackground(ContextCompat.getDrawable(this, R.color.white));
                } else {
                    get_tag.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.white));
                }
                frag=1;
                hashTag_act(frag);
                mTextHashTagHelper.handle(hashTag_zone);
                break;
            }
            case 1: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    get_tag.setBackground(ContextCompat.getDrawable(this, R.color.black));
                } else {
                    get_tag.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.black));
                }
                frag = 0;
                hashTag_act(frag);
                mTextHashTagHelper.handle(hashTag_zone);
                break;
            }
        }
    }

    private void show(final String keyword)
    {

        final List<String> ListItems = new ArrayList<>();
        ListItems.add("이 태그만 검색하기");
        ListItems.add("필터에 추가하기");
        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이 태그에 대하여");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                String selectedText = items[pos].toString();
                switch (selectedText){

                    case "이 태그만 검색하기":
                        Intent intent = new Intent(PostActivity.this, SearchActivity.class);
                        intent.putExtra("keyword", ""+keyword);
                        startActivity(intent);
                        finish();
                        break;
                    case "필터에 추가하기":
                        Intent intent2 = new Intent(PostActivity.this, SearchActivity.class);
                        intent2.putExtra("keyword", search_key+" "+keyword);
                        startActivity(intent2);
                        finish();
                        break;
                }
            }
        });
        builder.show();
    }
}
