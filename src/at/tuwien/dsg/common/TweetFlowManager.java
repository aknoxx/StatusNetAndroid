package at.tuwien.dsg.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

//import twitter4j.Status;
//import twitter4j.User;

import at.tuwien.dsg.common.User;
import at.tuwien.dsg.common.Status;

import android.content.Context;
import at.tuwien.dsg.R;
import at.tuwien.dsg.entities.Filter;
import at.tuwien.dsg.entities.Request;
import at.tuwien.dsg.entities.TweetflowPrimitive;
import at.tuwien.dsg.util.TweetFilterParser;

public class TweetFlowManager implements ITweetflowManager {

	private List<Filter> filters;
	private Pattern pattern;
	private Filter appliedFilter;
	private List<TweetflowPrimitive> primitives = null;
	
	public List<TweetflowPrimitive> getPrimitives() {
		return primitives;
	}

	public void setPrimitives(List<TweetflowPrimitive> primitives) {
		this.primitives = primitives;
	}

	public TweetFlowManager(Context ctx) {
		primitives = new ArrayList<TweetflowPrimitive>();
		filters = new ArrayList<Filter>();
		TweetFilterParser parser = new TweetFilterParser();
		//primitives = parser.parse(ctx.getResources()
		//		.getXml(R.xml.tweetflow_primitives_filters));
	}
	
	public void setFilter(Filter filter) {
		appliedFilter = filter;
		pattern = Pattern.compile(filter.getPattern());
	}
	
	public boolean match(String text) {
		return pattern.matcher(text.trim()).matches();
	}
	
	public Request extractRequest(Status status) {
		/*
		 * Status s = new Status();
		User u = s.getUser();
		String text = s.getText();
		Date date = s.getCreatedAt();
		long id = s.getId();
		 */
		Request request = null;
		TweetflowPrimitive currentPrimitive = null;
		for (TweetflowPrimitive primitive : primitives) {
			if((request = primitive.extractRequest(status)) != null) {
				currentPrimitive = primitive;
				break;
			}
		}
		
		
		/*
		User user = null;
		String requester = "noName";
		if((user = status.getUser()) != null) {
			requester = user.getName();
		}
		
		
		
		String text = status.getText().trim();
		
		Request request = new Request();
		request.setRequester(requester);
		
		// removes all spaces, tabs?
		String[] words = text.split("\\s+");
		request.setType(words[0]);
		
		String secondPart = words[1].substring(1).trim();
		
		String[] next = secondPart.split("\\?");
		String[] command = next[0].split("\\.");
		request.setVerb(command[0]);
		request.setObject(command[1]);
		
		String[] time = next[1].split("\\&");
		
		String date = time[0].substring(5);
		request.setDate(date);
		
		String duration = time[1].substring(9);
		request.setDuration(duration);
		
		request.getHashTags().add(words[2]);
		*/
		return request;		
	}
	
	public void addFilter(Filter filter) {
		filters.add(filter);
	}
	
	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	@Override
	public void loadRequestsFromDb() {
		// TODO Auto-generated method stub
		
	}
}
