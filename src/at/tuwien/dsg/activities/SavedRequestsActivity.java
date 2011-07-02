package at.tuwien.dsg.activities;

import java.util.Iterator;
import java.util.Map.Entry;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.ConnManager;
import at.tuwien.dsg.common.TweetFlowManager;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class SavedRequestsActivity extends MyListActivity {
	
	private static final int FILTER_DIALOG = 4;	
	
	private ActionBar actionBar;
	private ConnManager mConnManager;
	
	private TweetFlowManager tfm;
	private MyArrayAdapter adapter;
	
	public void onCreate(Bundle icicle) {
		this.TAG = "SavedRequestsActivity";
		super.onCreate(icicle);
		
		setContentView(R.layout.request_view);
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		actionBar.setTitle("View saved Requests");
		final Action infoIntentAction = new IntentAction(this, new Intent(this, InfoActivity.class), R.drawable.info);
		final Action requestsIntentAction = new IntentAction(this, new Intent(this, TweetflowActivity.class), R.drawable.requests);
		       
		actionBar.addAction(new FilterAction());
		actionBar.addAction(requestsIntentAction);
		actionBar.addAction(infoIntentAction);
		
		tfm = TweetFlowManager.getInstance(this, null);
		tfm.loadRequestsFromDb();
		adapter = new MyArrayAdapter(this, tfm.getSavedFilteredRequests());
		this.setListAdapter(adapter);
	}
	
	private class FilterAction extends AbstractAction {

        public FilterAction() {
            super(R.drawable.filter);
        }

        @Override
        public void performAction(View view) {
        	showDialog(FILTER_DIALOG);
        }
    }
	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

                case FILTER_DIALOG:

                	Iterator<Entry<CharSequence, Boolean>> iter = 
                		tfm.getDisplayFilter().entrySet().iterator();
                    
                	CharSequence[] types = new CharSequence[tfm.getDisplayFilter().size()];
                	boolean[] checkedItems = new boolean[tfm.getDisplayFilter().size()];
                	int i=0;
                	while(iter.hasNext()) {
                		Entry<CharSequence, Boolean> e = iter.next();
                		types[i] = e.getKey();
                		checkedItems[i] = (boolean)e.getValue();
                		i++;
                	}
                	
                	final CharSequence[] fTypes = types;
                    final boolean[] fCheckedItems = checkedItems;
                    
                    return new AlertDialog.Builder(this).setTitle(
                            "Requests to display").setMultiChoiceItems(
                            		fTypes, checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
                                	fCheckedItems[whichButton] = isChecked;
                                }
                            }).setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                   
                                   for (int j = 0; j < fCheckedItems.length; j++) {
                                	   tfm.getDisplayFilter().put(fTypes[j], 
                                			   new Boolean(fCheckedItems[j]));
                                   }
                                   tfm.loadRequestsFromDb();
                                   adapter.notifyDataSetChanged();
                                }
                            }).setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                }
                            }).create();
        }
        return null;
    }
}
