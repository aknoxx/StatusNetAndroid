package at.tuwien.dsg.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.TweetFlowManager;
import at.tuwien.dsg.common.ConnectionManager;
import at.tuwien.dsg.entities.Filter;
import at.tuwien.dsg.entities.NetworkConfig;
import at.tuwien.dsg.util.NetworkConfigParser;

public class HomeActivity extends ActionBarActivity {
	
	private static ConnectionManager userManager;
	private static final String TAG = "HOME";
	
	private static final String MY_PREFS = "myPrefs";
	private static final String NETWORK = "network";
	private static final String OAUTH_TOKEN = "oAuthToken";
	private static final String OAUTH_TOKEN_SECRET = "oAuthTokenSecret";
	
	private static final int ACTIVITY_EXPORT = 1337;
	private static final int ACTIVITY_IMPORT = 1338;
	
	private static final int LOGOUT_ID = 0;
	private static final int EXPORT_ID = 1;
	
	private static LinearLayout container;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");		
        super.onCreate(savedInstanceState);
        
        setTitle("Login");
                
        userManager = ConnectionManager.getInstance();
        userManager.setContext(getApplicationContext());
        container = (LinearLayout) findViewById(R.id.container);
        
        SharedPreferences settings = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        String network = settings.getString(NETWORK, "");
        String accessToken = settings.getString(OAUTH_TOKEN, "");
        String accessTokenSecret = settings.getString(OAUTH_TOKEN_SECRET, "");
        //String accessToken = System.getProperty("twitter4j.oauth.accessToken", ""); 
        //String accessTokenSecret = System.getProperty("twitter4j.oauth.accessTokenSecret", "");
		
