package at.tuwien.dsg.activities;

import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import at.tuwien.dsg.R;

public class LoginActivity extends Activity {

	private ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login_view);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		actionBar.setTitle("Login");
		
		Button btnLogin = (Button) findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	redirectToTweetFlowTimeline();
            }
		});
	}
	
	private void redirectToTweetFlowTimeline() {
		startActivity(new Intent(this, OAuthActivity.class));				
	}
}
