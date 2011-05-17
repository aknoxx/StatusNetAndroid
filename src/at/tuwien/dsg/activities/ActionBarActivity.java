package at.tuwien.dsg.activities;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import at.tuwien.dsg.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActionBarActivity extends Activity {

	private ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.container);
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		
        

        final Action shareAction = new IntentAction(this, createShareIntent(), R.drawable.ic_title_share_default);
        actionBar.addAction(shareAction);
        final Action searchAction = new IntentAction(this, new Intent(this, SearchActivity.class), R.drawable.ic_title_export_default);
        actionBar.addAction(searchAction);
	}
	
	public void setTitle(String title) {
		actionBar.setTitle(title);
	}
	
	public static Intent createIntent(Context context) {
        Intent i = new Intent(context, ActionSample.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    private Intent createShareIntent() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Shared from the ActionBar widget.");
        return Intent.createChooser(intent, "Share");
    }
}
