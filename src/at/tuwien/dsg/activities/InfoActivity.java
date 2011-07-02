package at.tuwien.dsg.activities;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.ConnManager;

public class InfoActivity extends Activity {

	private ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.info_view);
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		actionBar.setTitle("Info");
		final Action requestsIntentAction = new IntentAction(this, new Intent(this, TweetflowActivity.class), R.drawable.requests);
		final Action viewSavedRequestsIntentAction = new IntentAction(this, new Intent(this, SavedRequestsActivity.class), R.drawable.lock);
		
		actionBar.addAction(requestsIntentAction);	
		actionBar.addAction(viewSavedRequestsIntentAction);
		
		Button btnLogout = (Button) findViewById(R.id.btn_logout);
		btnLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	// TODO: do logout stuff
            	
            	ConnManager.getInstance(getApplicationContext()).logout();
            	
            	redirectToLogin();
            }
		});
		
		TextView tvUsername = (TextView) findViewById(R.id.tv_Username);
		TextView tvNetwork = (TextView) findViewById(R.id.tv_Network);
		TextView tvBaseUrl = (TextView) findViewById(R.id.tv_BaseUrl);
		
		tvUsername.setText("Username");
		tvNetwork.setText(ConnManager.getInstance(this).getCurrentNetwork().getName());
		tvBaseUrl.setText(ConnManager.getInstance(this).getCurrentNetwork().getRestBaseURL());
	}
	
	private void redirectToLogin() {
		startActivity(new Intent(this, LoginActivity.class));
	}
}
