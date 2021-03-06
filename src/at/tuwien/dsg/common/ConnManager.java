package at.tuwien.dsg.common;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;
import at.tuwien.dsg.R;
import at.tuwien.dsg.activities.Keys;
import at.tuwien.dsg.activities.OAuthActivity;


public class ConnManager {
	public static final String TAG = "ConnManager";
	
//	public static final String VERIFY_URL_STRING = "http://twitter.com/account/verify_credentials.json";
//	public static final String PUBLIC_TIMELINE_URL_STRING = "http://twitter.com/statuses/public_timeline.json";
//	public static final String USER_TIMELINE_URL_STRING = "http://twitter.com/statuses/user_timeline.json";
//	public static final String HOME_TIMELINE_URL_STRING = "http://api.twitter.com/1/statuses/home_timeline.json";	
//	public static final String FRIENDS_TIMELINE_URL_STRING = "http://api.twitter.com/1/statuses/friends_timeline.json";	
//	public static final String STATUSES_URL_STRING = "http://twitter.com/statuses/update.json";	

	/*
	public static final String VERIFY_URL_STRING = "http://identi.ca/api/account/verify_credentials.json";
	public static final String PUBLIC_TIMELINE_URL_STRING = "http://identi.ca/api/statuses/public_timeline.json";
	public static final String USER_TIMELINE_URL_STRING = "http://identi.ca/api/statuses/user_timeline.json";
	public static final String HOME_TIMELINE_URL_STRING = "http://identi.ca/api/statuses/home_timeline.json";	
	public static final String FRIENDS_TIMELINE_URL_STRING = "http://identi.ca/api/statuses/friends_timeline.json";	
	public static final String STATUSES_URL_STRING = "http://identi.ca/api/statuses/update.json";	
	*/
	
	public static final String VERIFY_URL_STRING = "http://192.168.0.10/statusnet/index.php/api/account/verify_credentials.json";
	public static final String PUBLIC_TIMELINE_URL_STRING = "http://192.168.0.10/statusnet/index.php/api/statuses/public_timeline.json";
	public static final String USER_TIMELINE_URL_STRING = "http://192.168.0.10/statusnet/index.php/api/statuses/user_timeline.json";
	public static final String HOME_TIMELINE_URL_STRING = "http://192.168.0.10/statusnet/index.php/api/statuses/home_timeline.json";	
	public static final String FRIENDS_TIMELINE_URL_STRING = "http://192.168.0.10/statusnet/index.php/api/statuses/friends_timeline.json";	
	public static final String STATUSES_URL_STRING = "http://192.168.0.10/statusnet/index.php/api/statuses/update.json";
	
	ProgressDialog postDialog = null;

//	public static final String TWITTER_REQUEST_TOKEN_URL = "http://twitter.com/oauth/request_token";
//	public static final String TWITTER_ACCESS_TOKEN_URL = "http://twitter.com/oauth/access_token";
//	public static final String TWITTER_AUTHORIZE_URL = "http://twitter.com/oauth/authorize";
	/*
	public static final String TWITTER_REQUEST_TOKEN_URL = "https://identi.ca/api/oauth/request_token";
	public static final String TWITTER_ACCESS_TOKEN_URL = "https://identi.ca/api/oauth/access_token";
	public static final String TWITTER_AUTHORIZE_URL = "https://identi.ca/api/oauth/authorize";
	*/
	public static final String TWITTER_REQUEST_TOKEN_URL = "http://192.168.0.10/statusnet/index.php/api/oauth/request_token";
	public static final String TWITTER_ACCESS_TOKEN_URL = "http://192.168.0.10/statusnet/index.php/api/oauth/access_token";
	public static final String TWITTER_AUTHORIZE_URL = "http://192.168.0.10/statusnet/index.php/api/oauth/authorize";

	private Context ctx;
	
	private OAuthConsumer mConsumer = null;
	
	public String mToken;
	public String mSecret;
	
	SharedPreferences mSettings;

	LinkedList<UserStatus> mHomeStatus = new LinkedList<UserStatus>();
	
	HttpClient mClient;
	
	private boolean loggedIn = false;
	
	private static ConnManager instance = null;
	
	public static ConnManager getInstance(Context ctx) {
		if(instance == null) {
			instance = new ConnManager(ctx);
		}
		return instance;
	}
	
