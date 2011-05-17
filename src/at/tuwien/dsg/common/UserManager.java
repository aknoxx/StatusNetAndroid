package at.tuwien.dsg.common;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import at.tuwien.dsg.entities.NetworkConfig;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class UserManager {
	
	private static Context ctx;
	
	private static UserManager instance;
	private static String consumerKey; 		
	private static String consumerSecret; 

	private static final String TAG = "UserManager";
		
	private final String CALLBACKURL = "T4JOAuth://main";
	private Twitter twitter;
	private RequestToken requestToken;
	private AccessToken accessToken;
	
	private boolean loggedIn = false;
	
	private String oAuthAccessToken;
	private String oAuthAccessTokenSecret;
	
	private String currentNetwork;
	
	private UserManager() {};

	public static UserManager getInstance() {
		System.out.println("Test");
		if (instance == null) {
			instance = new UserManager();
		}
		return instance;
	}
	
	public void setNetworkConfig(List<NetworkConfig> networkConfigs, String networkType) {
		
		NetworkConfig config = null;
		for (NetworkConfig c : networkConfigs) {
			if(c.getName().equals(networkType)) {
				config = c;
				break;
			}
		}
		
		currentNetwork = networkType;
		
		System.setProperty("twitter4j.oauth.consumerKey", config.getConsumerKey()); 
        System.setProperty("twitter4j.oauth.consumerSecret", config.getConsumerSecret()); 
        System.setProperty("twitter4j.oauth.accessTokenURL", config.getAccessTokenURL()); 
        System.setProperty("twitter4j.oauth.authorizationURL", config.getAuthorizationURL()); 
        System.setProperty("twitter4j.oauth.requestTokenURL", config.getRequestTokenURL()); 
        System.setProperty("twitter4j.restBaseURL", config.getRestBaseURL()); 
        System.setProperty("twitter4j.searchBaseURL", config.getSearchBaseURL()); 
		
	}
	
	public String getCurrentNetwork() {
		return currentNetwork;
	}
	
	public String getAuthenticationURL() throws TwitterException {
		
		Log.i(TAG, "login");

		twitter = new TwitterFactory().getInstance();		
		
		requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
		String authorizationUrl = requestToken.getAuthorizationURL();
		return authorizationUrl;
		
		/*		
        //cb.setSiteStreamBaseURL("http://sitestream.identi.ca/2b/");
       // cb.setStreamBaseURL("http://stream.identi.ca/1/");
        //cb.setUserStreamBaseURL("http://userstream.identi.ca/2/");		
        // siteStreamBaseURL http://sitestream.twitter.com/2b/
		// streamBaseURL http://stream.twitter.com/1/
		// userStreamBaseURL https://userstream.twitter.com/2/
*/
	}
	
	public void logout() {
		twitter = null;
	}
	
	public void finalizeOAuthentication(Uri uri) throws TwitterException {
		Log.d(TAG, "loginIntent");
		
		String verifier = uri.getQueryParameter("oauth_verifier");
		accessToken = twitter.getOAuthAccessToken(requestToken,verifier);
		oAuthAccessToken = accessToken.getToken();
		oAuthAccessTokenSecret = accessToken.getTokenSecret();		
		twitter.setOAuthAccessToken(accessToken);

		System.setProperty("oauth.accessToken", oAuthAccessToken); 
        System.setProperty("oauth.accessTokenSecret", oAuthAccessTokenSecret);
		
		loggedIn = true;
	}
	
	public void setContext(Context context) {
		this.ctx = context;
	}
	
	public void autoLogin() {   
        
        twitter = new TwitterFactory().getInstance();
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

	public String getOAuthToken() {
		return oAuthAccessToken;
	}

	public void setReqToken(String reqToken) {
		this.oAuthAccessToken = reqToken;
	}

	public String getOAuthTokenSecret() {
		return oAuthAccessTokenSecret;
	}

	public void setSecretToken(String secretToken) {
		this.oAuthAccessTokenSecret = secretToken;
	}	
}
