package at.tuwien.dsg.activities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
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
import at.tuwien.dsg.common.ConnManager;
import at.tuwien.dsg.common.Request.Conditions;
import at.tuwien.dsg.common.Request.HashTags;
import at.tuwien.dsg.common.Request.Requests;
import at.tuwien.dsg.common.Request.Variables;
import at.tuwien.dsg.common.TweetFlowManager;
import at.tuwien.dsg.entities.DisplayData;
import at.tuwien.dsg.entities.Network;
import at.tuwien.dsg.entities.Request;

public class TweetflowActivity extends MyListActivity {// extends ActionBarActivity {
	
	private static LinearLayout container;
	
	private static final int SAVE_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int RESET_ID = Menu.FIRST + 3;	
	private static final int CLEAR_ID = Menu.FIRST + 4;
	private static final int CONTEXT_DELETE_REQUEST_ID = Menu.FIRST + 5;
	
	
	private static final int FILTER_DIALOG = 4;	
	
	private TweetFlowManager tfm;
	private MyArrayAdapter adapter;
	private Request[] rs;
	private ArrayList<Request> requestTimeline;
	
	// Global mutable variables
    private Uri mUri;
    private Cursor mCursor;
    
    private ContentProviderClient requestsProvider;
    private ContentProviderClient hashTagsProvider;
    private ContentProviderClient conditionsProvider;
    private ContentProviderClient variablesProvider;
	
	private ActionBar actionBar;
	private ConnManager mConnManager;
	
	private SharedPreferences mSettings;
	
	private boolean loggedIn = false;
	
	private static final String TF_MANAGER_FILE = "TFManagerFile";
	private static final String CONNECTION_MANAGER_FILE = "ConnectionManagerFile";
	
