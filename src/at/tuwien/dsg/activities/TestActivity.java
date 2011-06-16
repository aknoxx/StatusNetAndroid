package at.tuwien.dsg.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import at.tuwien.dsg.R;
import at.tuwien.dsg.common.TweetFlowManager;
import at.tuwien.dsg.common.ConnectionManager;
import at.tuwien.dsg.entities.Filter;
import at.tuwien.dsg.entities.Request;

public class TestActivity extends ActionBarActivity {

	private static LinearLayout container;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle("Testing");
		
		container = (LinearLayout) findViewById(R.id.container);
		
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		
		PagableResponseList<UserList> lists = null;
		try {
			lists = ConnectionManager.getInstance().getUserLists("ikangai");
			//lists = ConnectionManager.getInstance().getUserLists("aknoxx");
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String regexLG = "LG\\s+\\w+\\.\\w+\\?date=\\d{2}\\.\\d{2}\\.\\d{4}&duration=\\d{4}\\s+#\\w+";
        
        Filter lg_Filter = new Filter("LG",
        		regexLG);
        
        TweetFlowManager fm = TweetFlowManager.getInstance(this, null);
//        fm.addFilter(lg_Filter);
//        fm.setFilter(lg_Filter);
        
		
		if(lists != null) {
			TextView tv = new TextView(this);
		    tv.setText("Testresult:" + lists.size());
			container.addView(tv);
			
			for (int i = 0; i < lists.size(); i++) {
				tv = new TextView(this);
			    tv.setText(lists.get(i).getFullName());
				container.addView(tv);
				tv = new TextView(this);
			    tv.setText(lists.get(i).getName());
				container.addView(tv);
			}
			
			UserList list = lists.get(0);
			// requesting page 1, number of elements per page is 10
		    Paging paging = new Paging(1, 40);

		    ResponseList<Status> statuses = null;
			try {
				//statuses = ConnectionManager.getInstance().getUserListStatuses("ikangai", list.getId(), paging);
				statuses = ConnectionManager.getInstance().getHomeTimeline(paging);
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ArrayList<Status> userTimeline = new ArrayList<Status>();
			int requestsFound = 0;
			for (Status status : statuses) {
//				if(fm.match(status.getText())) {
//					userTimeline.add(status);
//					requestsFound++;
//					
//					Request req = null; //fm.extractRequest(status);
//					tv = new TextView(this);
//				    tv.setText(req.getQualifier() + " " + req.getCreatedAt() + " " + //req.getDuration() +
//				    		" " + req.getOperation() + req.getService() + req.getRequester() + " " + 
//				    		req.getHashTags().get(0));
//					container.addView(tv);
//				}
			}		
			
			
			tv = new TextView(this);
		    tv.setText("Requests found: " + requestsFound);
			container.addView(tv);
			
			LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.home_tweets, null);
	
			container.addView(ll);
			ListView lv = (ListView) inflater.inflate(R.layout.tweet_list, null);
			container.addView(lv);
			
			ListView tweetsListView = (ListView) findViewById(R.id.tweet_list);
			
			//TimelineAdapter adapter = new TimelineAdapter(this, R.layout.list_item, userTimeline);
	        
	        //tweetsListView.setAdapter(adapter);
		}
		else {
			TextView tv = new TextView(this);
		    tv.setText("Testresult: empty");
			container.addView(tv);
		}
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
