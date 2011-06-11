package at.tuwien.dsg.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

//import twitter4j.Status;
//import twitter4j.User;

import at.tuwien.dsg.common.Status;

import android.content.Context;
import at.tuwien.dsg.entities.Filter;
import at.tuwien.dsg.entities.Request;
import at.tuwien.dsg.entities.TweetflowPrimitive;
import at.tuwien.dsg.util.TweetFilterParser;

public class TweetFlowManager implements ITweetflowManager {

	private RequestDbAdapter dbAdapter;
	
	private List<Filter> filters;
	private Pattern pattern;
	private Filter appliedFilter;
	private List<TweetflowPrimitive> primitives = null;
	private ArrayList<Request> requests;
	private ArrayList<Request> filteredRequests;
	
	public ArrayList<Request> getFilteredRequests() {
		return filteredRequests;
	}

	public void setFilteredRequests(ArrayList<Request> filteredRequests) {
		this.filteredRequests = filteredRequests;
	}

	private boolean useFilter = false;
	
	private Map<CharSequence, Boolean> displayFilter;
	private static final CharSequence[] qualifiers = { "SR", "SF", "TF", "LG", "VA",
		"AccessVariable", "AccessServiceResult" };
	

	public TweetFlowManager(Context ctx) {
		displayFilter = new HashMap<CharSequence, Boolean>();
		for (CharSequence qualifier : qualifiers) {
			displayFilter.put(qualifier, new Boolean(true));
		}
		
		dbAdapter = new RequestDbAdapter(ctx);
		dbAdapter.open();
		
		requests = new ArrayList<Request>();
		filteredRequests = new ArrayList<Request>();
		primitives = new ArrayList<TweetflowPrimitive>();
		filters = new ArrayList<Filter>();
		TweetFilterParser parser = new TweetFilterParser();
		//primitives = parser.parse(ctx.getResources()
		//		.getXml(R.xml.tweetflow_primitives_filters));
	}
	
	public void downloadNewTweets() {
		
		requests.addAll(0, testDownloadData());
		refreshFilteredRequests();
	}
	
	public void clearRequestList() {
		requests.clear();
		filteredRequests.clear();
	}
	
	public boolean saveRequests() {
		boolean success = true;
		for (Request request : filteredRequests) {
			if(dbAdapter.saveRequest(request) > 0) {
				request.setSaved(true);
			}			
			else {
				success = false;
			}
		}
		//if(success) {
		//	refreshFilteredRequests();
		//}
		return success;
	}
	
	public ArrayList<Request> loadRequests() {
		requests.clear();
		requests.addAll(dbAdapter.loadAllRequests());
		return requests;
	}
	
	private void refreshFilteredRequests() {
		filteredRequests.clear();
		for (Request req : requests) {
			if(displayFilter.containsKey(req.getQualifier())) {
				if(displayFilter.get(req.getQualifier())) {
					filteredRequests.add(req);
				}
			}
		}
	}
	
	public ArrayList<Request> loadFilteredRequests() {
		//loadRequests();
		refreshFilteredRequests();
		return filteredRequests;
	}
	
	public void deleteSavedRequests() {
		dbAdapter.clearDb();
	}
	
	public List<TweetflowPrimitive> getPrimitives() {
		return primitives;
	}

	public void setPrimitives(List<TweetflowPrimitive> primitives) {
		this.primitives = primitives;
	}

	public Map<CharSequence, Boolean> getDisplayFilter() {
		return displayFilter;
	}

	public void setDisplayFilter(Map<CharSequence, Boolean> displayFilter) {
		this.displayFilter = displayFilter;
	}
	
	public void setFilter(Filter filter) {
		appliedFilter = filter;
		pattern = Pattern.compile(filter.getPattern());
	}
	
	public boolean match(String text) {
		return pattern.matcher(text.trim()).matches();
	}
	
	public void deleteRequest(int id) {
		
		for (int i = 0; i < filteredRequests.size(); i++) {
			if(requests.get(id).getTweetId() == filteredRequests.get(i).getTweetId()) {
				filteredRequests.remove(i);
				break;
			}
		}

		if(requests.get(id).isSaved()) {
			dbAdapter.deleteRequest(requests.get(id).getDbId());
		}
		requests.remove(id);
	}
	
