package com.gihasil.lab.meminfochart.activity;

import com.gihasil.lab.meminfochart.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppsListAdapter extends CursorAdapter {
	private int mGroup;
	private PackageManager pm;

	@SuppressWarnings("deprecation")
	public AppsListAdapter(Context context, Cursor c) {
		super(context, c);
		pm = context.getPackageManager();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewgroup) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.gvoice_list_item, viewgroup,
				false);

		return view;
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {
		String appName = null;
		long id = c.getLong(0);
		String packageName = c.getString(1);		
//		long pssMem = c.getLong(2);
//		long timestamp = c.getLong(3);
		
		try {
			appName = (String) context.getPackageManager().getApplicationLabel(
					context.getPackageManager().getApplicationInfo(packageName,
							PackageManager.GET_UNINSTALLED_PACKAGES));
		} catch (NameNotFoundException e1) {
			// do nothing
			//e1.printStackTrace();
		}

		ImageView imgView = (ImageView) v.findViewById(R.id.g_image);
		TextView titleText = (TextView) v.findViewById(R.id.list_title_txt);
		TextView wordText = (TextView) v.findViewById(R.id.list_word_txt);

		try {
			imgView.setBackground(pm.getApplicationIcon(packageName));																		
		} catch (NameNotFoundException e) {
			//e.printStackTrace();
			imgView.setBackground(null);
		}
		titleText.setText(appName);
		wordText.setText(packageName);	
	}

}
