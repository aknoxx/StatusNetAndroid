package at.tuwien.dsg.common;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class UserManager {
	private static UserManager instance;
	private final static String consumerKey = "wfRZ0ziRJOS07W9KRmAtLQ"; 		
	private final static String consumerSecret = "PQzIniSepykkKpPQog2a7Se9I0mX0rLasPIgiygaPkE"; 

	private static final String TAG = "UserManager";
		
	private final String CALLBACKURL = "T4JOAuth://main";
	private Twitter twitter;
	private RequestToken requestToken;
	private AccessToken accessToken;
	
	private boolean loggedIn = false;
	
	private String oAuthAccessToken;
	private String oAuthAccessTokenSecret;
	
	private UserManager() {};

	public static UserManager getInstance() {
		System.out.println("Test");
		if (instance == null) {
			instance = new UserManager();
		}
		return instance;
	}
	
	public String getAuthenticationURL() throws TwitterException {

		Log.i(TAG, "login");
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(UserManager.getInstance().getConsumerkey(), UserManager.getInstance().getConsumersecret());
		requestToken = twitter.getOAuthRequestToken(UserManager.getInstance().getCALLBACKURL());
		String authUrl = requestToken.getAuthenticationURL();
		return authUrl;
	}
	
	public void finalizeOAuthentication(Uri uri) throws TwitterException {
		Log.d(TAG, "loginIntent");
		
		String verifier = uri.getQueryParameter("oauth_verifier");
		accessToken = UserManager.getInstance().getTwitter().getOAuthAccessToken(UserManager.getInstance().getRequestToken(),verifier);
		oAuthAccessToken = accessToken.getToken();
		oAuthAccessTokenSecret = accessToken.getTokenSecret();		
		twitter.setOAuthAccessToken(accessToken);
		loggedIn = true;
	}
	
	public void loginAuto(String requestToken, String secretToken) {
		Log.d(TAG, "loginAuto");
		twitter = new TwitterFactory().getInstance();
		accessToken = new AccessToken(requestToken, secretToken);
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		twitter.setOAuthAccessToken(accessToken);
		loggedIn = true;
	}

	public void sendTweeterMessage(String message) throws TwitterException {
		twitter.updateStatus(message);
	}
	
	public String getConsumerkey() {
		return consumerKey;
	}

	public String getConsumersecret() {
		return consumerSecret;
	}

	public String getCALLBACKURL() {
		return CALLBACKURL;
	}

	public Twitter getTwitter() {
		return twitter;
	}
	
	public QueryResult search(String query) throws TwitterException {
		Query q = new Query(query);
		return twitter.search(q);
	}

	public void setTwitter(Twitter twitter) {
		this.twitter = twitter;
	}

	public RequestToken getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(RequestToken requestToken) {
		this.requestToken = requestToken;
	}

	public AccessToken getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(AccessToken accessToken) {
		this.accessToken = accessToken;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getReqToken() {
		return oAuthAccessToken;
	}

	public void setReqToken(String reqToken) {
		this.oAuthAccessToken = reqToken;
	}

	public String getSecretToken() {
		return oAuthAccessTokenSecret;
	}

	public void setSecretToken(String secretToken) {
		this.oAuthAccessTokenSecret = secretToken;
	}
	
	
	
}
