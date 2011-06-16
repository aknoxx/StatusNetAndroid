package at.tuwien.dsg.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class DisplayData implements Serializable {

	private static final long serialVersionUID = -4340707485244336609L;
	
	public ArrayList<Request> requests;
	public ArrayList<Request> filteredRequests;
	public Map<CharSequence, Boolean> displayFilter;
	public Long newestSavedId = new Long(0);
	public Long newestReceivedId = new Long(0);
	
	public ArrayList<Request> getRequests() {
		return requests;
	}
	public void setRequests(ArrayList<Request> requests) {
		this.requests = requests;
	}
	public ArrayList<Request> getFilteredRequests() {
		return filteredRequests;
	}
	public void setFilteredRequests(ArrayList<Request> filteredRequests) {
		this.filteredRequests = filteredRequests;
	}
	public Map<CharSequence, Boolean> getDisplayFilter() {
		return displayFilter;
	}
	public void setDisplayFilter(Map<CharSequence, Boolean> displayFilter) {
		this.displayFilter = displayFilter;
	}
	public Long getNewestSavedId() {
		return newestSavedId;
	}
	public void setNewestSavedId(Long newestSavedId) {
		this.newestSavedId = newestSavedId;
	}
	public Long getNewestReceivedId() {
		return newestReceivedId;
	}
	public void setNewestReceivedId(Long newestReceivedId) {
		this.newestReceivedId = newestReceivedId;
	}
}