	public void setTestTFs() {
		requests.clear();
		
		long time = System.currentTimeMillis();
		
		String text = "SR forecast.weather location=vienna&date=weekend #weather #forecast #vienna #weekend" +
				" - what's a SR? -> http://bit.ly/fF0yDp #tweetflows";
		Status status = new Status(new String("User1"), text, new Date(time + 60000), (long) 5);
		
		text = "SR @johannes2112 recommend.Restaurant location=Vienna,1020&date=today&time=20:00 " +
		 "[@ikangai.availability?=true]";
		status = new Status(new String("User1"), text, new Date(time + 120000), (long) 4);
		requests.add(extractRequest(status));
		
		text = "SR @aknoxx proofread.WebPage http://www.ikangai.com/blog/tweetflows-specification-version-1-0";
		status = new Status(new String("User1"), text, new Date(time + 180000), (long) 3);
		requests.add(extractRequest(status));
		
		text = "SF @ikangai didProofread.Blogentry http://www.ikangai.com/blog #tweetflows #specification";
		status = new Status(new String("User1"), text, new Date(time + 240000), (long) 2);
		requests.add(extractRequest(status));
		
		refreshFilteredRequests();
	}
	
	private ArrayList<Request> testDownloadData() {
		
		long time = System.currentTimeMillis();
		
		ArrayList<Request> requests = new ArrayList<Request>();
		
		String text = "VA state finished";
		Status status = new Status(new String("User1"), text, new Date(time), (long) 2);
		requests.add(extractRequest(status));
		
		text = "@ikangai.state?";
		status = new Status(new String("User1"), text, new Date(time + 1* 60000), (long) 2);
		requests.add(extractRequest(status));
		
		text = "@cerridan.recommend.Restaurant?";
		status = new Status(new String("User1"), text, new Date(time + 2* 60000), (long) 2);
		requests.add(extractRequest(status));
		
		text = "TF didBegin.Tweetflow #dinner #restaurant #carpark";
		status = new Status(new String("User1"), text, new Date(time + 3* 60000), (long) 3);
		requests.add(extractRequest(status));
		
		text = "SR @cerridan recommend.Restaurant location=Vienna,1020&date=today&time=20:00";
		status = new Status(new String("User1"), text, new Date(time + 4* 60000), (long) 3);
		requests.add(extractRequest(status));
		
		text = "SR @ikangai get.ParkingInfo location=Vienna,1020&date=today&time=20:00";
		status = new Status(new String("User1"), text, new Date(time + 5* 60000), (long) 3);
		requests.add(extractRequest(status));
		
		text = "SR @johannes2112 get.Availability location=Vienna,1020&date=today&time=20:00";
		status = new Status(new String("User1"), text, new Date(time + 6* 60000), (long) 3);
		requests.add(extractRequest(status));
		
		text = "SR @redali75 get.Availability location=Vienna,1020&date=today&time=20:00";
		status = new Status(new String("User1"), text, new Date(time + 7* 60000), (long) 3);
		requests.add(extractRequest(status));
		
		text = "TF didFinish.Tweetflow #dinner #restaurant #carpark";
		status = new Status(new String("User1"), text, new Date(time + 8* 60000), (long) 3);
		requests.add(extractRequest(status));
		
		text = "LG didread.article http://www.theregister.co.uk/2011/05/27/distimo_app_store_report/ #android #app #sales";
		status = new Status(new String("User1"), text, new Date(time + 9* 60000), (long) 3);
		requests.add(extractRequest(status));
		
		text = "LG didAdapt.LayoutToDisplayRequests date=27.05.2011&duration=0130 #StatusNetAndroid";
		status = new Status(new String("User1"), text, new Date(time + 10* 60000), (long) 3);
		requests.add(extractRequest(status));
		
		return requests;
	}
	
	public ArrayList<Request> receiveTweetflows() {
		
		// TODO receive tweets with newer id's
		
		if(useFilter) {
			return filteredRequests;
		}
		return requests;
	}
	
	public Request extractRequest(Status status) {
		/*
		 * Status s = new Status();
		String u = s.getUser();
		String text = s.getText();
		Date date = s.getCreatedAt();
		long id = s.getId();
		 */
		
		/*Request request = null;
		TweetflowPrimitive currentPrimitive = null;
		for (TweetflowPrimitive primitive : primitives) {
			if((request = primitive.extractRequest(status)) != null) {
				currentPrimitive = primitive;
				break;
			}
		}*/
		
		return new TweetflowPrimitive().extractRequest(status);
		
		/*
		String user = null;
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
		//return request;		
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
		requests.addAll(dbAdapter.loadAllRequests());
		refreshFilteredRequests();		
	}
}
