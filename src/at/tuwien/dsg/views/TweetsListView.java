package at.tuwien.dsg.views;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import twitter4j.Status;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import at.tuwien.dsg.R;

public class TweetsListView extends ListView {

	private Context ctx;
	//private TimelineAdapter adapter;
	
	public TweetsListView(Context context, ArrayList<Status> userTimeline) {
		super(context);
		
		this.ctx = context;
		
		//this.getl = (ListView) findViewById(R.layout.home_tweets);
		
		//setContentView(R.layout.home_tweets);
		findViewById(R.id.tweet_list);
		
		//this.adapter = new TimelineAdapter(ctx, R.layout.list_item, userTimeline);
		//this.setAdapter(adapter); 
	}
	
	/*public void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
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
                    LayoutInflater vi = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    }*/

}
