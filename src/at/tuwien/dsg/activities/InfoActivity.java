package at.tuwien.dsg.activities;

import java.io.File;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.ConnectionManager;
import at.tuwien.dsg.common.TweetFlowManager;

public class InfoActivity extends MyActivity {

	private ActionBar actionBar;
	private SharedPreferences.Editor mSettingsEditor;
	private TweetFlowManager tfm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.TAG = "InfoActivity";
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.info_view);
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		final Action requestsIntentAction = new IntentAction(this, new Intent(this, TweetflowActivity.class), R.drawable.ic_title_home_default);
		actionBar.setHomeAction(requestsIntentAction);		
		actionBar.setTitle("Info");
		
		final Action viewSavedRequestsIntentAction = new IntentAction(this, new Intent(this, SavedRequestsActivity.class), R.drawable.lock);

		actionBar.addAction(viewSavedRequestsIntentAction);
		
		mSettingsEditor = getSharedPreferences(OAuthActivity.PREFS, Context.MODE_PRIVATE).edit();
		
		Button btnLogout = (Button) findViewById(R.id.btn_logout);
		btnLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	// TODO: do logout stuff
            	mSettingsEditor.putBoolean(ConnectionManager.LOGGEDIN, false);
				mSettingsEditor.commit();
				
				tfm = TweetFlowManager.getInstance(getApplicationContext(), null);
				tfm.clearRequestList();
				tfm.resetIds();
				
				//File file = new File(TweetflowActivity.TF_MANAGER_FILE);
				//file.delete();
            	
            	ConnectionManager.getInstance(getApplicationContext()).logout();
            	
            	redirectToLogin();
            }
		});
		
		TextView tvUsername = (TextView) findViewById(R.id.tv_Username);
		TextView tvNetwork = (TextView) findViewById(R.id.tv_Network);
		TextView tvBaseUrl = (TextView) findViewById(R.id.tv_BaseUrl);
		
		tvUsername.setText("Username");
		tvNetwork.setText(ConnectionManager.getInstance(this).getCurrentNetwork().getName());
		tvBaseUrl.setText(ConnectionManager.getInstance(this).getCurrentNetwork().getRestBaseURL());
	}
	
	private void redirectToLogin() {
		startActivity(new Intent(this, LoginActivity.class));
	}
}
