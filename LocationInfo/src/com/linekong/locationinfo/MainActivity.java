package com.linekong.locationinfo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button bt_init;
	private Button bt_getInfo;
	private TextView tv_info;
	private Location location;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bt_init = (Button) findViewById(R.id.bt_init);
		bt_getInfo = (Button) findViewById(R.id.bt_getInfo);
		tv_info = (TextView) findViewById(R.id.tv_info);
		
		bt_init.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				location = new Location(getApplicationContext());
			}
		});
		bt_getInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (location!=null) {
					location.getLocation();
				}else{
					Log.e("wgq", "init baidulocation fail");
				}
			}
		});
	}
}