	private ConnManager(Context ctx) {
		this.ctx = ctx;
		
		HttpParams parameters = new BasicHttpParams();
		HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(parameters, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(parameters, false);
		HttpConnectionParams.setTcpNoDelay(parameters, true);
		HttpConnectionParams.setSocketBufferSize(parameters, 8192);
		
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		ClientConnectionManager tsccm = new ThreadSafeClientConnManager(parameters, schReg);
		mClient = new DefaultHttpClient(tsccm, parameters);
		
		mSettings = ctx.getSharedPreferences(OAuthActivity.PREFS, Context.MODE_PRIVATE);
		mConsumer = new CommonsHttpOAuthConsumer(
				Keys.TWITTER_CONSUMER_KEY, 
				Keys.TWITTER_CONSUMER_SECRET);
	}
	
	public void lookupSavedUserKeys() {
		// We look for saved user keys
		if(mSettings.contains(OAuthActivity.USER_TOKEN) && mSettings.contains(OAuthActivity.USER_SECRET)) {
			mToken = mSettings.getString(OAuthActivity.USER_TOKEN, null);
			mSecret = mSettings.getString(OAuthActivity.USER_SECRET, null);
			if(!(mToken == null || mSecret == null)) {
				mConsumer.setTokenWithSecret(mToken, mSecret);
			}
		}
	}
	
	public void shutdownConnectionManager() {
		mClient.getConnectionManager().shutdown();
	}
	
	// Get stuff from the two types of Twitter JSONObject we deal with: credentials and status 
	private String getCurrentTweet(JSONObject status) {
		return status.optString("text", ctx.getString(R.string.bad_value));
	}

	private String getUserName(JSONObject credentials) {
		return credentials.optString("name", ctx.getString(R.string.bad_value));
	}

	private String getLastTweet(JSONObject credentials) {
		try {
			JSONObject status = credentials.getJSONObject("status");
			return getCurrentTweet(status);
		} catch (JSONException e) {
			e.printStackTrace();
			return ctx.getString(R.string.tweet_error);
		}
	}

	// These parameters are needed to talk to the messaging service
	public HttpParams getParams() {
		// Tweak further as needed for your app
		HttpParams params = new BasicHttpParams();
		// set this to false, or else you'll get an Expectation Failed: error
		HttpProtocolParams.setUseExpectContinue(params, false);
		return params;
	}
	
	
	public JSONObject getCredentials() {
		JSONObject jso = null;
    	HttpGet get = new HttpGet(VERIFY_URL_STRING);
    	try {
			mConsumer.sign(get);
			String response = mClient.execute(get, new BasicResponseHandler());
			jso = new JSONObject(response);
			Log.d(TAG, "authenticatedQuery: " + jso.toString(2));
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jso;
	}
	
	public JSONArray getTimeline(TimelineSelector ts) {
		JSONArray array = null;
		try {		
			Uri sUri = Uri.parse(ts.url);
			Uri.Builder builder = sUri.buildUpon();
			if(ts.since_id != null) {
				builder.appendQueryParameter("since_id", String.valueOf(ts.since_id));
			} else if (ts.max_id != null) { // these are mutually exclusive
				builder.appendQueryParameter("max_id", String.valueOf(ts.max_id));
			}
			if(ts.count != null) {
				builder.appendQueryParameter("count", String.valueOf((ts.count > 200) ? 200 : ts.count));
			}
			if(ts.page != null) {
				builder.appendQueryParameter("page", String.valueOf(ts.page));
			}
			HttpGet get = new HttpGet(builder.build().toString());
			mConsumer.sign(get);
			String response = mClient.execute(get, new BasicResponseHandler());
			array = new JSONArray(response);
		}
		catch (JSONException e) {
			e.printStackTrace();
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		}
		return array;
	}	
	
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public class TimelineSelector extends Object {
		public String url; // the url to perform the query from
		// not all these apply to every url - you are responsible
		public Long since_id; // ids newer than this will be fetched
		public Long max_id; // ids older than this will be fetched
		public Integer count; // # of tweets to fetch Max is 200
		public Integer page; // # of page to fetch (with limits)
		
		public TimelineSelector(String u) {
			url = u;
			max_id = null;
			since_id = null;
			count = null;
			page = null;
		}
		
		@SuppressWarnings("unused")
		public TimelineSelector(String u, Long since, Long max, Integer cnt, Integer pg) {
			url = u;
			max_id = max;
			since_id = since;
			count = cnt;
			page = pg;
		}
	}
	
	public class UserStatus {
		
		JSONObject mStatus;
		JSONObject mUser;
		
		public UserStatus(JSONObject status) throws JSONException {

			mStatus = status;
			mUser = status.getJSONObject("user");
		}
		@SuppressWarnings("unused")
		public long getId() {
			return mStatus.optLong("id", -1);
		}
		public String getUserName() {
			return mUser.optString("name", ctx.getString(R.string.bad_value));
		}
		public String getText() {
			return getCurrentTweet(mStatus);
		}
		public String getCreatedAt() {
			@SuppressWarnings("unused")
			Time ret1 = new Time();
			return mStatus.optString("created_at", ctx.getString(R.string.bad_value));
		}
	}
}
