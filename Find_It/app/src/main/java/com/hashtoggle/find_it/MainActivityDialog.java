package com.hashtoggle.find_it;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by MIPUNG on 2018. 7. 25..
 */
public class MainActivityDialog extends Activity {
    protected  void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //다이얼로그처럼 보이게 세팅
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams= new WindowManager.LayoutParams();
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.activity_main_dialog);

        ImageButton close_btn = (ImageButton)findViewById(R.id.close_btn);

    }

    public void onClick_close(View v) {

        MainActivityDialog.this.finish();
    }


    public void onClick_menu_detail(View view){
        switch (view.getId()){
            case R.id.best_tag_btn:
                setContentView(R.layout.activity_dialog_detail);
                ImageView iv = (ImageView)findViewById(R.id.img_view);
                iv.setImageResource(R.drawable.best_tag_info);
                break;
            case R.id.help_btn:
                setContentView(R.layout.activity_dialog_detail);
                ImageView iv2 = (ImageView)findViewById(R.id.img_view);
                iv2.setImageResource(R.drawable.help_info);
                break;
            case R.id.dev_btn:
                setContentView(R.layout.activity_dialog_detail);
                ImageView iv3 = (ImageView)findViewById(R.id.img_view);
                iv3.setImageResource(R.drawable.dev_info);
                break;

        }
    }

    public void onClick_imgview(View view){
        setContentView(R.layout.activity_main_dialog);
    }

}
