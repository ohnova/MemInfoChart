package com.gihasil.lab.meminfochart.activity;

import com.gihasil.lab.meminfochart.chart.IAChart;
import com.gihasil.lab.meminfochart.chart.MemoryLineChart;
import com.gihasil.lab.meminfochart.db.MemInfoDBHandler;
import com.gihasil.lab.meminfochart.service.AccumMemoryService;
import com.gihasil.lab.meminfochart.service.AlwaysOnTopService;
import com.gihasil.lab.meminfochart.service.MemCheckServiceHelper;
import com.gihasil.lab.meminfochart.service.NotificationLoader;
import com.gihasil.lab.meminfochart.utils.AppInfo;
import com.gihasil.lab.meminfochart.utils.MemInfoLog;
import com.gihasil.lab.meminfochart.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	/* AdMob */
	private AdView mAdView = null;
	private static final String AD_UNIT_ID = "ca-app-pub-2736235758520293/6303196362";
	
	public static final int GATHERING_ALARM_CODE = 0x123;
	private static final int MENU_ITEM_RESET = 1;
	private static final int MENU_ITEM_START_SERVICE = 2;
	private static final int MENU_ITEM_STOP_SERVICE = 3;
	private static final int MENU_ITEM_STOP_POPUP = 4;
	private static final int MENU_SETTINGS = 5;
	
	private static final int CONTEXT_MENU_MONKEY = 1;
	private static final int CONTEXT_MENU_FOCUSING_TEST = 2;
	
	private Context mContext;

	private Cursor mCursor;
	private AppsListAdapter mAdapter;
	private ListView mListView;

	private static final int SECS = 1000;
	public static final int MINS = 60 * SECS;
	
	private IAChart[] mCharts = new IAChart[] { new MemoryLineChart() };

	private MemInfoDBHandler mDBHandler = null;
	private MemCheckServiceHelper mServiceHelper = null;

	ProgressDialog mDlg;
	
	private static boolean mPaid = true;
	private AlertDialog mDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(AccumMemoryService.START_LIST_VIEW_INTENT);	
		registerReceiver(mDbDoneReceiver, intentFilter);
		
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
        
		mContext = getApplicationContext();
		mServiceHelper = new MemCheckServiceHelper(mContext);
		
		if (savedInstanceState == null) {			
			mServiceHelper.startMemCheckService(true);
		}				
		
		initListView();		
	}

	public void startLoadingAni() {

		mDlg = new ProgressDialog(this);
		mDlg = new ProgressDialog(this);
		mDlg.setTitle(getResources().getText(R.string.loading));
		mDlg.setMessage(getResources().getText(R.string.collecting));
		mDlg.setIndeterminate(true);
		mDlg.setCancelable(true);
		mDlg.show();
	}
	
	public void stopLoadingAni() {
		if (mDlg != null) {
			mDlg.dismiss();
			mDlg = null;
		}
	}	

	@Override
	protected void onDestroy() {
		stopLoadingAni();
		unregisterReceiver(mDbDoneReceiver);
	    // Destroy the AdView.
	    if (mAdView != null) {
	    	mAdView.destroy();
	    }
		super.onPause();
	}

	@Override
	protected void onStart() {		
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdView != null) {
			mAdView.resume();
		}

	}

	@Override
	protected void onPause() {
		if (mAdView != null) {
			mAdView.pause();
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0, MENU_ITEM_START_SERVICE, 0, R.string.start_service_menu);
		menu.add(1, MENU_ITEM_STOP_SERVICE, 0, R.string.stop_service_menu);
		if (mPaid == true)
			menu.add(2, MENU_ITEM_STOP_POPUP, 0, R.string.stop_popup);
		menu.add(3, MENU_ITEM_RESET, 0, R.string.reset);
		menu.add(4, MENU_SETTINGS, 0, R.string.settings);
		return true;
	}
	
	private void refreshView() {
		mDBHandler = MemInfoDBHandler.open(this);
		mCursor = mDBHandler.dbSelectAllCusor();
		mAdapter = new AppsListAdapter(this, mCursor);
		mListView.setAdapter(mAdapter);
	}
	
    private AlertDialog createDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(R.string.dialog_title_list_preference);
        ab.setMessage(R.string.delete_db);
        ab.setCancelable(true);
          
        ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
    			if (mDBHandler != null) {
    				mDBHandler.deleteAll();
    				Toast.makeText(mContext, R.string.erased_all,
    						Toast.LENGTH_SHORT).show();
    			}
                setDismiss(mDialog);
            }
        });
          
        ab.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setDismiss(mDialog);
            }
        });
          
        return ab.create();
    }
    
    private void setDismiss(Dialog dialog){
        if(dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_RESET:
			mDialog = createDialog();
			mDialog.show();
			break;
		case MENU_ITEM_START_SERVICE:			
			if (!mServiceHelper.startMemCheckService())
				Toast.makeText(mContext, R.string.already_started, Toast.LENGTH_SHORT).show();			
			break;
		case MENU_ITEM_STOP_SERVICE:
			if (mServiceHelper.stopMemCheckService())							
				Toast.makeText(mContext, R.string.stopped_service, Toast.LENGTH_SHORT).show();
			else 
				Toast.makeText(mContext, R.string.no_service_started, Toast.LENGTH_SHORT).show();
			break;
		case MENU_ITEM_STOP_POPUP:
			Intent intent = new Intent(this, AlwaysOnTopService.class);
			stopService(intent);
			break;
		case MENU_SETTINGS:
			startActivity(new Intent(getApplicationContext(), SettingActivity.class));
			break;
		case R.id.action_refresh:
			refreshView();
			Toast.makeText(mContext, R.string.updated, Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		mCursor = mAdapter.getCursor();
		String selectedPackage = null;
		if (mCursor != null) {
			mCursor.moveToPosition(info.position);	
			selectedPackage = mCursor.getString(1);
		}			
		
		switch (item.getItemId()) {
		case CONTEXT_MENU_MONKEY:
			showMonkeyAlertDialog(selectedPackage);								
			break;
		case CONTEXT_MENU_FOCUSING_TEST:
			Intent intent = new Intent(this, AlwaysOnTopService.class);
			intent.putExtra("package", selectedPackage);
			stopService(intent);
			startService(intent);
			break;
		}
		
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {		
		if (mPaid == true) {
			menu.setHeaderTitle(R.string.title_function);
			//menu.add(0, CONTEXT_MENU_MONKEY, 0, R.string.monkey_test);
			menu.add(0, CONTEXT_MENU_FOCUSING_TEST, 0, R.string.focus_test);
			
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	private void initListView() {
		MemInfoLog.D("initListView");
		
		mDBHandler = MemInfoDBHandler.open(this);
		mCursor = mDBHandler.dbSelectAllCusor();

		mListView = (ListView) findViewById(R.id.app_listview);		
			
		if (mCursor != null) {
			mAdapter = new AppsListAdapter(this, mCursor);
			mListView.setAdapter(mAdapter);
			registerForContextMenu(mListView);
			
			mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {

					mCursor.moveToPosition(position);
					String packageName = mCursor.getString(1);					

					Intent intent = new Intent(mContext, MemoryChartActivity.class);
					intent.putExtra("name", packageName);
					startActivity(intent);
				}
			}); 
		}
	}

	private void showMonkeyAlertDialog(final String packageName){
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("몽키 테스트 진행 여부 확인");
		LayoutInflater layout = getLayoutInflater();
		View view = layout.inflate( R.layout.dialog_custom, null );
		final EditText editText = (EditText) view.findViewById(R.id.custom_command);
		final String command = "monkey -p " + packageName + " -v --throttle 500 10000";
		editText.setText(command);
		builder.setView(view);
		builder.setPositiveButton(getString(android.R.string.yes), new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// make events 100000 time.. (within 1 min..)					
				if(editText!=null){
					if(!editText.getText().toString().isEmpty()){						
						return;
					}
				}				
				AccumMemoryService.makeMonkeyEvent(command, true);
				Toast.makeText(mContext, "몽키테스트가  진행 되었습니다.", Toast.LENGTH_SHORT).show();							
				/*
				Intent intent = mCharts[0].execute(mContext, packageName);
				if (intent != null) {
					startActivity(intent);
				}
				*/
			}
		});		
		builder.setNegativeButton(getString(android.R.string.no), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Toast.makeText(mContext, "취소합니다.", Toast.LENGTH_SHORT).show();
				Intent intent = mCharts[0].execute(mContext, packageName);
				if (intent != null) {
					startActivity(intent);
				}
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public BroadcastReceiver mDbDoneReceiver = new BroadcastReceiver() {		
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String name = intent.getAction();
			MemInfoLog.D("");
			if (name.equals(AccumMemoryService.START_LIST_VIEW_INTENT)) {
				mAdapter.notifyDataSetChanged();
				if (MemCheckServiceHelper.needToUpdate(mContext)) {					
					refreshView();
				}
			}
		}
	};	
}