	private Menu menu;
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle icicle) {
		this.TAG = "TweetflowActivity";
		super.onCreate(icicle);
		
		setContentView(R.layout.request_view);
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		actionBar.setTitle("Requests");
		final Action infoIntentAction = new IntentAction(this, new Intent(this, InfoActivity.class), R.drawable.info);
		final Action viewSavedRequestsIntentAction = new IntentAction(this, new Intent(this, SavedRequestsActivity.class), R.drawable.lock);
		
		actionBar.addAction(new RefreshAction());
		actionBar.addAction(new FilterAction());
		actionBar.addAction(viewSavedRequestsIntentAction);
		actionBar.addAction(infoIntentAction);
		
		ListView listView = getListView();
		
		FileInputStream fis = null;
		ObjectInputStream in = null;
		DisplayData dd;		
		try {
			fis = openFileInput(TF_MANAGER_FILE);
			in = new ObjectInputStream(fis);
			dd = (DisplayData) in.readObject();
			in.close();
		} catch (Exception e) {
			dd = null;
		}		
		tfm = TweetFlowManager.getInstance(this, dd);
		
		Network n;
		try {
			fis = openFileInput(CONNECTION_MANAGER_FILE);
			in = new ObjectInputStream(fis);
			n = (Network) in.readObject();
			in.close();
		} catch (Exception e) {
			n = null;
		}	
		if(n != null) {
			ConnManager.restartConnectionManagerWithNewNetwork(n);
		}		
		mConnManager = ConnManager.getInstance(this);
		
		// Get Content Providers
		requestsProvider = getContentResolver().acquireContentProviderClient(Requests.CONTENT_URI);
		hashTagsProvider = getContentResolver().acquireContentProviderClient(HashTags.CONTENT_URI);
		conditionsProvider = getContentResolver().acquireContentProviderClient(Conditions.CONTENT_URI);
		variablesProvider = getContentResolver().acquireContentProviderClient(Variables.CONTENT_URI);
		
		adapter = new MyArrayAdapter(this, tfm.getFilteredRequests());
		this.setListAdapter(adapter);
		
		registerForContextMenu(listView);
		
		mSettings = getSharedPreferences(OAuthActivity.PREFS, Context.MODE_PRIVATE);
	}
	
	@Override
	public void onResume() {
		super.onResume();

		loggedIn = mSettings.getBoolean(ConnManager.LOGGEDIN, false);
		if(!loggedIn) {
			if(mConnManager.getKeysAvailable()) {
				new GetCredentialsTask().execute();
			}
			else {
				// sets the Network
				startActivity(new Intent(this, LoginActivity.class));
			}
		}
		else {
			ConnManager.TimelineSelector ss = 
				mConnManager.new TimelineSelector(ConnManager.getInstance(this).getUrls().getHomeTimelineUrlString(),
        				//tfm.getNewestReceivedId(), null, null, null);
						null, null, null, null);
			new GetTimelineWithProgressTask().execute(ss);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		FileOutputStream fos = null;
		ObjectOutputStream out = null;		
		try {
			fos = openFileOutput(TF_MANAGER_FILE, Context.MODE_PRIVATE);
			out = new ObjectOutputStream(fos);
			out.writeObject(tfm.getDd());
			out.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		try {
			fos = openFileOutput(CONNECTION_MANAGER_FILE, Context.MODE_PRIVATE);
			out = new ObjectOutputStream(fos);
			out.writeObject(mConnManager.getCurrentNetwork());
			out.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}	
	}
	
	public void onFinish() {
		super.onFinish();
		
		mConnManager.shutdownConnectionManager();
	}
	
	//----------------------------
	// This task is run on every onResume(), to make sure the current credentials are valid.
	// This is probably overkill for a non-educational program
	private class GetCredentialsTask extends AsyncTask<Void, Void, JSONObject> {
 
		ProgressDialog authDialog;
 
		@Override
		protected void onPreExecute() {
			authDialog = ProgressDialog.show(TweetflowActivity.this, 
				getText(R.string.auth_progress_title), 
				getText(R.string.auth_progress_text),
				true,	// indeterminate duration
				false); // not cancel-able
		}
		
		@Override
		protected JSONObject doInBackground(Void... arg0) {
			return mConnManager.getCredentials();
		}
		
		// This is in the UI thread, so we can mess with the UI
		protected void onPostExecute(JSONObject jso) {
			authDialog.dismiss();
			if(jso != null) {
				ConnManager.TimelineSelector ss = 
					mConnManager.new TimelineSelector(ConnManager.getInstance(getApplicationContext()).getUrls().getHomeTimelineUrlString(),
	        				//tfm.getNewestReceivedId(), null, null, null);
							null, null, null, null);
				new GetTimelineWithProgressTask().execute(ss);
			}
		}
	}
	
	private class GetTimelineWithProgressTask extends AsyncTask<ConnManager.TimelineSelector, Void, JSONArray> {
		
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
		protected JSONArray doInBackground(ConnManager.TimelineSelector... params) {
			return mConnManager.getTimeline(params[0]);
		}
		
		// This is in the UI thread, so we can mess with the UI
		protected void onPostExecute(JSONArray array) {
			if(array != null) {
				try {
					for(int i = array.length()-1; i >=0 ; i--) {
						JSONObject status = array.getJSONObject(i);
						ConnManager.UserStatus s = mConnManager.new UserStatus(status);
						tfm.addUserStatus(s);
					}
					retrieveDialog.dismiss();
					adapter.notifyDataSetChanged();
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				retrieveDialog.dismiss();
				Toast.makeText(TweetflowActivity.this, "Could not retrieve new Requests!", Toast.LENGTH_SHORT);
			}
		}
	}
	
	private class RefreshAction extends AbstractAction {

        public RefreshAction() {
            super(R.drawable.refresh);
        }

        @Override
        public void performAction(View view) {
//        	tfm.downloadNewTweets();
//        	adapter.notifyDataSetChanged();
        	ConnManager.TimelineSelector ss = 
        		mConnManager.new TimelineSelector(ConnManager.getInstance(getApplicationContext()).getUrls().getHomeTimelineUrlString(),
        				tfm.getNewestReceivedId(), null, null, null);
        	new GetTimelineWithProgressTask().execute(ss);
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
        menu.add(0, CLEAR_ID, 2, "Clear request list");
        menu.add(0, DELETE_ID, 3, "Delete saved requests");
        menu.add(0, RESET_ID, 4, "Reset receiver");
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
    	case CLEAR_ID:
    		
    		tfm.clearRequestList();
    		adapter.notifyDataSetChanged();
    		
    		return true;
    	case DELETE_ID:
    		
    		tfm.deleteSavedRequests();
    		adapter.notifyDataSetChanged();
    		
    		Toast.makeText(this, "All saved Requests deleted!", Toast.LENGTH_LONG)
			.show();
    		
    		return true;
    	case RESET_ID:
    		
    		tfm.resetIds();
    		
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

	       	tfm.deleteRequest((int)info.position);;
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
    
    private class LoadDataContentProviderTask extends AsyncTask<ContentProviderClient, Void, Boolean> {
   	 
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
		protected Boolean doInBackground(ContentProviderClient... params) {
			try {
				return tfm.loadRequestsFromContentProvider(params[0], params[1], params[2], params[3]);
			} catch (RemoteException e) {
				return false;
			}
		}
 
		// This is in the UI thread, so we can mess with the UI
		protected void onPostExecute(Boolean req) {
			retrieveDialog.dismiss();
			if(!req) {
				Toast.makeText(TweetflowActivity.this, "Error loading data!", Toast.LENGTH_SHORT).show();
			}
			adapter.notifyDataSetChanged();
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
    
    public static Intent createInfoIntent(Context context) {
        Intent i = new Intent(context, InfoActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }
    
    final String[] REQUEST_PROJECTION = 
		new String[] { 
			Requests._ID,
			Requests.QUALIFIER,
			Requests.ADDRESSED_USER_NAME,
			Requests.OPERATION,
			Requests.SERVICE,
			Requests.URL,
			Requests.COMPLETE_REQUEST_TEXT,
			Requests.TWEET_ID,
			Requests.SENDER_NAME,
			Requests.CREATED_AT
		};
	
	final String[] HASHTAG_PROJECTION = 
		new String[] { 
			HashTags.REQUEST_ID,
			HashTags.NAME
		};
	
	final String[] CONDITION_PROJECTION = 
		new String[] { 
			Conditions.REQUEST_ID,
			Conditions.USER_NAME,
			Conditions.VARIABLE,
			Conditions.VALUE
		};
	
	final String[] VARIABLE_PROJECTION = 
		new String[] { 
			Variables.REQUEST_ID,
			Variables.NAME,
			Variables.VALUE
		};
}