		if(network != "" && accessToken != "" && accessTokenSecret != "") {
			
			ConnectionManager.getInstance().autoLogin();
			
			// TODO check if login was successful!!!
			
			/*
	         * Redirect to SearchActivity
	         */
	        //startActivity(new Intent(HomeActivity.this, SearchActivity.class));
			startActivity(new Intent(HomeActivity.this, TestActivity.class));
		}        
		else {			
        	setLoginView();
        }
    }
	
	public boolean isOnline() {
		 ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		 return cm.getActiveNetworkInfo().isConnected();
	}
	
	private void setLoginView() {
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		
		Button btn = (Button) inflater.inflate(R.layout.login, null);
	    container.addView(btn);        
        
        Log.d(TAG, "home");
    	    	        
    	//Button btnLogin = (Button) findViewById(R.id.btn_login);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	if(!isOnline()) {
            		Toast.makeText(getApplicationContext(), "No internet connection available!", Toast.LENGTH_LONG).show();
            	}
            	else {               		
            		NetworkConfigParser parser = new NetworkConfigParser();
            		final List<NetworkConfig> networkConfigs = parser.parse(HomeActivity.this.getResources().getXml(R.xml.network_config));

            		CharSequence[] networks = new CharSequence[networkConfigs.size()];
            		
            		for (int j = 0; j < networks.length; j++) {
            			networks[j] = networkConfigs.get(j).getName();
            		}            		
            		final CharSequence[] items = networks;
            		
            		AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            		builder.setTitle("Select a network");
            		builder.setItems(items, new DialogInterface.OnClickListener() {
            		    public void onClick(DialogInterface dialog, int itemIndex) {
            		    	
            		        ConnectionManager.getInstance().setNetworkConfig(networkConfigs, items[itemIndex].toString());
            		        
            		        /*
            		         * Start login procedure after network selection
            		         */
            		        String authUrl = null;
            				try {
            					authUrl = ConnectionManager.getInstance().getAuthenticationURL();
            				} catch (TwitterException e) {
            					// TODO Auto-generated catch block
            					e.printStackTrace();
            				}
            				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
            				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            				startActivity(intent);
            		    }
            		});
            		
            		AlertDialog alert = builder.create();
            		alert.show();         		
            	}
            }
        });		
	}
	
	/*
	 * Finish login procedure after callback from browser
	 * (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent()");	
		
		Uri uri = intent.getData();
		if(uri != null) {
			try {
				ConnectionManager.getInstance().finalizeOAuthentication(uri);
				
				SharedPreferences settings = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
			    SharedPreferences.Editor editor = settings.edit();
			    editor.putString(NETWORK, ConnectionManager.getInstance().getCurrentNetwork());
			    editor.putString(OAUTH_TOKEN, ConnectionManager.getInstance().getOAuthToken());
			    editor.putString(OAUTH_TOKEN_SECRET, ConnectionManager.getInstance().getOAuthTokenSecret());
			    editor.commit();
			    
			    //System.setProperty("oauth.accessToken", ConnectionManager.getInstance().getOAuthToken()); 
		        //System.setProperty("oauth.accessTokenSecret", ConnectionManager.getInstance().getOAuthTokenSecret());
				
		        /*
		         * Redirect to SearchActivity
		         */
		        //startActivity(new Intent(HomeActivity.this, SearchActivity.class));
			    startActivity(new Intent(HomeActivity.this, TestActivity.class));
		        
			} catch (TwitterException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	
	
	private void displayTimeline() { 
		
		//setContentView(R.layout.home_tweets);
		
		//container.removeViewAt(1);
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		
		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.home_tweets, null);
		container.addView(ll);
		ListView lv = (ListView) inflater.inflate(R.layout.tweet_list, null);
		container.addView(lv);
		
		/*
		try {
			ConnectionManager.getInstance().getTwitter().updateStatus("hey it works!");
			Toast.makeText(this, "message sent", Toast.LENGTH_LONG).show();
		} catch (TwitterException e1) {
			Toast.makeText(this, "error sending message :(", Toast.LENGTH_LONG).show();
			e1.printStackTrace();
		}*/
		
        List<Status> userTimeline = null;
		try {
			
			userTimeline = ConnectionManager.getInstance().getTwitter().getHomeTimeline();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        List<String> msgs = new ArrayList<String>();
        
        // \\d	A digit: [0-9]
        // \\w	A word character: [a-zA-Z_0-9]
        // \\s	A whitespace character: [ \t\n\x0B\f\r]
        
        // +	One or more.
        // \\.+	Anything
        // *	Zero or more.
        
        String regexLG = "LG\\s+\\w+\\.\\w+\\?date=\\d{2}\\.\\d{2}\\.\\d{4}&duration=\\d{4}\\s+#\\w+";
        
        Filter lg_Filter = new Filter("LG",
        		regexLG);
        
        TweetFlowManager fm = TweetFlowManager.getInstance(this);
        fm.addFilter(lg_Filter);
        fm.setFilter(lg_Filter);
        
        // SR proofread.Blog http://www.ikangai.com #blog 
        //String regexSR = "SR\\s+\\w+\\.\\w+\\?date=\\d{2}\\.\\d{2}\\.\\d{4}&duration=\\d{4}\\s+#\\w+";
        
        String regex2 = "#.{1,}";
        
        /*for (Status status : userTimeline) {
        	if(fm.match(status.getText())) {
        		msgs.add("PATTERN MATCH " + status.getText());
        	}			
        	msgs.add(status.getText());
		}*/
        
        ListView tweetsListView = (ListView) findViewById(R.id.tweet_list); 
        
        TimelineAdapter adapter = new TimelineAdapter(this, R.layout.list_item, (ArrayList<Status>) userTimeline);
        
        tweetsListView.setAdapter(adapter);
        
        //TweetsListView tweetsListView = new TweetsListView(this, (ArrayList<Status>) userTimeline);
        //tweetsListView.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
	}
	
	
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }
       
    @Override
	protected void onPause() {
    	Log.d(TAG, "onPause()");
		super.onPause();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart()");
		super.onStart();
	}
	
	private class TimelineAdapter extends ArrayAdapter<Status> {

        private ArrayList<Status> timeline;

        public TimelineAdapter(Context context, int textViewResourceId, ArrayList<Status> timeline) {
                super(context, textViewResourceId, timeline);
                this.timeline = timeline;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(R.layout.list_item, null);
                }
                Status status = timeline.get(position);
                if (status != null) {
                	/*
                	ImageView imageView = (ImageView) findViewById(R.id.img_tweet);
                	if (imageView != null) {
                		
                		URL imageUrl = status.getUser().getProfileImageURL();
                		
                		try{ 
                            HttpURLConnection conn =  (HttpURLConnection)imageUrl.openConnection(); 
                            conn.setDoInput(true); 
                            conn.connect(); 
                            int length = conn.getContentLength(); 
                            int[] bitmapData =new int[length]; 
                            byte[] bitmapData2 =new byte[length]; 
                            InputStream is = conn.getInputStream(); 
                            Bitmap bmp = BitmapFactory.decodeStream(is); 
                            imageView.setImageBitmap(bmp); 
                            } catch (IOException e) 
                            { 
                                    //e.printStackTrace(); 
                            }
                	}
                	*/
                	
                	TextView senderView = (TextView) findViewById(R.id.request_info);
                	if(senderView != null) {
                		senderView.setText(status.getUser().getScreenName() + " at "
                				+ status.getCreatedAt().toLocaleString());
                	}
                	TextView msgView = (TextView) findViewById(R.id.request_content);
                	if(msgView != null) {
                		msgView.setText(status.getText());
                	}
                }             
                return view;
        }
    }
}
