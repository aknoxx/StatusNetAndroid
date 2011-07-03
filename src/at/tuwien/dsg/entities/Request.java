package at.tuwien.dsg.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request implements Serializable {

	private static final long serialVersionUID = -8448411208707721327L;
	
	private String qualifier;	
	private String addressedUser;
	private String operation;
	private String service;
	private String operationExecutionStatus;
	private List<String> hashTags;
	private String url;
	private Condition condition;
	private Map<String, String> variables;	
	private String completeRequestText;
	
	private Long dependentOnTweetId;
	private boolean isClosedSequence;
	private Integer ordering;
	private Integer dependentOrderNumber;
	
	private long tweetId;
	private String sender;
	private Date createdAt;
	
	// this field is not saved to Db
	private boolean saved;
	private long dbId;
	
	public Request() {
		hashTags = new ArrayList<String>();
		variables = new HashMap<String, String>();
	}

	public Request(String completeRequestText, String qualifier, String addressedUser, String serviceName,
			String serviceOperation, List<String> hashTags, String url, long tweetId,
			String sender, Date createdAt) {
		super();
		this.completeRequestText = completeRequestText;
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
	
	public Request(String completeRequestText, String qualifier, String addressedUser, String operation,
			String service, String url, long tweetId,
			String sender, Date createdAt) {
		super();
		this.completeRequestText = completeRequestText;
		this.qualifier = qualifier;
		this.addressedUser = addressedUser;
		this.setOperation(operation);
		this.setService(service);
		this.url = url;
		this.tweetId = tweetId;
		this.sender = sender;
		this.createdAt = createdAt;
	}
	
	/*
     * Returns a ContentValues instance (a map) for this NoteInfo instance. This is useful for
     * inserting a NoteInfo into a database.
     */
   /* public ContentValues getContentValues() {
        // Gets a new ContentValues object
        ContentValues v = new ContentValues();

        // Adds map entries for the user-controlled fields in the map
        v.put(Request.Requests., title);
        v.put(NotePad.Notes.COLUMN_NAME_NOTE, note);
        v.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, createDate);
        v.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, modDate);
        return v;

    }*/

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

	public void setAddressedUser(String addressedUser) {
		this.addressedUser = addressedUser;
	}

	public String getAddressedUser() {
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

	public void setCompleteRequestText(String completeRequestText) {
		this.completeRequestText = completeRequestText;
	}

	public String getCompleteRequestText() {
		return completeRequestText;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}

	public boolean isSaved() {
		return saved;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	public long getDbId() {
		return dbId;
	}

	public void setDependentOnTweetId(Long dependentOnTweetId) {
		this.dependentOnTweetId = dependentOnTweetId;
	}

	public Long getDependentOnTweetId() {
		return dependentOnTweetId;
	}

	public void setClosedSequence(boolean isClosedSequence) {
		this.isClosedSequence = isClosedSequence;
	}

	public boolean isClosedSequence() {
		return isClosedSequence;
	}

	public void setOperationExecutionStatus(String operationExecutionStatus) {
		this.operationExecutionStatus = operationExecutionStatus;
	}

	public String getOperationExecutionStatus() {
		return operationExecutionStatus;
	}

	public void setOrdering(Integer ordering) {
		this.ordering = ordering;
	}

	public Integer getOrdering() {
		return ordering;
	}

	public void setDependentOrderNumber(Integer dependentOrderNumber) {
		this.dependentOrderNumber = dependentOrderNumber;
	}

	public Integer getDependentOrderNumber() {
		return dependentOrderNumber;
	}
}
