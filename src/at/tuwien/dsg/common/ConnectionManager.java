package at.tuwien.dsg.common;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import at.tuwien.dsg.R;
import at.tuwien.dsg.activities.HomeActivity;
import at.tuwien.dsg.entities.NetworkConfig;
import at.tuwien.dsg.util.NetworkConfigParser;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class ConnectionManager {
	
	private static Context ctx;
	
	private static ConnectionManager instance;
	private static String consumerKey; 		
	private static String consumerSecret; 

	private static final String TAG = "ConnectionManager";
		
	private final String CALLBACKURL = "T4JOAuth://main";
	private Twitter twitter;
	private RequestToken requestToken;
	private AccessToken accessToken;
	
	private boolean loggedIn = false;
	
	private String oAuthAccessToken;
	private String oAuthAccessTokenSecret;
	
	private static final String MY_PREFS = "myPrefs";
	private static final String NETWORK = "network";
	private static final String OAUTH_TOKEN = "oAuthToken";
	private static final String OAUTH_TOKEN_SECRET = "oAuthTokenSecret";
	
	private String currentNetwork;
	
	private ConnectionManager() {};

	public static ConnectionManager getInstance() {
		System.out.println("Test");
		if (instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}
	
	public static void destroyInstance() {
		instance = null;
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
        System.setProperty("twitter4j.oauth.authenticationURL", config.getAuthenticationURL()); 
		
	}
	
	public String getCurrentNetwork() {
		return currentNetwork;
	}
	
	public String getAuthenticationURL() throws TwitterException {
		
		Log.i(TAG, "login");

		twitter = new TwitterFactory().getInstance();		
		
		requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
		
		
		String authorizationUrl = requestToken.getAuthorizationURL();
		
		/*
		 * ONLY working with twitter.com...:
		 */
		//String authorizationUrl = requestToken.getAuthenticationURL();
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

		//System.setProperty("twitter4j.oauth.accessToken", oAuthAccessToken); 
        //System.setProperty("twitter4j.oauth.accessTokenSecret", oAuthAccessTokenSecret);
		
		loggedIn = true;
	}
	
	public PagableResponseList<UserList> getUserLists(String listOwnerScreenName) 
			throws TwitterException {		
		// Provide a value of -1 to begin paging
		return twitter.getUserLists(listOwnerScreenName, new Long(-1));
	}
	
	public ResponseList<Status> getUserListStatuses(String listOwnerScreenName, int id, Paging paging) 
			throws TwitterException {
		return twitter.getUserListStatuses(listOwnerScreenName, id, paging);
	}
	
	public ResponseList<Status> getHomeTimeline(Paging paging) throws TwitterException {
		return twitter.getHomeTimeline(paging);
	}
	
	public void setContext(Context context) {
		this.ctx = context;
	}
	
	public void autoLogin() {   
		
		NetworkConfigParser parser = new NetworkConfigParser();
		final List<NetworkConfig> networkConfigs = parser.parse(ctx.getResources().getXml(R.xml.network_config));
		
		SharedPreferences settings = ctx.getSharedPreferences(MY_PREFS, 0);
		String network = settings.getString(NETWORK, "");
		
		setNetworkConfig(networkConfigs, network);
		String accessToken = settings.getString(OAUTH_TOKEN, "");
        String accessTokenSecret = settings.getString(OAUTH_TOKEN_SECRET, "");
        
        AccessToken at = new AccessToken(accessToken, accessTokenSecret);
        
        // potentionally returns a cached instance
        twitter = new TwitterFactory().getInstance(at);
        // this would create a new instance: twitter.setOAuthAccessToken(at);
        try {
			twitter.verifyCredentials();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
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
