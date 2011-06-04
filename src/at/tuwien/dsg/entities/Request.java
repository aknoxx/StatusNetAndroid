package at.tuwien.dsg.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.tuwien.dsg.common.User;

public class Request {

	private String qualifier;	
	private User addressedUser;	// nullable
	private String operation;
	private String service;
	private List<String> hashTags;
	private String url;
	private Condition condition;
	private Map<String, String> variables;
	
	private long tweetId;
	private String sender;
	private Date createdAt;
	
	public Request() {
		hashTags = new ArrayList<String>();
		variables = new HashMap<String, String>();
	}

	public Request(String qualifier, User addressedUser, String serviceName,
			String serviceOperation, List<String> hashTags, String url, long tweetId,
			String sender, Date createdAt) {
		super();
		this.qualifier = qualifier;
		this.addressedUser = addressedUser;
		this.setOperation(serviceName);
		this.setService(serviceOperation);
		this.hashTags = hashTags;
		this.url = url;
		this.tweetId = tweetId;
		this.sender = sender;
		this.createdAt = createdAt;
	}

	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String type) {
		this.qualifier = type;
	}
	public List<String> getHashTags() {
		return hashTags;
	}
	public void setHashTags(List<String> hashTags) {
		this.hashTags = hashTags;
	}

	public void setRequester(String requester) {
		this.sender = requester;
	}

	public String getRequester() {
		return sender;
	}

	public void setCreatedAt(Date date) {
		this.createdAt = date;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setTweetId(long tweetId) {
		this.tweetId = tweetId;
	}

	public long getTweetId() {
		return tweetId;
	}

	public void setAddressedUser(User addressedUser) {
		this.addressedUser = addressedUser;
	}

	public User getAddressedUser() {
		return addressedUser;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getOperation() {
		return operation;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getService() {
		return service;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public Condition getCondition() {
		return condition;
	}
}
