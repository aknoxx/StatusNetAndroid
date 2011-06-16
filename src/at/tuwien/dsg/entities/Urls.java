package at.tuwien.dsg.entities;

public class Urls {

	private static String API_URL_STRING = "http://twitter.com/";
	
	private static final String VERIFY_URL_STRING = 			"account/verify_credentials.json";
	private static final String PUBLIC_TIMELINE_URL_STRING =	"statuses/public_timeline.json";
	private static final String USER_TIMELINE_URL_STRING = 	 	"statuses/user_timeline.json";
	private static final String HOME_TIMELINE_URL_STRING = 	 	"statuses/home_timeline.json";	
	private static final String FRIENDS_TIMELINE_URL_STRING = 	"statuses/friends_timeline.json";	
	private static final String STATUSES_URL_STRING = 			"statuses/update.json";

	private static final String REQUEST_TOKEN_URL = "oauth/request_token";
	private static final String ACCESS_TOKEN_URL = 	"oauth/access_token";
	private static final String AUTHORIZE_URL = 	"oauth/authorize";
	
	public static void setApiUrlString(String apiUrlString) {
		//API_URL_STRING = apiUrlString;
	}
	
	public static String getVerifyUrlString() {
		return API_URL_STRING + VERIFY_URL_STRING;
	}	
	public static String getPublicTimelineUrlString() {
		return API_URL_STRING + PUBLIC_TIMELINE_URL_STRING;
	}	
	public static String getUserTimelineUrlString() {
		return API_URL_STRING + USER_TIMELINE_URL_STRING;
	}	
	public static String getHomeTimelineUrlString() {
		return API_URL_STRING + HOME_TIMELINE_URL_STRING;
	}	
	public static String getFriendsTimelineUrlString() {
		return API_URL_STRING + FRIENDS_TIMELINE_URL_STRING;
	}	
	public static String getStatusesUrlString() {
		return API_URL_STRING + STATUSES_URL_STRING;
	}	
	public static String getRequestTokenUrl() {
		return API_URL_STRING + REQUEST_TOKEN_URL;
	}
	public static String getAccessTokenUrl() {
		return API_URL_STRING + ACCESS_TOKEN_URL;
	}
	public static String getAuthorizeUrl() {
		return API_URL_STRING + AUTHORIZE_URL;
	}
}