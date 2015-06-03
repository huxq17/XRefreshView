package com.andview.refreshview;

import com.andview.refreshview.example.ListViewActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	private Intent intent = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	public void onClick(View v){
		switch (v.getId()) {
		case R.id.bt_list:
			intent = new Intent(this,ListViewActivity.class);
			startActivity(intent);
			break;
		case R.id.bt_gridview:
			
			break;
		case R.id.bt_webView:
			
			break;

		default:
			break;
		}
		
	}
}
