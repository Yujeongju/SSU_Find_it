package com.hashtoggle.find_it;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {
    private EditText search_text;
    private SimpleDateFormat simpleDateFormat;
    private Date now_date;
    private DBHelper dbHelper;
    private String aaa_keyword;
    private RecyclerView myrv;
    private RecyclerViewAdapter myAdapter;
    private TextView img_number;
    private SwipyRefreshLayout swipyRefreshLayout;
    String[] ps_shortcode, ps_display_url, ps_liked, ps_wholeHashtag;
    int check_num = 0;
    int loop_num = 0;
    int post_list_num =0;

    List<Post> postList;
    int c = 0;
    Handler handler, handler2, handler3, handler4, handler5, handler6;

    private String[] all_keyword;
    int length;
    InputStream is = null;
    JSONObject JS;
    JSONArray temp = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search); // search 액티비티로 설정

        Intent intent = getIntent(); //인텐트로 얻고
        aaa_keyword = intent.getStringExtra("keyword"); //keyword변수로 검색어인 keyword 데이터를 얻음
        search_text = (EditText) findViewById(R.id.keyword_zone);
        search_text.setText(aaa_keyword); // search_text의 글자를 keyword로 설정함
        postList = new ArrayList<>(); //ArrayList 만듦. recyclerView에 사용됨
        img_number = (TextView) findViewById(R.id.img_number);
        myrv = (RecyclerView) findViewById(R.id.rec_view);
        myAdapter = new RecyclerViewAdapter(this, postList); //어댑터 설정

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);

        myrv.setLayoutManager(gridLayoutManager);//3행을 가진 그리드뷰로 레이아웃을 만듬
        all_keyword = aaa_keyword.split(" ");
        handler = new Handler() {
            public void handleMessage(Message msg) {
                myrv.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
//                if (myAdapter.getItemCount() <= 12) {
//                    myrv.setAdapter(myAdapter);
//                    myAdapter.notifyDataSetChanged();
//                }
//                //handler.sendEmptyMessage(0); //10ms마다 handleMessage(Message msg) 반복 호출, 즉 10ms 마다 다시 배경색을
//                if (myAdapter.getItemCount() == length) {
//                    super.removeMessages(0);
//                    System.out.println("---------------removeHandler---------------");
//                }
//                if (myAdapter.getItemCount() >= 12) {
//                    super.removeMessages(0);
//                    System.out.println("---------------removeHandler---------------");
//                }

                System.out.println("-----------------firstViewHandler-----  " + length + "  " + myAdapter.getItemCount());
            }
        };

        handler2 = new Handler() {
            public void handleMessage(Message msg) {
                img_number.setText("로딩중");
            }
        };
        handler3 = new Handler() {
            public void handleMessage(Message msg) {
                img_number.setText("로딩끝");
                handler6.removeMessages(0);
            }
        };

        handler4 = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(SearchActivity.super.getApplicationContext(), "데이터를 가져오는 중", Toast.LENGTH_LONG).show();
            }
        };
        handler5 = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(SearchActivity.super.getApplicationContext(), "데이터를 로딩 완료", Toast.LENGTH_LONG).show();
            }
        };
        handler6 = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(SearchActivity.super.getApplicationContext(), "데이터가 더이상 없습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        new Thread() {
            public void run() {
                try {
                    handler4.sendEmptyMessage(0);
                    handler2.sendEmptyMessage(0);
                    System.out.println("\n-----------------Instagram Start--------------\n");

                    for (int i = 0; i < all_keyword.length; i++)
                        Instagram(all_keyword[i]);
                    System.out.println("\n-----------------Instagram finish--------------\n");

                    System.out.println("\n-----------------insert_post Start--------------\n");
                    insert_post(inputData());
                    System.out.println("\n-----------------insert_post finish--------------\n");
                    //handler.sendEmptyMessage(0);

                    handler3.sendEmptyMessage(0);
                    handler5.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        dbHelper = new DBHelper((Context) getApplicationContext(), "LASTWORD.db", null, 1);

        // 날짜는 현재 날짜로 고정
        // 현재 시간 구하기
        long now = System.currentTimeMillis();
        now_date = new Date(now);
        // 출력될 포맷 설정
        simpleDateFormat = new SimpleDateFormat("yyyy. MM. dd.");

    }

//    private void initSwipyRefreshLayout(final String wholeText, final String keyword) {
//
//        swipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh(SwipyRefreshLayoutDirection direction) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //swipyRefreshLayout.setRefreshing(true);
//                        //refresh_post(wholeText, keyword);
//                        try {
//                            //insert_post(inputData());
//                            myrv.setAdapter(myAdapter);
//                            myAdapter.notifyDataSetChanged();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
////                        handler.sendEmptyMessage(0);
//                        //myrv.smoothScrollToPosition(cur);
//                        //cur+=30;
//                        //swipyRefreshLayout.setRefreshing(false);
//                        System.out.println("-----------------secondViewHandler-----------------");
//                    }
//                }, 2000);
//            }
//        });


    //}
    public void refresh_post(final String wholeText, final String keyword) {
        loop_num = 0;
        new Thread() {
            String wholeText2 = wholeText;

            public void run() {

                System.out.println("------------------refresh_post1--------------------");
                if (hasNextPage(wholeText2)) { // 로드해야 할 게시물이 남았다면
                    String cur = getEndCursor(wholeText2); // 현재 불러온 게시물들의 마지막을 가리키는 커서를 수집한다.
                    try {
                        System.out.println("------------------refresh_post2--------------------");
                        wholeText2 = getNextPage(keyword, cur); // 키워드와 커서를 이용하여 다음 게시물의 페이지 소스코드를 가져온다.

                        System.out.println("------------------refresh_post3--------------------");
                        try {
                            getInstaInfoFromKeyword(wholeText2, keyword); // 소스코드에서 shortcode, 이미지링크, 좋아요수를 수집한다.
                            //insert_post(inputData());
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                        System.out.println("------------------refresh_post4--------------------");


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    public void Instagram(String keyword) {
        System.out.println("\n-----------------Instagram2 Start--------------\n");

        try {
            //request 헤더 추가
            System.out.println("\n-----------------Instagram Connect Start--------------\n");

            Document wholeCode = Jsoup.connect("https://www.instagram.com/explore/tags/" + keyword)
                    .header("Cookie",
                            "shbid=2382; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; ds_user_id=3451665929; mid=W3ZeWwAEAAFxRTah2gMqSLyFoUIA; mcd=3; fbm_124024574287414=base_domain=.instagram.com; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; rur=FTW; sessionid=IGSCbe1f22d109edc3691bd08d36485f9e900cac1f8b62fb2e5cf10ded0f97814a81%3A0rPYcNUdl69GWVgZVmcTdaFpnvqx1eZs%3A%7B%22_auth_user_id%22%3A3451665929%2C%22_auth_user_backend%22%3A%22accounts.backends.CaseInsensitiveModelBackend%22%2C%22_auth_user_hash%22%3A%22%22%2C%22_platform%22%3A4%2C%22_token_ver%22%3A2%2C%22_token%22%3A%223451665929%3ATls7kBIRv15DZszC7jFBZUPtxc2cgfrT%3A3a4a3e68491efeb3c447a4201458f7f0edaf37fd82695bc319512ed45ae049bc%22%2C%22last_refreshed%22%3A1534945024.2404546738%7D; fbsr_124024574287414=Rsn_jkyzvupVXCogJy4SMsU7kSm-xvudkXbRjpG6-VA.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUUNEUHRXejduZUMwYXBCZDNHbVdseXFmc1lZQUQzNU01NHFKYVZDaWlJUEx3V3lhR3dYYVVMOXU1MDRvSUFta3puYVA2ZzdLVnplRmNzekNhZW9OYlJmclRCZ0tFbHFUYXpYVkZHUGRxdmFTV3BpVW1mSU90eGpTRkRwZ1hRLUE2NDVseXFoWFAwX2d1UDBzV3I0a0E5OUFfSFoyS2JfSEpFck9CTktNZlRMUmtuQlR2QUxSY01wdW5wdi05N2ZHRnBlMTI2dFNQdVFnX0E2SzFkMU1lajloVWpKa3JiUkt0U0dMSWhlQWRPSkZZODNITW1QV3dXYjlDakdxeFdhV3FkNUxfWms5dDJhOTc5ZUNtTl82N1kwUndtbzRvdGZQb1VoN2kwUUozcGFOZnI4R3dZanJVTlBhTzhHSTlQZFNteDE0ck5yVXdjS21HWFJ2RFZMVE9XRyIsImlzc3VlZF9hdCI6MTUzNDk1MTAzNCwidXNlcl9pZCI6IjEwMDAwNDg2NTA1MDI4OCJ9; shbts=1534951070.1531627; urlgen=\"{\\\"210.93.56.23\\\": 23668\\054 \\\"121.170.57.238\\\": 4766}:1fsUtK:jqGFF0QxE9hkyQhCd-66-w5izC0\"")
                    .userAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
                    .get(); // 링크 연결

            System.out.println("\n-----------------Instagram Connect finish--------------\n");

            Elements scriptCode = wholeCode.select("body > script:eq(1)");
            String wholeText = scriptCode.toString();
            System.out.println("\n-----------------Instagram2 finish--------------\n");

            getInstaInfoFromKeyword(wholeText, keyword);    //	정보를 수집하는 함수

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getInstaInfoFromKeyword(String wholeText, String keyword) throws Exception { // keyword로 첫번째 게시물페이지의 정보를 가져옴
        System.out.println("\n-----------------getInstanInfoFromKeyword Start--------------\n");

        //initSwipyRefreshLayout(wholeText, keyword);

        String[] shortcode = wholeText.split("\"shortcode\":\""); // 특정패턴 검색	/ 전체 문자열에서 필요한 부분만 수집하기 위해서
        String shortcode__end = new String("\",\"edge_medi");

        String[] display_url = wholeText.split("\"display_url\":\"");    // ~부터
        String url__end = new String("\",\"edg");                            // ~까지 추출

        String[] liked = wholeText.split("\"edge_liked_by\":\\{\"count\":");
        String liked__end = new String("},");

        String[] contents = new String[1000];
        contents = wholeText.split("text\":\"");
        String contents__end = new String("\"}}]},\"");

        String[] timestamp = wholeText.split("\"taken_at_timestamp\":");
        String timestamp__end = new String(",\"dimensi");


        length = display_url.length;

        for (int i = 1; i < display_url.length; i++) {
            //shortcode[i] = shortcode[i].substring(0, 11); // 길이가 11인 쇼트코드 추출 (보니깐 간혹 10개 짜리도 있는 것 같은데...)
            try {
                int shortcode_end = shortcode[i].indexOf(shortcode__end);
                if (shortcode_end > -1) {
                    shortcode[i] = shortcode[i].substring(0, shortcode_end);
                }

                int url_end = display_url[i].indexOf(url__end);
                if (url_end > -1) {
                    display_url[i] = display_url[i].substring(0, url_end);
                }

                int like_end = liked[i].indexOf(liked__end);
                if (like_end > -1) {
                    liked[i] = liked[i].substring(0, like_end + liked__end.length() - 2);
                }


                int contents_end = contents[i].indexOf(contents__end);
                if (contents_end > -1) {
                    contents[i] = contents[i].substring(0, contents_end);
                }
                deleteEmoji(contents);
                contents[i] = unicodeConvert(contents[i]);


                int timestamp_end = timestamp[i].indexOf(timestamp__end);
                if (timestamp_end > -1) {
                    timestamp[i] = timestamp[i].substring(0, timestamp_end);
                    // Date를 위해 import java.util.*;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                contents[i] = " ";
            }

        }

        for (int i = 1; i < display_url.length; i++) {
            Log.d("insertData_shortcode", display_url.length + "----" + shortcode[i]);
            insertData("" + liked[i], "" + shortcode[i], "" + display_url[i], "" + URLEncoder.encode("" + contents[i], "UTF-8"));
        }
        System.out.println("\n-----------------getInstanInfoFromKeyword finish--------------\n");

        System.out.println("\n-----------------hasNextPage Start--------------\n");


        while (true) {
            if (post_list_num > 10)
                break;
            if(loop_num>30)
                break;
            loop_num++;
            //Log.d("loop","------------------getInstaInfoFromKeyword refresh1--------------");
            if (hasNextPage(wholeText)) { // 로드해야 할 게시물이 남았다면
                System.out.println("------------------getInstaInfoFromKeyword refresh2--------------");

                String cur = getEndCursor(wholeText); // 현재 불러온 게시물들의 마지막을 가리키는 커서를 수집한다.

                System.out.println("------------------getInstaInfoFromKeyword refresh3--------------");

                try {
                    wholeText = getNextPage(keyword, cur); // 키워드와 커서를 이용하여 다음 게시물의 페이지 소스코드를 가져온다.
                    System.out.println("------------------getInstaInfoFromKeyword refresh4--------------");

                    getInstaInfoFromKeyword(wholeText, keyword); // 소스코드에서 shortcode, 이미지링크, 좋아요수를 수집한다.
                    System.out.println("------------------getInstaInfoFromKeyword refresh5--------------");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //System.out.println("\n-----------------hasNextPage finish--------------\n");

    }

    public String unicodeConvert(String str) {
        StringBuilder sb = new StringBuilder();
        char ch;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            ch = str.charAt(i);
            if (ch == '\\' && str.charAt(i + 1) == 'u') {
                sb.append((char) Integer.parseInt(str.substring(i + 2, i + 6), 16));
                i += 5;
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    //////////////////////////////////////////여기부터///////////////////////////////////
    // 크롤링한 내용들 공백 문자로 대치!
    public void deleteEmoji(String[] contents) { // 크롤링한 게시물들의 이모티콘 부분을 공백으로 대치하는 부분
        for (int i = 1; i < contents.length; i++) {
            Pattern emoticons = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"); // 이모티콘의 정규식을 설정해서
            Matcher emoticonsMatcher = emoticons.matcher(contents[i]);    //	여기서 찾은 다음
            contents[i] = emoticonsMatcher.replaceAll(" ");    //	공백으로 대치
        }
    }
////////////////////////////////////////여기까지///////////////////////////////////


    public boolean hasNextPage(String wholeText) { // 게시물 페이지가 마지막인지 확인하는 함수
        boolean check;
        String[] hasNextPage = wholeText.split("has_next_page\":");    //	~부터
        String end = new String(",\"end_");                            //	~까지 추출
        int hasNextPage_end = hasNextPage[1].indexOf(end);
        if (hasNextPage_end > -1) {
            hasNextPage[1] = hasNextPage[1].substring(0, hasNextPage_end);
        }

        if (hasNextPage[1].equals("true")) {
            System.out.println("---------hasnext : " + hasNextPage[1]);
            check = true;
        } else {
            check = false;
            System.out.println("---------hasnext : " + hasNextPage[1]);
        }

        return check;
    }

    public String getEndCursor(String wholeText) { // nextpage가 있을 때 커서를 확인하는 라인

        String[] endCursor = wholeText.split("\"end_cursor\":\"");
        String end = new String("\"},\"edges\":");
        int endCursor_end = endCursor[1].indexOf(end);
        if (endCursor_end > -1) {
            endCursor[1] = endCursor[1].substring(0, endCursor_end);
        }
        System.out.println("endCursor : " + endCursor[1]);
        if (endCursor[1].equals("null"))
            return "null";
        else
            return endCursor[1];
    }

    public String getNextPage(String keyword, String cur) throws Exception {    //	로드해야할 다음 페이지소스코드를 가져오는 함수
        String url = "https://www.instagram.com/graphql/query/?query_hash=faa8d9917120f16cec7debbd3f16929d&variables={\"tag_name\":\""
                + keyword + "\",\"first\":12,\"after\":\"" + cur + "\"}";
        // request 헤더를 포함시킴
        // System.out.println("++++++++++++++++" + url);

        System.out.println("\n-----------------getNextPage Connect Start--------------\n");

        Connection conn = Jsoup.connect(url)
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7").header("Connection", "keep-alive")
                .header("Cookie",
                        "shbid=2382; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; ds_user_id=3451665929; mid=W3ZeWwAEAAFxRTah2gMqSLyFoUIA; mcd=3; fbm_124024574287414=base_domain=.instagram.com; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; rur=FTW; sessionid=IGSCbe1f22d109edc3691bd08d36485f9e900cac1f8b62fb2e5cf10ded0f97814a81%3A0rPYcNUdl69GWVgZVmcTdaFpnvqx1eZs%3A%7B%22_auth_user_id%22%3A3451665929%2C%22_auth_user_backend%22%3A%22accounts.backends.CaseInsensitiveModelBackend%22%2C%22_auth_user_hash%22%3A%22%22%2C%22_platform%22%3A4%2C%22_token_ver%22%3A2%2C%22_token%22%3A%223451665929%3ATls7kBIRv15DZszC7jFBZUPtxc2cgfrT%3A3a4a3e68491efeb3c447a4201458f7f0edaf37fd82695bc319512ed45ae049bc%22%2C%22last_refreshed%22%3A1534945024.2404546738%7D; fbsr_124024574287414=Rsn_jkyzvupVXCogJy4SMsU7kSm-xvudkXbRjpG6-VA.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUUNEUHRXejduZUMwYXBCZDNHbVdseXFmc1lZQUQzNU01NHFKYVZDaWlJUEx3V3lhR3dYYVVMOXU1MDRvSUFta3puYVA2ZzdLVnplRmNzekNhZW9OYlJmclRCZ0tFbHFUYXpYVkZHUGRxdmFTV3BpVW1mSU90eGpTRkRwZ1hRLUE2NDVseXFoWFAwX2d1UDBzV3I0a0E5OUFfSFoyS2JfSEpFck9CTktNZlRMUmtuQlR2QUxSY01wdW5wdi05N2ZHRnBlMTI2dFNQdVFnX0E2SzFkMU1lajloVWpKa3JiUkt0U0dMSWhlQWRPSkZZODNITW1QV3dXYjlDakdxeFdhV3FkNUxfWms5dDJhOTc5ZUNtTl82N1kwUndtbzRvdGZQb1VoN2kwUUozcGFOZnI4R3dZanJVTlBhTzhHSTlQZFNteDE0ck5yVXdjS21HWFJ2RFZMVE9XRyIsImlzc3VlZF9hdCI6MTUzNDk1MTAzNCwidXNlcl9pZCI6IjEwMDAwNDg2NTA1MDI4OCJ9; shbts=1534951070.1531627; urlgen=\"{\\\"210.93.56.23\\\": 23668\\054 \\\"121.170.57.238\\\": 4766}:1fsUtK:jqGFF0QxE9hkyQhCd-66-w5izC0\"")
                .header("Host", "www.instagram.com").header("Upgrade-Insecure-Requests", "1")
                .userAgent(
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
                .method(Connection.Method.GET).referrer("http://www.instagram.com").ignoreContentType(true);

        System.out.println("\n-----------------getNextPage Connect finish--------------\n");

        Document wholeCode = conn.get();
        System.out.println("\n-----------------getNextPage Connect finish1--------------\n");
        String wholeText = wholeCode.toString();
        System.out.println("\n-----------------getNextPage Connect finish2--------------\n");

        return wholeText;

    }


    public void onClick_search(View view) {
        search_text = (EditText) findViewById(R.id.keyword_zone);

        String item = search_text.getText().toString();
        String date = simpleDateFormat.format(now_date);

        dbHelper.insert(date, item);
        ((MainActivity) MainActivity.CONTEXT).onResume();

        if (view != null) {
            //키보드 숨기기
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        this.finish();
        Intent intent = new Intent(SearchActivity.this, SearchActivity.class);
        intent.putExtra("keyword", "" + item);
        startActivity(intent);
    }

    public void onClick_menu(View view) {
        Intent intent = new Intent(SearchActivity.this, MainActivityDialog.class);
        startActivity(intent);
    }

    private void insert_post(JSONArray temp) throws Exception {

        System.out.println("\n-----------------Insert_post Start--------------\n");
        String[] jsonName = {"likenum", "shortcode", "imageUrl", "wholeHashtag", "keyword"};
        String[][] parseData = new String[temp.length()][jsonName.length];

        System.out.println("\n-----------------JS Start--------------\n");
        for (int i = 0; i < temp.length(); i++) {
            JS = temp.getJSONObject(i);
            if (JS != null) {
                for (int j = 0; j < jsonName.length; j++) {
//                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!" + JS.getString(jsonName[j]));
                    parseData[i][j] = JS.getString(jsonName[j]);
                }
            }
        }
        System.out.println("\n-----------------JS finish--------------\n");

        System.out.println("\n-----------------JS2 Start--------------\n");
        ps_liked = new String[temp.length()];
        ps_shortcode = new String[temp.length()];
        ps_display_url = new String[temp.length()];
        ps_wholeHashtag = new String[temp.length()];

        for (int i = 0; i < temp.length(); i++) {
            if (JS != null) {
                for (int j = 0; j < jsonName.length; j++) {
//                    System.out.println("i : " + i + "j : " + j);
//                    System.out.println(parseData[i][j]);
                    if (j == 0)
                        ps_liked[i] = parseData[i][j];
                    else if (j == 1)
                        ps_shortcode[i] = parseData[i][j];
                    else if (j == 2)
                        ps_display_url[i] = parseData[i][j];
                    else if (j == 3)
                        ps_wholeHashtag[i] = parseData[i][j];
                }
            } else
                System.out.println("--------------!!!------------");
        }
        System.out.println("\n-----------------JS2 finish--------------\n");

        System.out.println("\n-----------------Glide Start--------------\n");

        for (int i = 0; i < ps_liked.length; i++) {
            Log.d("ps_i", "" + i + "  " + ps_shortcode[i]);
            postList.add(new Post("" + ps_display_url[i], "좋아요♡" + ps_liked[i], "" + ps_wholeHashtag[i], "" + aaa_keyword));
            post_list_num++;
            // System.out.println("\n!!!!!!!!!!!!!!!!!!!!!!!Image_URL   "+ps_display_url[i]);
        }

        handler.sendEmptyMessage(0);

        System.out.println("\n-----------------Glide finish--------------\n");
        System.out.println("\n-----------------Insert_post finish--------------\n");
    }

    private JSONArray inputData() throws IOException {
        System.out.println("\n-----------------inputData Start--------------\n");

        //sendData();
        HttpPost request = makeHttpPost("http://ryunha.cafe24.com/user_signup/sendData.php");
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpClient client = new DefaultHttpClient(params);
        ResponseHandler reshandler = new BasicResponseHandler();

        try {
            client.execute(request, reshandler);
            System.out.println("\nsendData_end");
        } catch (Exception e) {
            System.out.println("\nsendData_Exception");
            e.printStackTrace();
        }

        System.out.println("\ninputData_start");
        try {
            HttpResponse response = client.execute(request);
            HttpEntity responseResultEntity = response.getEntity();
            if (responseResultEntity != null) {
                is = responseResultEntity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                Log.d("result", sb.toString());
                String result = sb.toString();
                JS = new JSONObject(result);
                temp = JS.getJSONArray("result");
                System.out.println("\ninputData_end");
            } else
                System.out.println("--------------NULL----------");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n-----------------inputData finish--------------\n");

        return temp;
    }

//    private void sendData() throws IOException {
//        // TODO Auto-generated method stub
//        System.out.println("\n-----------------sendData Start--------------\n");
//        request = makeHttpPost("http://ryunha.cafe24.com/user_signup/sendData.php");
//        HttpParams params = new BasicHttpParams();
//        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
//        client = new DefaultHttpClient(params);
//        reshandler = new BasicResponseHandler();
//
//        try {
//        client.execute(request, reshandler);
//        System.out.println("\nsendData_end");
//        }
// catch (IOException e) {
//            System.out.println("\nsendData_Exception");
//            e.printStackTrace();
//        }
//        System.out.println("\n-----------------sendData finish--------------\n");
//    }

    private void insertData(final String like, final String shortcode, final String imageUrl, final String wholeHashtag) throws IOException {
        // TODO Auto-generated method stub
        //System.out.println("\n-----------------InsertData Start--------------\n");
        new Thread() {

            public void run() {
                try {
                    Log.d("insertData_fun", shortcode);
                    HttpPost request = makeHttpPost(aaa_keyword, like, shortcode, imageUrl, wholeHashtag, "http://ryunha.cafe24.com/user_signup/insertData.php");
                    HttpClient client = new DefaultHttpClient();
                    ClientConnectionManager mgr = client.getConnectionManager();
                    HttpParams params = client.getParams();
                    client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
                    ResponseHandler reshandler = new BasicResponseHandler();
                    client.execute(request, reshandler);

//                    try {
//                        response = client.execute(request);
//
//                        HttpEntity responseResultEntity = response.getEntity();
//                        if (responseResultEntity != null) {
//                            is = responseResultEntity.getContent();
//
//                            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
//                            StringBuilder sb = new StringBuilder();
//                            String line = null;
//
//                            while ((line = reader.readLine()) != null) {
//                                sb.append(line + "\n");
//                            }
//                            is.close();
//                            //Log.d("result", sb.toString());
//                            String result = sb.toString();
//                            Log.d("php_insert", result);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


        //System.out.println("\n-----------------insertData finish--------------\n");

    }

    private HttpPost makeHttpPost(String surl) throws IOException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");
        Log.d("surl", surl);
        String[] keyword_arr = aaa_keyword.split(" ");
        String keyword_final = "";

        int i = 0;
        for (i = 0; i < keyword_arr.length - 1; i++) {
            keyword_final += "'%#" + keyword_arr[i] + "%'" + " AND wholeHashtag LIKE ";
        }
        keyword_final += "'%#" + keyword_arr[i] + "%'";

        Log.d("keyword", keyword_final);
        HttpPost request = new HttpPost(surl);
        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("keyword", URLEncoder.encode("" + keyword_final, "UTF-8")));
        request.setEntity(makeEntity(nameValue));
        System.out.println("\n-----------------makeHttpPost finish--------------\n");
        return request;
    }

    private HttpPost makeHttpPost(String keyword, String like, String shortcode, String imageUrl, String wholeHashtag, String url) throws UnsupportedEncodingException {
        System.out.println("\n-----------------makeHttpPost Start--------------\n");

        HttpPost request = new HttpPost(url);

        //System.out.println("---" + keyword + "----" + like + "----" + shortcode + "---" + imageUrl + "---" + wholeHashtag);
        //System.out.println("\n\n-----------keyword_final----------" + keyword_final);
        Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
        nameValue.add(new BasicNameValuePair("keyword", "" + URLEncoder.encode("" + keyword, "UTF-8")));
        Log.d("keyword2222", keyword);
        nameValue.add(new BasicNameValuePair("likenum", "" + like));
        nameValue.add(new BasicNameValuePair("shortcode", "" + shortcode));
        nameValue.add(new BasicNameValuePair("imageUrl", "" + imageUrl));
        nameValue.add(new BasicNameValuePair("wholeHashtag", "" + wholeHashtag));

        request.setEntity(makeEntity(nameValue));
        System.out.println("\n-----------------makeHttpPost finish--------------\n");

        return request;
    }

    private HttpEntity makeEntity(Vector<NameValuePair> nameValue) {
        HttpEntity result = null;
        try {
            result = new UrlEncodedFormEntity(nameValue);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public void onBackPressed() {
        this.finish();
    }
}


//    public String GetHashtag(String shortcode) throws Exception { // shortcode로 해시태그를 가져오는 함
//        System.out.println("\n-----------------GetHashtag Connect Start--------------\n");
//        Document doc = Jsoup.connect("https://www.instagram.com/p/" + shortcode)
//                .header("Accept",
//                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                .header("Accept-Encoding", "gzip, deflate, br")
//                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7").header("Connection", "keep-alive")
//                .header("Cookie",
//                        "shbid=2382; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; ds_user_id=3451665929; mid=W3ZeWwAEAAFxRTah2gMqSLyFoUIA; mcd=3; fbm_124024574287414=base_domain=.instagram.com; csrftoken=ofJhzxbj5b7DqvZzd9BbecC9ddMYrYxL; rur=FTW; sessionid=IGSCbe1f22d109edc3691bd08d36485f9e900cac1f8b62fb2e5cf10ded0f97814a81%3A0rPYcNUdl69GWVgZVmcTdaFpnvqx1eZs%3A%7B%22_auth_user_id%22%3A3451665929%2C%22_auth_user_backend%22%3A%22accounts.backends.CaseInsensitiveModelBackend%22%2C%22_auth_user_hash%22%3A%22%22%2C%22_platform%22%3A4%2C%22_token_ver%22%3A2%2C%22_token%22%3A%223451665929%3ATls7kBIRv15DZszC7jFBZUPtxc2cgfrT%3A3a4a3e68491efeb3c447a4201458f7f0edaf37fd82695bc319512ed45ae049bc%22%2C%22last_refreshed%22%3A1534945024.2404546738%7D; fbsr_124024574287414=Rsn_jkyzvupVXCogJy4SMsU7kSm-xvudkXbRjpG6-VA.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUUNEUHRXejduZUMwYXBCZDNHbVdseXFmc1lZQUQzNU01NHFKYVZDaWlJUEx3V3lhR3dYYVVMOXU1MDRvSUFta3puYVA2ZzdLVnplRmNzekNhZW9OYlJmclRCZ0tFbHFUYXpYVkZHUGRxdmFTV3BpVW1mSU90eGpTRkRwZ1hRLUE2NDVseXFoWFAwX2d1UDBzV3I0a0E5OUFfSFoyS2JfSEpFck9CTktNZlRMUmtuQlR2QUxSY01wdW5wdi05N2ZHRnBlMTI2dFNQdVFnX0E2SzFkMU1lajloVWpKa3JiUkt0U0dMSWhlQWRPSkZZODNITW1QV3dXYjlDakdxeFdhV3FkNUxfWms5dDJhOTc5ZUNtTl82N1kwUndtbzRvdGZQb1VoN2kwUUozcGFOZnI4R3dZanJVTlBhTzhHSTlQZFNteDE0ck5yVXdjS21HWFJ2RFZMVE9XRyIsImlzc3VlZF9hdCI6MTUzNDk1MTAzNCwidXNlcl9pZCI6IjEwMDAwNDg2NTA1MDI4OCJ9; shbts=1534951070.1531627; urlgen=\"{\\\"210.93.56.23\\\": 23668\\054 \\\"121.170.57.238\\\": 4766}:1fsUtK:jqGFF0QxE9hkyQhCd-66-w5izC0\"")
//                .header("Host", "www.instagram.com").header("Upgrade-Insecure-Requests", "1")
//                .userAgent(
//                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36")
//                .method(Connection.Method.GET).referrer("http://www.instagram.com").ignoreContentType(true)
//                .get(); // 링크 연결
//        System.out.println("\n-----------------GetHashtag Connect finish--------------\n");
//
//        Elements Hashtags = doc.select("meta[property=instapp:hashtags]"); // 해시태그만 뽑아냄
//        String all_hashtag = "";
//        for (Element hashtagLine : Hashtags) { // contents가 metatag에 할당
//            String hashtag = hashtagLine.attr("content");
//            all_hashtag += "#" + hashtag + " ";
//        }
//
//        return all_hashtag;
//    }