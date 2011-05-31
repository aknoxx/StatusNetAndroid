package at.tuwien.dsg.entities;

import java.util.ArrayList;
import java.util.List;

public class Request {

	private String type;
	private String verb;
	private String object;
	private String date;
	private String duration;
	private List<String> hashTags;
	private String requester;
	
	public Request() {
		hashTags = new ArrayList<String>();
	}
	
	public Request(String type, String verb, String object,
			List<String> hashTags, String requester) {
		super();
		this.type = type;
		this.verb = verb;
		this.object = object;
		this.hashTags = hashTags;
		this.requester = requester;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getVerb() {
		return verb;
	}
	public void setVerb(String verb) {
		this.verb = verb;
	}
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public List<String> getHashTags() {
		return hashTags;
	}
	public void setHashTags(List<String> hashTags) {
		this.hashTags = hashTags;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	public String getRequester() {
		return requester;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return date;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getDuration() {
		return duration;
	}
}
