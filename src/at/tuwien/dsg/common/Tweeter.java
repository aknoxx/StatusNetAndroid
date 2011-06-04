package at.tuwien.dsg.common;

import java.util.Date;
import java.util.List;

import twitter4j.*;
import twitter4j.auth.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class Tweeter {

	Twitter twitter;
	RequestToken requestToken;
	//Please put the values of consumerKy and consumerSecret of your app 
	public final String consumerKey = "wfRZ0ziRJOS07W9KRmAtLQ"; // "your key here";
	public final String consumerSecret = "PQzIniSepykkKpPQog2a7Se9I0mX0rLasPIgiygaPkE"; // "your secret key here";
	private final String CALLBACKURL = "T4JOAuth://main";  //Callback URL that tells the WebView to load this activity when it finishes with twitter.com. (see manifest)

	
	private boolean isLoggedIn = false;
	private static Tweeter instance = null;
	
	//public final static String consumerKey = "9a74ad0a805737218ba3da94a0236b53"; // "your key here";
	//public final static String consumerSecret = "dc3f43cba9e36cb84725f0f8d654ed6e"; 

	//private static final String HTTP_IDENTI_CA_API = "http://identi.ca/api/";
	private final String HTTP_IDENTI_CA_API = "http://twitter.com/";
	
	private final String oAuthAccessToken = "aToken";
	private final String oAuthAccessTokenSecret = "aTokenSecret";
	
	private SharedPreferences settings;
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	private Tweeter() {}
	
	public static Tweeter getInstance() {
		
		if(instance == null) {
			instance = new Tweeter();
		}
		return instance;
		
		/*settings = getPreferences(MODE_PRIVATE);
		String accessToken = settings.getString(oAuthAccessToken, null);
		String accessTokenSecret = settings.getString(oAuthAccessTokenSecret, null);*/
	}
	
	public String getAuthURL() {
			//ConfigurationBuilder cb = new ConfigurationBuilder();
			/*cb.setDebugEnabled(true);
            cb.setRestBaseURL(HTTP_IDENTI_CA_API);
            cb.setSearchBaseURL(HTTP_IDENTI_CA_API);
            cb.setOAuthAccessTokenURL("https://identi.ca/api/oauth/access_token");
            cb.setOAuthAuthorizationURL("https://identi.ca/api/oauth/authorize");
            cb.setOAuthRequestTokenURL("https://identi.ca/api/oauth/request_token");
            cb.setOAuthAuthenticationURL("https://identi.ca/api/oauth/authenticate");*/
            
            //cb.setOAuthConsumerKey(consumerKey);
            //cb.setOAuthConsumerSecret(consumerSecret);
            
            /*cb.setOAuthAccessTokenURL("http://twitter.com/oauth/access_token");
            cb.setOAuthAuthorizationURL("http://twitter.com/oauth/authorize");
            cb.setOAuthRequestTokenURL("http://twitter.com/oauth/request_token");*/
            
            //Configuration conf = cb.build();
			
            //twitter = new TwitterFactory(conf).getInstance();
			twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
			
			try {
				requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
			return requestToken.getAuthenticationURL();
	}
	
	public boolean authenticate(Uri uri, Context context) {
		try {
			String verifier = uri.getQueryParameter("oauth_verifier");
			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,
					verifier);
			String token = accessToken.getToken();
			String secret = accessToken.getTokenSecret();
			
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putString(oAuthAccessToken, token);
		    editor.putString(oAuthAccessTokenSecret, secret);
		    editor.commit();
			
			// create a tweet
			Date d = new Date(System.currentTimeMillis());
			String tweet = "#OAuth working! " + d.toLocaleString();
			
			//send the tweet
			// TODO
			Status status = null;// twitter.updateStatus(tweet);
			
			// feedback for the user...
			//Toast.makeText(this, tweet, Toast.LENGTH_LONG).show();
			
			//displayTimeLine(accessToken); //after everything, display the first tweet 
			isLoggedIn = true;
			return true;

		} catch (TwitterException ex) {
			Log.e("Main.onNewIntent", "" + ex.getMessage());
			return false;
		}
	}
	
	public static void onNewIntent(Intent intent, Context context) {
		Uri uri = intent.getData();
		if(uri != null) {
			try {
				ConnectionManager.getInstance().finalizeOAuthentication(uri);
			} catch (TwitterException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	/*
	 * Displays the timeline's first tweet in a Toast
	 */

	/*@SuppressWarnings("deprecation")
	private void displayTimeLine(AccessToken accessToken) {
		if (null != token && null != secret) {
			List<Status> statuses = null;
			try {
				twitter.setOAuthAccessToken(accessToken);
				//twitter.setOAuthAccessToken(token, secret);
				statuses = twitter.getFriendsTimeline();
				Toast.makeText(this, statuses.get(0).getText(), Toast.LENGTH_LONG)
					.show();
			} catch (Exception ex) {
				Toast.makeText(this, "Error:" + ex.getMessage(),
						Toast.LENGTH_LONG).show();
				Log.d("Main.displayTimeline",""+ex.getMessage());
			}
			
		} else {
			Toast.makeText(this, "Not Verified", Toast.LENGTH_LONG).show();
		}
	}*/
}
