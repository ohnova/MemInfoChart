package com.gihasil.lab.meminfochart.activity;

import android.app.Activity;
import android.os.Bundle;

public class SettingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 getFragmentManager().beginTransaction().replace(android.R.id.content,
	                new SettingPreferenceFragment()).commit();
	}

	public SettingActivity() {
		// TODO Auto-generated constructor stub
	}

}
