package com.andview.example.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.andview.example.R;

public class MainActivity extends Activity {
    private Intent intent = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_list:
                intent = new Intent(this, ListViewActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_gridview:
                intent = new Intent(this, GridViewActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_webView:
                intent = new Intent(this, WebViewActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_customview:
                intent = new Intent(this, CustomViewActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_recylerView:
                intent = new Intent(this, RecyclerViewActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_scrollview:
                intent = new Intent(this, ScrollViewActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_headAd:
                intent = new Intent(this, HeadAdActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_rain:
//                intent = new Intent(this, RainDropActivity.class);
//                startActivity(intent);
                break;

            default:
                break;
        }

    }
}
