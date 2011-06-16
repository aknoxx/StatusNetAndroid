package at.tuwien.dsg.entities;

import java.io.Serializable;

public class Network implements Serializable {

	private static final long serialVersionUID = 3319261788597734731L;
	
	private String name;
	private String restBaseURL;
	private String consumerKey;
	private String consumerSecret;
	
	public Network(String name, String restBaseURL, String consumerKey,
			String consumerSecret) {
		super();
		this.name = name;
		this.restBaseURL = restBaseURL;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRestBaseURL() {
		return restBaseURL;
	}
	public void setRestBaseURL(String restBaseURL) {
		this.restBaseURL = restBaseURL;
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
}
