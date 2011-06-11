package at.tuwien.dsg.activities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.TweetFlowManager;
import at.tuwien.dsg.entities.Request;

public class TweetflowActivity extends ListActivity {// extends ActionBarActivity {

	private static LinearLayout container;
	
	private static final int SAVE_ID = Menu.FIRST;
	private static final int LOAD_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int FILTER_ID = Menu.FIRST + 3;	
	private static final int CLEAR_ID = Menu.FIRST + 4;
	private static final int CONTEXT_DELETE_REQUEST_ID = Menu.FIRST + 5;
	private static final int TEST_DATA_ID = Menu.FIRST + 6;
	
	
	private static final int FILTER_DIALOG = 4;	
	
	private TweetFlowManager tfm;
	private MyArrayAdapter adapter;
	private Request[] rs;
	private ArrayList<Request> requestTimeline;
	
	// Global mutable variables
    private Uri mUri;
    private Cursor mCursor;
	
	private ActionBar actionBar;
	
	private Menu menu;
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.action_bar);
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		actionBar.setTitle("Requests");
		final Action filterAction = new IntentAction(this, createFilterIntent(), R.drawable.ic_title_share_default);
		
		actionBar.addAction(new RefreshAction());
		actionBar.addAction(new FilterAction());        
		
		//View header = getLayoutInflater().inflate(R.layout.action_bar, null);
		ListView listView = getListView();
		//listView.addHeaderView(header);
		
		tfm = new TweetFlowManager(this);	
		
		
		/*
         * Using the URI passed in with the triggering Intent, gets the note or notes in
         * the provider.
         * Note: This is being done on the UI thread. It will block the thread until the query
         * completes. In a sample app, going against a simple provider based on a local database,
         * the block will be momentary, but in a real app you should use
         * android.content.AsyncQueryHandler or android.os.AsyncTask.
         * 
         * --> use CursorLoader !!!
         */
        /*mCursor = managedQuery(
            mUri,         // The URI that gets multiple notes from the provider.
            null,   // A projection that returns the note ID and note content for each note.
            null,         // No "where" clause selection criteria.
            null,         // No "where" clause selection values.
            null          // Use the default sort order (modification date, descending)
        );   	*/	
		
		//tfm.setTestTFs();
		
		
		//requestTimeline = tfm.loadFilteredRequests();
		//rs = requestTimeline.toArray(new Request[requestTimeline.size()]);	
		
		adapter = new MyArrayAdapter(this, tfm.getFilteredRequests());
		this.setListAdapter(adapter);
		
		registerForContextMenu(listView);
	}
	
	private class RefreshAction extends AbstractAction {

        public RefreshAction() {
            super(R.drawable.refresh);
        }

        @Override
        public void performAction(View view) {
        	tfm.downloadNewTweets();
        	adapter.notifyDataSetChanged();
        }
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
	
	/*
	private void loadRequests() {
		ArrayList<Request> requestTimeline = tfm.receiveTweetflows();
		rs = requestTimeline.toArray(new Request[requestTimeline.size()]);	
		adapter.notifyDataSetChanged();
	}
/*
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// Get the item that was clicked
		Object o = this.getListAdapter().getItem(position);
		String keyword = o.toString();
		Toast.makeText(this, "You selected: " + keyword, Toast.LENGTH_LONG)
				.show();
	}
	*/
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        menu.add(0, SAVE_ID, 0, "Save requests");
        menu.add(0, LOAD_ID, 1, "Load saved requests");
        menu.add(0, CLEAR_ID, 2, "Clear request list");
        menu.add(0, DELETE_ID, 3, "Delete saved requests");
        menu.add(0, TEST_DATA_ID, 4, "Load test data");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case SAVE_ID:        
        	
        	if(tfm.saveRequests()) {
        		// update: eg. displayed state
        		//tfm.loadFilteredRequests();
        		adapter.notifyDataSetChanged();
        		
        		Toast.makeText(this, "Requests saved successfully!", Toast.LENGTH_LONG)
    			.show();
        	}
        	else {
        		Toast.makeText(this, "Error saving Requests!", Toast.LENGTH_LONG)
    			.show();
        	}

	        return true;
        case LOAD_ID:        
        	new LoadDataTask().execute();        	

	        return true;
    	case CLEAR_ID:
    		
    		tfm.clearRequestList();
    		adapter.notifyDataSetChanged();
    		
    		return true;
    	case DELETE_ID:
    		
    		tfm.deleteSavedRequests();
    		//requestTimeline = tfm.loadFilteredRequests();
    		adapter.notifyDataSetChanged();
    		
    		Toast.makeText(this, "All saved Requests deleted!", Toast.LENGTH_LONG)
			.show();
    		
    		return true;
    	case TEST_DATA_ID:
    		
    		tfm.setTestTFs();
    		adapter.notifyDataSetChanged();
    		
    		return true;
    	}
        
        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CONTEXT_DELETE_REQUEST_ID, 0, "Delete request");
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
    	case CONTEXT_DELETE_REQUEST_ID:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

	       	tfm.deleteRequest((int)info.position);
	       	//tfm.loadFilteredRequests();
	       	adapter.notifyDataSetChanged();
	       	
	       	Toast.makeText(this, "Request deleted successfully!", Toast.LENGTH_LONG)
			.show();

	        return true;
		}
		return super.onContextItemSelected(item);
	}
    
    
    private class LoadDataTask extends AsyncTask<Void, Void, ArrayList<Request>> {
    	 
		ProgressDialog retrieveDialog;
 
		@Override
		protected void onPreExecute() {
			retrieveDialog = ProgressDialog.show(TweetflowActivity.this, 
				getText(R.string.request_progress_title), 
				getText(R.string.request_progress_text), 
				true,	// indeterminate duration
				false); // not cancel-able
		}
 
		@Override
		protected ArrayList<Request> doInBackground(Void... arg0) {
			tfm.loadRequestsFromDb();
			return tfm.loadFilteredRequests();
		}
 
		// This is in the UI thread, so we can mess with the UI
		protected void onPostExecute(ArrayList<Request> req) {
			retrieveDialog.dismiss();
			adapter.notifyDataSetChanged();
		}
	}
    
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        /*ListView lv = ((AlertDialog) dialog).getListView();
        boolean[] checked = myDialog.getCheckedBoxes();
        for (int i=0; i<checked.length; i++)
            if (checked[i])
                lv.setItemChecked(i, true);*/
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
                                   //requestTimeline = tfm.loadFilteredRequests();
                                   tfm.loadFilteredRequests();
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
    
    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, ActionSample.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    private Intent createFilterIntent() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Shared from the ActionBar widget.");
        return Intent.createChooser(intent, "Share");
    }
}
