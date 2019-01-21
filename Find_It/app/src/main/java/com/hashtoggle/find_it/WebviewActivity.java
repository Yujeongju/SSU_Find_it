package com.hashtoggle.find_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebviewActivity extends Activity {

    String search_keyword;
    WebView webView;
    protected void onCreate(Bundle savedInstaneState)
    {
        super.onCreate(savedInstaneState);
        setContentView(R.layout.activity_webview);

        Intent intent = getIntent();
        search_keyword = intent.getStringExtra("search_keyword");
        webView = (WebView)findViewById(R.id.naver_Web);

        //새로운 창이 뜨지 않게 하기 위한 코드
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.loadUrl("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query="+search_keyword);
    }

    public void onClick_return(View view){
        finish();
    }

}
