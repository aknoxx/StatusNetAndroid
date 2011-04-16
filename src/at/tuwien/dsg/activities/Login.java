package at.tuwien.dsg.activities;

import java.util.Date;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import at.tuwien.dsg.R;

public class Login extends Activity {
	
	/** Called when the activity is first created. */

	Twitter twitter;
	RequestToken requestToken;
//Please put the values of consumerKy and consumerSecret of your app 
	public final String consumerKey = "GhmuvdvMcbXdWwKREtWmXA"; // "your key here";
	public final String consumerSecret = "qAS1zbwn7WgCrnaCzxODvgKReUjKB6jHBMmVlAVU"; // "your secret key here";
	private final String CALLBACKURL = "T4JOAuth://main";  //Callback URL that tells the WebView to load this activity when it finishes with twitter.com. (see manifest)

	
	//public final static String consumerKey = "9a74ad0a805737218ba3da94a0236b53"; // "your key here";
	//public final static String consumerSecret = "dc3f43cba9e36cb84725f0f8d654ed6e"; 

	//private static final String HTTP_IDENTI_CA_API = "http://identi.ca/api/";
	private final String HTTP_IDENTI_CA_API = "http://twitter.com/";
	
	private final String oAuthAccessToken = "aToken";
	private final String oAuthAccessTokenSecret = "aTokenSecret";
	
	private SharedPreferences settings;
	
	/*
	 * Calls the OAuth login method as soon as its started
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.other);
		
		settings = getPreferences(MODE_PRIVATE);
		String accessToken = settings.getString(oAuthAccessToken, null);
		String accessTokenSecret = settings.getString(oAuthAccessTokenSecret, null);
		
		/*if(accessToken.equals(null)) {
			OAuthLogin();
		}
		else {
			
			ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthAccessToken(oAuthAccessToken);
            cb.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
            cb.setOAuthConsumerKey(consumerKey);
            cb.setOAuthConsumerSecret(consumerSecret);
            
            twitter = new TwitterFactory(cb.build()).getInstance();

			/*twitter = new TwitterFactory().getOAuthAuthorizedInstance(consumerKey, consumerSecret, 
					accessToken);
			twitter.getOAuthAccessToken(token, tokenSecret, pin)
			List<Status> statuses = null;
			try {
				statuses = twitter.getFriendsTimeline();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Toast.makeText(this, statuses.get(0).getText(), Toast.LENGTH_LONG)
				.show();
		}	*/	
		
		OAuthLogin();
	}

	/*
	 * - Creates object of Twitter and sets consumerKey and consumerSecret
	 * - Prepares the URL accordingly and opens the WebView for the user to provide sign-in details
	 * - When user finishes signing-in, WebView opens your activity back
	 */
	void OAuthLogin() {
		try {
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
			Toast.makeText(this, "login", Toast.LENGTH_LONG).show();
            //twitter = new TwitterFactory(conf).getInstance();
			twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
			
			requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
			String authUrl = requestToken.getAuthenticationURL();
			startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri
					.parse(authUrl)), 5);
		} catch (TwitterException ex) {
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
			Log.e("in Main.OAuthLogin", ex.getMessage());
		}
	}
	
	@Override
	public void onDestroy() {
	    Log.i("OAUTH", "onDestroy()");
	    Toast.makeText(this, "destroy", Toast.LENGTH_LONG).show();
	    super.onDestroy();
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    
	    Toast.makeText(this, "resume", Toast.LENGTH_LONG).show();
	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Toast.makeText(this, "result", Toast.LENGTH_LONG).show();
		
		switch(requestCode){
        /*case ACTIVITY_EXPORT:
        	if (resultCode == FileManager.SUCCESS_RETURN_CODE)
        		showToast("Meals exported successfuly!");
        	else
        		showToast("Error while exporting Meals!");
	        break;
        case ACTIVITY_IMPORT:  	
        	if(resultCode == FileManager.SUCCESS_RETURN_CODE)
        		showToast("Meals imported successfuly!");
        	else
        		showToast("Error while imoprting Meals!");*/
		}
		
		Uri uri = data.getData();
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
			//Status status = twitter.updateStatus(tweet);
			
			// feedback for the user...
			Toast.makeText(this, tweet, Toast.LENGTH_LONG).show();
			
			//displayTimeLine(token, secret); //after everything, display the first tweet 

		} catch (TwitterException ex) {
			Log.e("Main.onNewIntent", "" + ex.getMessage());
		}
	}

	
	/*
	 * - Called when WebView calls your activity back.(This happens when the user has finished signing in)
	 * - Extracts the verifier from the URI received
	 * - Extracts the token and secret from the URL 
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		
		 Toast.makeText(this, "newIntent", Toast.LENGTH_LONG).show();
		
		super.onNewIntent(intent);
		Uri uri = intent.getData();
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
			//Status status = twitter.updateStatus(tweet);
			
			// feedback for the user...
			Toast.makeText(this, tweet, Toast.LENGTH_LONG).show();
			
			//displayTimeLine(token, secret); //after everything, display the first tweet 

		} catch (TwitterException ex) {
			Log.e("Main.onNewIntent", "" + ex.getMessage());
		}

	}
	
	/*
	 * Displays the timeline's first tweet in a Toast
	 */

	@SuppressWarnings("deprecation")
	void displayTimeLine(AccessToken accessToken) {
		if (null != accessToken) {
			List<Status> statuses = null;
			try {
				twitter.setOAuthAccessToken(accessToken);
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
	}

}
