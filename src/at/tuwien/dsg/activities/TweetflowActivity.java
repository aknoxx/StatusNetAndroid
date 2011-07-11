package at.tuwien.dsg.activities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.ConnectionManager;
import at.tuwien.dsg.common.Request.Conditions;
import at.tuwien.dsg.common.Request.HashTags;
import at.tuwien.dsg.common.Request.Requests;
import at.tuwien.dsg.common.Request.Variables;
import at.tuwien.dsg.common.TweetFlowManager;
import at.tuwien.dsg.entities.DisplayData;
import at.tuwien.dsg.entities.Network;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class TweetflowActivity extends MyListActivity {
	
	private static final int SAVE_ID = Menu.FIRST;
	private static final int RESET_ID = Menu.FIRST + 3;	
	private static final int CLEAR_ID = Menu.FIRST + 4;
	private static final int CONTEXT_DELETE_REQUEST_ID = Menu.FIRST + 5;
		
	private static final int FILTER_DIALOG = 4;	

	private MyArrayAdapter adapter;
    
    private ContentProviderClient requestsProvider;
    private ContentProviderClient hashTagsProvider;
    private ContentProviderClient conditionsProvider;
    private ContentProviderClient variablesProvider;
	
	private ActionBar actionBar;
	private ConnectionManager mConnectionManager;
	
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mSettingsEditor;
	
	private boolean loggedIn = false;
	
	public static final String TF_MANAGER_FILE = "TFManagerFile";
	private static final String CONNECTION_MANAGER_FILE = "ConnectionManagerFile";
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle icicle) {
		this.TAG = "TweetflowActivity";
		super.onCreate(icicle);
		
		setContentView(R.layout.request_view);
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		final Action requestsIntentAction = new IntentAction(this, new Intent(this, TweetflowActivity.class), R.drawable.ic_title_home_default);
		actionBar.setHomeAction(requestsIntentAction);		
		actionBar.setTitle("Home");
		
		final Action infoIntentAction = new IntentAction(this, new Intent(this, InfoActivity.class), R.drawable.info);
		final Action viewSavedRequestsIntentAction = new IntentAction(this, new Intent(this, SavedRequestsActivity.class), R.drawable.lock);
		
		actionBar.addAction(new RefreshAction());
		actionBar.addAction(new FilterAction());
		actionBar.addAction(viewSavedRequestsIntentAction);
		actionBar.addAction(infoIntentAction);
		
		ListView listView = getListView();
		
		// try to restore data from last session
		// create new objects otherwise
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
		TweetFlowManager.getInstance(this, dd);
		
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
			ConnectionManager.restartConnectionManagerWithNewNetwork(n);
		}		
		mConnectionManager = ConnectionManager.getInstance(this);
		
		// Get Content Providers
		requestsProvider = getContentResolver().acquireContentProviderClient(Requests.CONTENT_URI);
		hashTagsProvider = getContentResolver().acquireContentProviderClient(HashTags.CONTENT_URI);
		conditionsProvider = getContentResolver().acquireContentProviderClient(Conditions.CONTENT_URI);
		variablesProvider = getContentResolver().acquireContentProviderClient(Variables.CONTENT_URI);
		
		adapter = new MyArrayAdapter(this, TweetFlowManager.getInstance(this, null).getFilteredRequests());
		this.setListAdapter(adapter);
		
		registerForContextMenu(listView);
		
		mSettings = getSharedPreferences(OAuthActivity.PREFS, Context.MODE_PRIVATE);
		mSettingsEditor = mSettings.edit();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		loggedIn = mSettings.getBoolean(ConnectionManager.LOGGEDIN, false);
		if(!loggedIn) {
			TweetFlowManager.getInstance(this, null).clearRequestList();
			adapter.notifyDataSetChanged();
			
			if(mConnectionManager.getKeysAvailable()) {
				new GetCredentialsTask().execute();
			}
			else {
				// sets the Network
				startActivity(new Intent(this, LoginActivity.class));
			}
		}
		else {
//			Long newestId = tfm.getNewestReceivedId();
//			if(newestId == 0) {
//				newestId = null;
//			}
//			
//			ConnectionManager.TimelineSelector ss = 
//				mConnectionManager.new TimelineSelector(ConnectionManager.getInstance(getApplicationContext()).getUrls().getHomeTimelineUrlString(),
//						newestId, null, null, null);
			
			new GetTimelineWithProgressTask().execute();
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
			out.writeObject(TweetFlowManager.getInstance(this, null).getDd());
			out.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		try {
			fos = openFileOutput(CONNECTION_MANAGER_FILE, Context.MODE_PRIVATE);
			out = new ObjectOutputStream(fos);
			out.writeObject(mConnectionManager.getCurrentNetwork());
			out.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}	
	}
	
	public void onFinish() {
		super.onFinish();
		
		mConnectionManager.shutdownConnectionManager();
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
			return mConnectionManager.getCredentials();
		}
		
		// This is in the UI thread, so we can mess with the UI
		protected void onPostExecute(JSONObject jso) {
			authDialog.dismiss();
			if(jso != null) {
				// now we are definitly "logged in"
				mSettingsEditor.putBoolean(ConnectionManager.LOGGEDIN, true);
				mSettingsEditor.commit();
				
				new GetTimelineWithProgressTask().execute();
			}
		}
	}
	
	private class GetTimelineWithProgressTask extends AsyncTask<Void, Void, Boolean> {
		
		ProgressDialog retrieveDialog;
		ConnectionManager.TimelineSelector ts;
		Long newestId;
		 
		@Override
		protected void onPreExecute() {
			retrieveDialog = ProgressDialog.show(TweetflowActivity.this, 
				getText(R.string.request_progress_title), 
				getText(R.string.request_progress_text), 
				true,	// indeterminate duration
				false); // not cancel-able
			
			newestId = TweetFlowManager.getInstance(TweetflowActivity.this, null).getNewestReceivedId();
			if(newestId == 0) {
				newestId = null;
			}
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			JSONArray array;
			if(newestId == null) {
				for (int i = 0; i < 5; i++) {
					ts = ConnectionManager.getInstance(getApplicationContext()).new TimelineSelector(
							ConnectionManager.getInstance(getApplicationContext()).getUrls()
							.getHomeTimelineUrlString(),
								newestId, null, null, i+1);
					
					array =  ConnectionManager.getInstance(getApplicationContext())
								.getTimeline(ts);
					
					if(array != null) {
						try {
							for(int j = array.length()-1; j >=0 ; j--) {
								JSONObject status = array.getJSONObject(j);
								ConnectionManager.UserStatus s =  ConnectionManager.getInstance(getApplicationContext())
											.new UserStatus(status);
								TweetFlowManager.getInstance(TweetflowActivity.this, null).addUserStatus(s);
							}						
							
						} catch (JSONException e) {
							return false;
						}
					}
				}
			}
			else {
				ts =  ConnectionManager.getInstance(getApplicationContext()).new TimelineSelector(
						ConnectionManager.getInstance(getApplicationContext()).getUrls()
						.getHomeTimelineUrlString(),
							newestId, null, null, null);
				
				array =  ConnectionManager.getInstance(getApplicationContext())
						.getTimeline(ts);
				
				if(array != null) {
					try {
						for(int j = array.length()-1; j >=0 ; j--) {
							JSONObject status = array.getJSONObject(j);
							ConnectionManager.UserStatus s = ConnectionManager
								.getInstance(getApplicationContext()).new UserStatus(status);
							TweetFlowManager.getInstance(TweetflowActivity.this, null).addUserStatus(s);
						}						
						
					} catch (JSONException e) {
						return false;
					}
				}
			}
			return true;
		}
		
		// This is in the UI thread, so we can mess with the UI
		protected void onPostExecute(Boolean result) {
			
			if(result) {
				retrieveDialog.dismiss();
				adapter.notifyDataSetChanged();
			}
			else {
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
        	new GetTimelineWithProgressTask().execute();
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
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SAVE_ID, 0, "Save requests");
        menu.add(0, CLEAR_ID, 2, "Clear request list");
        menu.add(0, RESET_ID, 4, "Reset receiver");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case SAVE_ID:        
        	
        	if(TweetFlowManager.getInstance(this, null).saveRequests()) {
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
    		
    		TweetFlowManager.getInstance(this, null).clearRequestList();
    		adapter.notifyDataSetChanged();
    		
    		return true;
    	case RESET_ID:
    		
    		TweetFlowManager.getInstance(this, null).resetIds();
    		
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

    		TweetFlowManager.getInstance(this, null).deleteRequest((int)info.position);;
	       	adapter.notifyDataSetChanged();
	       	
	       	Toast.makeText(this, "Request deleted successfully!", Toast.LENGTH_LONG)
			.show();

	        return true;
		}
		return super.onContextItemSelected(item);
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

                case FILTER_DIALOG:

                	Iterator<Entry<CharSequence, Boolean>> iter = 
                		TweetFlowManager.getInstance(this, null).getDisplayFilter().entrySet().iterator();
                    
                	CharSequence[] types = new CharSequence[TweetFlowManager.getInstance(this, null).getDisplayFilter().size()];
                	boolean[] checkedItems = new boolean[TweetFlowManager.getInstance(this, null).getDisplayFilter().size()];
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
                                	   TweetFlowManager.getInstance(TweetflowActivity.this, null).getDisplayFilter().put(fTypes[j], 
                                			   new Boolean(fCheckedItems[j]));
                                   }
                                   TweetFlowManager.getInstance(TweetflowActivity.this, null).loadFilteredRequests();
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
			Requests.OPERATION_EXECUTION_STATUS,
			Requests.SERVICE,
			Requests.URL,
			Requests.COMPLETE_REQUEST_TEXT,
			Requests.TWEET_ID,
			Requests.SENDER_NAME,
			Requests.CREATED_AT,
			Requests.IS_CLOSED_SEQUENCE,
			Requests.DEPENDENT_ON_TWEETID,
			Requests.ORDERING,
			Requests.DEPENDENT_ON_NUMBER
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
