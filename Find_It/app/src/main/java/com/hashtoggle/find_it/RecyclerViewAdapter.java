package com.hashtoggle.find_it;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHoler> {

    private Context mContext;
    private List<Post> mData;
    HashTagHelper mTextHashTagHelper;
    private EditText bottom;

    public RecyclerViewAdapter(Context mContext, List<Post> mData){
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyViewHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.card_view_item, viewGroup, false);

        return new MyViewHoler(view);
    }

    int j;
    @Override
    public void onBindViewHolder(@NonNull MyViewHoler myViewHoler, final int i) {
        myViewHoler.card_like.setText(String.valueOf(mData.get(i).getLike()));
        myViewHoler.card_content.setText(String.valueOf(mData.get(i).getHashtag()));
        Glide.with(mContext).load(""+mData.get(i).getCard_url()).placeholder(R.drawable.loading).into(myViewHoler.card_img);

        mTextHashTagHelper = HashTagHelper.Creator.create(mContext.getResources().getColor(R.color.colorPrimary), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {
                Log.d("hashtag", hashTag);
            }
        });
        mTextHashTagHelper.handle(myViewHoler.card_content);


//        myViewHoler.cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                new Thread(){
//                    public void run(){
//                        String final_hashtag = "";
//                        try {
//                            Intent intent = new Intent(mContext, PostActivity.class);
//                            intent.putExtra("url", mData.get(i).getCard_url().toString());
//                            intent.putExtra("hashtag", mData.get(i).getHashtag().toString());
//                            intent.putExtra("like", mData.get(i).getLike());
//                            intent.putExtra("keyword", mData.get(i).getKeyword().toString());
//                            mContext.startActivity(intent);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//
//
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHoler extends RecyclerView.ViewHolder{

        TextView card_like;
        TextView card_content;
        ImageView card_img;
        CardView cardView ;


        public MyViewHoler(@NonNull View itemView) {
            super(itemView);
            card_like = (TextView)itemView.findViewById(R.id.card_like);
            card_content = (TextView)itemView.findViewById(R.id.card_content);
            card_img = (ImageView)itemView.findViewById(R.id.card_img);
            cardView = (CardView)itemView.findViewById(R.id.card_view);


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
//        System.out.println("+++++++++++"+all_hashtag+"++++++++++");
//
//        return all_hashtag;
//    }
}
