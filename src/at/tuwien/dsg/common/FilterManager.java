package at.tuwien.dsg.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.User;

import at.tuwien.dsg.entities.Filter;
import at.tuwien.dsg.entities.Request;

public class FilterManager {

	private List<Filter> filters;
	private Pattern pattern;
	private Filter appliedFilter;
	
	public FilterManager() {
		filters = new ArrayList<Filter>();
	}
	
	public void setFilter(Filter filter) {
		appliedFilter = filter;
		pattern = Pattern.compile(filter.getPattern());
	}
	
	public boolean match(String text) {
		return pattern.matcher(text.trim()).matches();
	}
	
	public Request extractRequest(Status status) {
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
	
	
}
