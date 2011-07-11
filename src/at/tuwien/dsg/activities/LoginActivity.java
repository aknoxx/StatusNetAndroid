package at.tuwien.dsg.activities;

import java.util.List;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.ConnectionManager;
import at.tuwien.dsg.common.TweetFlowManager;
import at.tuwien.dsg.entities.Network;

public class LoginActivity extends MyActivity {

	private ConnectivityManager cm;
	private ActionBar actionBar;
	private TweetFlowManager tfm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.TAG = "LoginActivity";
		super.onCreate(savedInstanceState);
		
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		setContentView(R.layout.login_view);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		final Action requestsIntentAction = new IntentAction(this, new Intent(this, LoginActivity.class), R.drawable.ic_title_home_default);
		actionBar.setHomeAction(requestsIntentAction);
		actionBar.setTitle("Login");
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		
		if(extras != null) {
			if(extras.getBoolean("aborted")) {
				Toast.makeText(this, "Error while logging in, probably you have to change"
						+ " your network's base-url!", Toast.LENGTH_LONG);
			}
		}
		
		tfm = TweetFlowManager.getInstance(this, null);
		
		Button btnLogin = (Button) findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(!isOnline()) {
            		Toast.makeText(getApplicationContext(), "No internet connection available!", Toast.LENGTH_LONG).show();
            	}
            	else {               		
            		
            		final List<Network> n = tfm.getAllNetworks();

            		CharSequence[] networks = new CharSequence[n.size()];
            		
            		for (int j = 0; j < networks.length; j++) {
            			networks[j] = n.get(j).getName();
            		}            		
            		final CharSequence[] items = networks;
            		
            		AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            		builder.setTitle("Select a network");
            		builder.setItems(items, new DialogInterface.OnClickListener() {
            		    public void onClick(DialogInterface dialog, int itemIndex) {
            		    	ConnectionManager.restartConnectionManagerWithNewNetwork(n.get(itemIndex));            		        
            		        startActivity(new Intent(LoginActivity.this, OAuthActivity.class));
            		    }
            		});
            		
            		AlertDialog alert = builder.create();
            		alert.show();         		
            	}
            }
		});
	}
	
	public boolean isOnline() {
		 return cm.getActiveNetworkInfo().isConnected();
	}
}
