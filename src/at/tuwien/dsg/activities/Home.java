package at.tuwien.dsg.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.TwitterException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.FilterManager;
import at.tuwien.dsg.common.UserManager;
import at.tuwien.dsg.entities.Filter;
import at.tuwien.dsg.views.TweetsListView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class Home extends Activity {
	
	private static UserManager userManager;
	private static final String TAG = "HOME";
	
	private static final int ACTIVITY_EXPORT = 1337;
	private static final int ACTIVITY_IMPORT = 1338;
	
	private static final int LOGOUT_ID = 0;
	private static final int EXPORT_ID = 1;
	
	private Menu menu;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		
        super.onCreate(savedInstanceState);
                
        userManager = UserManager.getInstance();
        
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
		String accessToken = settings.getString("oAuthAccessToken", null);
		String accessTokenSecret = settings.getString("oAuthAccessTokenSecret", null);
		
		if(accessToken != null && accessTokenSecret != null) {
			UserManager.getInstance().loginAuto(accessToken, accessTokenSecret);
			
			// TODO check if login was successful!!!
			
			/*Button btnSearch = (Button) findViewById(R.id.btn_search);
			btnSearch.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	
	            	EditText et_search = (EditText) findViewById(R.id.et_search);
	            	String query = et_search.getText().toString();
	            	
	            	QueryResult result = null;
	            	try {
	            		result = UserManager.getInstance().search(query);
					} catch (TwitterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					List<Tweet> tweets = result.getTweets();
					displaySearch(tweets);
	            }
	        });*/
			
			
			displayTimeline();
		}        
		else {
        	setLoginView();
        }
    }
	
	private void setLoginView() {
		setContentView(R.layout.home_login);
    	final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
        actionBar.setTitle("Home");

        final Action shareAction = new IntentAction(this, createShareIntent(), R.drawable.ic_title_share_default);
        actionBar.addAction(shareAction);
        final Action otherAction = new IntentAction(this, new Intent(this, OtherActivity.class), R.drawable.ic_title_export_default);
        actionBar.addAction(otherAction);
        
        Log.d(TAG, "home");
    	
    	
    	Button startProgress = (Button) findViewById(R.id.btn_login);
        startProgress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	//String authUrl = tweeter.getAuthURL();
                /*Intent i = new Intent(Home.this, Login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(i,5);*/
            	
            	//login();
            	
            	String authUrl = null;
				try {
					authUrl = UserManager.getInstance().getAuthenticationURL();
				} catch (TwitterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
    			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    			startActivity(i);
            	
            	/*try {
        			String authUrl = tweeter.getAuthURL();
        			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
        		} catch (Exception e) {
        			//Toast.makeText(this.e.getMessage(), Toast.LENGTH_LONG).show();
        		}*/
            }
        });
        
        
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        menu.add(0, LOGOUT_ID, 0, "Change user"/*R.string.menu_import*/);
        //menu.add(0, EXPORT_ID, 1, R.string.menu_export);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case EXPORT_ID:            
        	//exportMeals();        	
            return true;
        case LOGOUT_ID:        
        	logout();        	
	        return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
	
    private void logout() {
    	SharedPreferences settings = getPreferences(0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("oAuthAccessToken", null);
	    editor.putString("oAuthAccessTokenSecret", null);
	    editor.commit();
	    
	    setLoginView();
	    //this.menu.removeItem(LOGOUT_ID);
    }
	
	private void displayTimeline() { 
		
		setContentView(R.layout.home_tweets);
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
        actionBar.setTitle("Home");

        final Action shareAction = new IntentAction(this, createShareIntent(), R.drawable.ic_title_share_default);
        actionBar.addAction(shareAction);
        final Action otherAction = new IntentAction(this, new Intent(this, OtherActivity.class), R.drawable.ic_title_export_default);
        actionBar.addAction(otherAction);
        
        List<Status> userTimeline = null;
		try {
			userTimeline = UserManager.getInstance().getTwitter().getHomeTimeline();
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
        
        FilterManager fm = new FilterManager();
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
	
	/*private void displaySearch(List<Tweet> tweets) { 
		
		setContentView(R.layout.home_tweets);
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
        actionBar.setTitle("Search");

        final Action shareAction = new IntentAction(this, createShareIntent(), R.drawable.ic_title_share_default);
        actionBar.addAction(shareAction);
        final Action otherAction = new IntentAction(this, new Intent(this, OtherActivity.class), R.drawable.ic_title_export_default);
        actionBar.addAction(otherAction);
       
        ListView tweetsListView = (ListView) findViewById(R.id.tweet_list); 
        
        SearchAdapter adapter = new SearchAdapter(this, R.layout.list_item, (ArrayList<Tweet>) tweets);
        
        tweetsListView.setAdapter(adapter);
        
        //TweetsListView tweetsListView = new TweetsListView(this, (ArrayList<Status>) userTimeline);
        //tweetsListView.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
	}*/
	
	@Override
	protected void onNewIntent(Intent intent) {
		
		Log.d(TAG, "onNewIntent()");
		
		Toast.makeText(this, "new Intent", Toast.LENGTH_LONG).show();
		
		super.onNewIntent(intent);
		
		Uri uri = intent.getData();
		if(uri != null) {
			try {
				UserManager.getInstance().finalizeOAuthentication(uri);
				
				SharedPreferences settings = getPreferences(0);
			    SharedPreferences.Editor editor = settings.edit();
			    editor.putString("oAuthAccessToken", UserManager.getInstance().getReqToken());
			    editor.putString("oAuthAccessTokenSecret", UserManager.getInstance().getSecretToken());
			    editor.commit();
				
				displayTimeline();
				
			} catch (TwitterException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public static Intent createIntent(Context context) {
        Intent i = new Intent(context, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    private Intent createShareIntent() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Shared from the ActionBar widget.");
        return Intent.createChooser(intent, "Share");
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
                	TextView senderView = (TextView) findViewById(R.id.sender_tweet);
                	if(senderView != null) {
                		senderView.setText(status.getUser().getScreenName() + " at "
                				+ status.getCreatedAt().toLocaleString());
                	}
                	TextView msgView = (TextView) findViewById(R.id.msg_tweet);
                	if(msgView != null) {
                		msgView.setText(status.getText());
                	}
                }             
                return view;
        }
    }
	
	private class SearchAdapter extends ArrayAdapter<Tweet> {

        private ArrayList<Tweet> searchResults;

        public SearchAdapter(Context context, int textViewResourceId, ArrayList<Tweet> searchResults) {
                super(context, textViewResourceId, searchResults);
                this.searchResults = searchResults;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(R.layout.list_item, null);
                }
                Tweet tweet = searchResults.get(position);
                if (tweet != null) {
                	
                	ImageView imageView = (ImageView) findViewById(R.id.img_tweet);
                	if (imageView != null) {
                		
                		String imageUrl = tweet.getProfileImageUrl();
                		
                		try{ 
                			URL url = new URL(imageUrl);
           
                            HttpURLConnection conn =  (HttpURLConnection)url.openConnection(); 
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
                	TextView senderView = (TextView) findViewById(R.id.sender_tweet);
                	if(senderView != null) {
                		senderView.setText(tweet.getSource() + " at "
                				+ tweet.getCreatedAt().toLocaleString());
                	}
                	TextView msgView = (TextView) findViewById(R.id.msg_tweet);
                	if(msgView != null) {
                		msgView.setText(tweet.getText());
                	}
                }             
                return view;
        }
    }
}
