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
import at.tuwien.dsg.activities.OAuthActivity;
import at.tuwien.dsg.entities.Network;
import at.tuwien.dsg.entities.Urls;


public class ConnManager {
	private static final String TAG = "ConnManager";
	
	
	private static Network currentNetwork; 
	private static Urls urls;
	
	ProgressDialog postDialog = null;
	
	private Context ctx;
	
	private static OAuthConsumer mConsumer = null;
	
	private String mToken;
	private String mSecret;
	
	SharedPreferences mSettings;
	
	public static final String LOGGEDIN = "loggedIn";

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
		
		/**
		 * Set saved state
		 */
//		if(currentNetwork != null) {
//			this.currentNetwork = currentNetwork;
//			this.urls = new Urls(currentNetwork.getRestBaseURL());
//		}
	}
	
	public static void restartConnectionManagerWithNewNetwork(Network network) {
		urls = new Urls(network.getRestBaseURL());
		mConsumer = new CommonsHttpOAuthConsumer(
				network.getConsumerKey(), 
				network.getConsumerSecret());
		currentNetwork = network;
		instance = null;
	}
	
	public Network getCurrentNetwork() {
		return currentNetwork;
	}
	
	public boolean getKeysAvailable() {
		// We look for saved user keys
		if(mSettings.contains(OAuthActivity.USER_TOKEN) && mSettings.contains(OAuthActivity.USER_SECRET)) {
			mToken = mSettings.getString(OAuthActivity.USER_TOKEN, null);
			mSecret = mSettings.getString(OAuthActivity.USER_SECRET, null);
			if(mToken != null && mSecret != null) {
				mConsumer.setTokenWithSecret(mToken, mSecret);
				return true;
			}
		}
		return false;
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

	// These parameters are needed to talk to the messaging service
	public HttpParams getParams() {
		// Tweak further as needed for your app
		HttpParams params = new BasicHttpParams();
		// set this to false, or else you'll get an Expectation Failed: error
		HttpProtocolParams.setUseExpectContinue(params, false);
		return params;
	}
	
	public String getmToken() {
		return mToken;
	}

	public String getmSecret() {
		return mSecret;
	}

	public JSONObject getCredentials() {
		JSONObject jso = null;
    	HttpGet get = new HttpGet(urls.getVerifyUrlString());
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
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putBoolean(LOGGEDIN, true);
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

	public void setUrls(Urls urls) {
		this.urls = urls;
	}

	public Urls getUrls() {
		return urls;
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
