package at.tuwien.dsg.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.TwitterException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.UserManager;

public class SearchActivity extends ActionBarActivity {

	private static LinearLayout container;
	private static SearchAdapter adapter;
	private static ArrayList<Tweet> resultTweets;
	
	private static final String NETWORK = "network";
	private static final int LOGOUT_ID = 0;
	
	private Menu menu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle("Simple search");
		
		container = (LinearLayout) findViewById(R.id.container);
		
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		
		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.home_tweets, null);
		
		final EditText etFind = (EditText) ll.getChildAt(0);
		Button btnFind = (Button) ll.getChildAt(1);
		btnFind.setOnClickListener(new OnClickListener(){    	   	
	     	   public void onClick(View arg0) {  
	     		   String query = etFind.getText().toString();
	     		   search(query);
	     	    }
	     	});
		
		
		container.addView(ll);
		ListView lv = (ListView) inflater.inflate(R.layout.tweet_list, null);
		container.addView(lv);
		
		ListView tweetsListView = (ListView) findViewById(R.id.tweet_list);
		
		resultTweets = new ArrayList<Tweet>();
        adapter = new SearchAdapter(this, R.layout.list_item, resultTweets);
        
        tweetsListView.setAdapter(adapter);
	}
	
	private void search(String query) {
		QueryResult queryResult = null;
		Query q = new Query(query);
		try {
			queryResult = UserManager.getInstance().getTwitter().search(q);			
			resultTweets.clear();
			resultTweets.addAll((ArrayList<Tweet>) queryResult.getTweets());
			adapter.notifyDataSetChanged();
		} catch (TwitterException e) {
			Toast.makeText(getApplicationContext(), "Invalid search string!", 
					Toast.LENGTH_LONG).show();
			
			// TODO what happens here with identi.ca?
			//e.printStackTrace();
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
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        menu.add(0, LOGOUT_ID, 0, "Logout");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case LOGOUT_ID:        
        	logout();        	
	        return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
	
    private void logout() {
    	UserManager.getInstance().logout();
    	
    	SharedPreferences settings = getPreferences(0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(NETWORK, "");
	    editor.commit();
	    
	    System.setProperty("oauth.accessToken", ""); 
        System.setProperty("oauth.accessTokenSecret", "");
        
        UserManager.destroyInstance();
	    
        startActivity(new Intent(SearchActivity.this, HomeActivity.class));
    }
}
