package at.tuwien.dsg.activities;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

public class MyActivity extends Activity {

	protected String TAG = "MyListActivity";
	private ConnectivityManager cm;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	protected boolean isOnline() {
		 return cm.getActiveNetworkInfo().isConnected();
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart()");
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart()");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop()");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}
	
	public void onFinish() {
		Log.d(TAG, "onFinish()");
	}
}
