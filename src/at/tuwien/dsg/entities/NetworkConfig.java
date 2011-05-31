package at.tuwien.dsg.entities;

public class NetworkConfig {

	private String name;
	private String consumerKey;
	private String consumerSecret;
	private String accessTokenURL;
	private String authorizationURL;
	private String requestTokenURL;
	private String restBaseURL;
	private String searchBaseURL;
	private String authenticationURL;
	
	public NetworkConfig copy() {
		NetworkConfig nc = new NetworkConfig();
		nc.name = this.name;
		nc.consumerKey = this.consumerKey;
		nc.accessTokenURL = this.accessTokenURL;
		nc.authorizationURL = this.authorizationURL;
		nc.requestTokenURL = this.requestTokenURL;
		nc.restBaseURL = this.restBaseURL;
		nc.searchBaseURL = this.searchBaseURL;
		nc.authenticationURL = this.authenticationURL;
		
		return nc;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getConsumerKey() {
		return consumerKey;
	}
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	public String getConsumerSecret() {
		return consumerSecret;
	}
	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}
	public String getAccessTokenURL() {
		return accessTokenURL;
	}
	public void setAccessTokenURL(String accessTokenURL) {
		this.accessTokenURL = accessTokenURL;
	}
	public String getAuthorizationURL() {
		return authorizationURL;
	}
	public void setAuthorizationURL(String authorizationURL) {
		this.authorizationURL = authorizationURL;
	}
	public String getRequestTokenURL() {
		return requestTokenURL;
	}
	public void setRequestTokenURL(String requestTokenURL) {
		this.requestTokenURL = requestTokenURL;
	}
	public void setRestBaseURL(String restBaseURL) {
		this.restBaseURL = restBaseURL;
	}

	public String getRestBaseURL() {
		return restBaseURL;
	}

	public void setSearchBaseURL(String searchBaseURL) {
		this.searchBaseURL = searchBaseURL;
	}

	public String getSearchBaseURL() {
		return searchBaseURL;
	}

	public void setAuthenticationURL(String authenticationURL) {
		this.authenticationURL = authenticationURL;
	}

	public String getAuthenticationURL() {
		return authenticationURL;
	}
}