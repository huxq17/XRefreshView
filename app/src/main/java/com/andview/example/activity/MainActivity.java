package com.andview.example.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.andview.example.R;
import com.andview.refreshview.utils.LogUtils;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //如果不想XRefreshView后台输出log，此处传入false即可
        LogUtils.enableLog(true);
    }

    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.bt_list:
                intent = new Intent(this, ListViewActivity.class);
                break;
            case R.id.bt_gridview:
                intent = new Intent(this, GridViewActivity.class);
                break;
            case R.id.bt_webView:
                intent = new Intent(this, WebViewActivity.class);
                break;
            case R.id.bt_customview:
                intent = new Intent(this, CustomViewActivity.class);
                break;
            case R.id.bt_recylerView:
                intent = new Intent(this, RecyclerViewsActivity.class);
                break;
            case R.id.bt_scrollview:
                intent = new Intent(this, ScrollViewActivity.class);
                break;
            case R.id.bt_headAd:
                intent = new Intent(this, HeadAdActivity.class);
                break;
            case R.id.bt_not_full_screen:
                intent = new Intent(this, NotFullScreenActivity.class);
                break;
            case R.id.bt_not_fullscreen_nofooter:
                intent = new Intent(this, NotFullScreenWithoutFooterActivity.class);
                break;
            case R.id.bt_emptyview:
                intent = new Intent(this, EmptyViewActivity.class);
                break;
            case R.id.bt_smileview:
                intent = new Intent(this, SmileViewActivity.class);
                break;
            case R.id.bt_rain:
//                intent = new Intent(this, RainDropActivity.class);
                break;
            default:
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }

    }
}
