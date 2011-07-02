package at.tuwien.dsg.common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import at.tuwien.dsg.common.Status;
import at.tuwien.dsg.common.Request.Conditions;
import at.tuwien.dsg.common.Request.HashTags;
import at.tuwien.dsg.common.Request.Requests;
import at.tuwien.dsg.common.Request.Variables;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;
import at.tuwien.dsg.entities.Condition;
import at.tuwien.dsg.entities.DisplayData;
import at.tuwien.dsg.entities.Network;
import at.tuwien.dsg.entities.Request;

public class TweetFlowManager implements ITweetflowManager {

	private RequestDbAdapter dbAdapter;
	private static Context ctx;
	private DisplayData dd;
	private TweetflowFilter tfFilter;
	
	private static final CharSequence[] qualifiers = { "SR", "SF", "TF", "LG", "SP",
		"RT", "SD", "RJ", "VA", "AccessVariable", "AccessServiceResult" };
	
	private static TweetFlowManager instance = null;
	
	public static TweetFlowManager getInstance(Context context, DisplayData displayData) {
		if(instance == null) {
			ctx = context;
			instance = new TweetFlowManager(ctx, displayData);
		}
		return instance;
	}	

	private TweetFlowManager(Context ctx, DisplayData displayData) {
		if(displayData == null) {
			dd = new DisplayData();
			dd.setDisplayFilter(new HashMap<CharSequence, Boolean>());
			for (CharSequence qualifier : qualifiers) {
				dd.getDisplayFilter().put(qualifier, new Boolean(true));
			}
			
			dbAdapter = new RequestDbAdapter(ctx);
			dbAdapter.open();
			
			dd.setRequests(new ArrayList<Request>());
			dd.setFilteredRequests(new ArrayList<Request>());
			dd.setSavedRequests(new ArrayList<Request>());
			dd.setSavedFilteredRequests(new ArrayList<Request>());
		}
		else {
			dd = displayData;
			dbAdapter = new RequestDbAdapter(ctx);
			dbAdapter.open();
		}
		tfFilter = new TweetflowFilter();
	}
	
	public DisplayData getDd() {
		return dd;
	}
	
	public ArrayList<Request> getFilteredRequests() {
		return dd.getFilteredRequests();
	}
	
	public ArrayList<Request> getSavedFilteredRequests() {
		return dd.getSavedFilteredRequests();
	}

	public void setFilteredRequests(ArrayList<Request> filteredRequests) {
		dd.setFilteredRequests(filteredRequests);
	}
	
	public Long getNewestReceivedId() {
		return dd.getNewestReceivedId();
	}
	
	public Long getNewestSavedId() {
		return dd.getNewestSavedId();
	}

	public void downloadNewTweets() {
		
		dd.getRequests().addAll(0, testDownloadData());
		refreshFilteredRequests();
	}
	
	public void clearRequestList() {
		dd.getRequests().clear();
		dd.getFilteredRequests().clear();
	}
	
	public boolean saveRequests() {
		// TODO save closed squences !!!
		boolean success = true;
		for (Request request : dd.getFilteredRequests()) {
			if(dbAdapter.saveRequest(request) > 0) {
				request.setSaved(true);
				if(request.getTweetId() > dd.getNewestSavedId()) {
					dd.setNewestSavedId(request.getTweetId());
				}
			}			
			else {
				success = false;
			}
		}
		return success;
	}
	
	public ArrayList<Request> loadRequests() {
		dd.getRequests().clear();
		dd.getRequests().addAll(dbAdapter.loadAllRequests());
		return dd.getRequests();
	}
	
	private void refreshFilteredRequests() {
		dd.getFilteredRequests().clear();
		for (Request req : dd.getRequests()) {
			if(dd.getDisplayFilter().containsKey(req.getQualifier())) {
				if(dd.getDisplayFilter().get(req.getQualifier())) {
					dd.getFilteredRequests().add(req);
				}
			}
		}
	}
	
	private void refreshFilteredSavedRequests() {
		dd.getSavedFilteredRequests().clear();
		for (Request req : dd.getSavedRequests()) {
			if(dd.getDisplayFilter().containsKey(req.getQualifier())) {
				if(dd.getDisplayFilter().get(req.getQualifier())) {
					dd.getSavedFilteredRequests().add(req);
				}
			}
		}
	}
	
	public ArrayList<Request> loadFilteredRequests() {
		refreshFilteredRequests();
		return dd.getFilteredRequests();
	}
	
	public void deleteSavedRequests() {
		dbAdapter.clearDb();
		Iterator<Request> iter = dd.getRequests().iterator();
		Request request = null;
		while(iter.hasNext()){
			request = (Request) iter.next();
			if(request.isSaved()) {
				iter.remove();
			}
		}
		refreshFilteredRequests();
	}
	
	public List<Network> getAllNetworks() {
		return dbAdapter.loadAllNetworks();
	}
	
	public Map<CharSequence, Boolean> getDisplayFilter() {
		return dd.getDisplayFilter();
	}

	public void setDisplayFilter(Map<CharSequence, Boolean> displayFilter) {
		dd.setDisplayFilter(displayFilter);
	}
	
	public void deleteRequest(int id) {
		
		for (int i = 0; i < dd.getFilteredRequests().size(); i++) {
			if(dd.getRequests().get(id).getTweetId() == dd.getFilteredRequests().get(i).getTweetId()) {
				dd.getFilteredRequests().remove(i);
				break;
			}
		}

		if(dd.getRequests().get(id).isSaved()) {
			dbAdapter.deleteRequest(dd.getRequests().get(id).getDbId());
		}
		dd.getRequests().remove(id);
	}
	
	public void setTestTFs() {
		dd.getRequests().clear();
		
		long time = System.currentTimeMillis();
		
		String text = "SR forecast.weather location=vienna&date=weekend #weather #forecast #vienna #weekend" +
				" - what's a SR? -> http://bit.ly/fF0yDp #tweetflows";
		Status status = new Status(new String("User1"), text, new Date(time + 60000), (long) 5);
		
		text = "SR @johannes2112 recommend.Restaurant location=Vienna,1020&date=today&time=20:00 " +
		 "[@ikangai.availability?=true]";
		status = new Status(new String("User1"), text, new Date(time + 120000), (long) 4);
		dd.getRequests().add(extractRequest(status));
		
		text = "SR @aknoxx proofread.WebPage http://www.ikangai.com/blog/tweetflows-specification-version-1-0";
		status = new Status(new String("User1"), text, new Date(time + 180000), (long) 3);
		dd.getRequests().add(extractRequest(status));
		
		text = "SF @ikangai didProofread.Blogentry http://www.ikangai.com/blog #tweetflows #specification";
		status = new Status(new String("User1"), text, new Date(time + 240000), (long) 2);
		dd.getRequests().add(extractRequest(status));
		
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
	
	public void addUserStatus(ConnManager.UserStatus status) {
		List<Request> requests;
		if((requests = extractRequestFromUserStatus(status)) != null) {
			for (Request r : requests) {
				dd.getRequests().add(0, r);
				dd.setNewestReceivedId(status.getId());
			}			
			refreshFilteredRequests();
		}	
	}
	
	private Long predecessorTweetId = null;
	
	public List<Request> extractRequestFromUserStatus(ConnManager.UserStatus status) {
		try {
			List<Request> requests = tfFilter.extractRequestFromBloaStatus(status);
//			if(requests != null) {
//				if(requests.size() == 1) {
//					Request request = requests.get(0);
//					if(request.isClosedSequence()) {
//						if(request.getPredecessorTweetId() == new Long(0)) {
//							predecessorTweetId = request.getTweetId();
//						}
//						else {
//							request.setPredecessorTweetId(predecessorTweetId);
//						}
//					}
//				}
//				return requests;
//			}
//			return null;
			return requests;
		} catch (ParseException e) {
			return null;
		}
	}
	
	public Request extractRequest(Status status) {
		
		return new TweetflowFilter().extractRequest(status);
	}
	
	public void resetIds() {
		dd.setNewestReceivedId(new Long(0));
		dd.setNewestSavedId(new Long(0));
	}

	@Override
	public void loadRequestsFromDb() {
		List<Request> rs;
		if((rs = dbAdapter.loadAllRequests()) != null) {
			dd.getSavedRequests().addAll(rs);
		}
		refreshFilteredSavedRequests();		
	}
	
	public boolean loadRequestsFromContentProvider(ContentProviderClient requestsProvider,
			ContentProviderClient hashTagsProvider, ContentProviderClient conditionsProvider,
			ContentProviderClient variablesProvider) throws RemoteException {
		
		if(requestsProvider == null || hashTagsProvider == null 
				|| conditionsProvider == null || variablesProvider == null) {
			return false;
		}
		
		Cursor cRequests = requestsProvider.query(Requests.CONTENT_URI, REQUEST_PROJECTION, null, null, null);
		
		// cpc.release();
		
		if(cRequests != null) {
			if(cRequests.getCount() > 0 ){
				cRequests.moveToFirst();
				
				int idColumn = cRequests.getColumnIndex(Requests._ID);
				int qualifierColumn = cRequests.getColumnIndex(Requests.QUALIFIER);
				int addressedUserNameColumn = cRequests.getColumnIndex(Requests.ADDRESSED_USER_NAME);
				int operationColumn = cRequests.getColumnIndex(Requests.OPERATION);
				int serviceColumn = cRequests.getColumnIndex(Requests.SERVICE);
				int urlColumn = cRequests.getColumnIndex(Requests.URL);
				int completeRequestTextColumn = cRequests.getColumnIndex(Requests.COMPLETE_REQUEST_TEXT);
				int tweetIdColumn = cRequests.getColumnIndex(Requests.TWEET_ID);
				int senderNameColumn = cRequests.getColumnIndex(Requests.SENDER_NAME);
				int createdAtColumn = cRequests.getColumnIndex(Requests.CREATED_AT);
				
				cRequests.moveToFirst();
				while (cRequests.isAfterLast() == false) {
					Request r = new Request();
					r.setAddressedUser(cRequests.getString(addressedUserNameColumn));
					r.setCompleteRequestText(cRequests.getString(completeRequestTextColumn));
					r.setCreatedAt(new Date(cRequests.getLong(createdAtColumn)));
					r.setOperation(cRequests.getString(operationColumn));
					r.setService(cRequests.getString(serviceColumn));
					r.setTweetId(cRequests.getLong(tweetIdColumn));
					r.setUrl(cRequests.getString(urlColumn));
					r.setQualifier(cRequests.getString(qualifierColumn));
					r.setRequester(cRequests.getString(senderNameColumn));
					
					r.setSaved(true);
					long id = cRequests.getLong(idColumn);
					r.setDbId(id);
					
					Cursor htc = hashTagsProvider.query(HashTags.CONTENT_URI, HASHTAG_PROJECTION, 
								HashTags.REQUEST_ID + "=?", new String[] { ""+id }, null);
					
					if(htc != null) {
						if(htc.getCount() > 0) {
							htc.moveToFirst();
							
							int nameColumn = htc.getColumnIndex(HashTags.NAME);
							
							List<String> hashTags = new ArrayList<String>();
							
							for(int j=0; j<htc.getCount(); j++) {
								hashTags.add(htc.getString(nameColumn));
							}
							r.setHashTags(hashTags);
						}
					}
					
					Cursor cc = conditionsProvider.query(Conditions.CONTENT_URI, CONDITION_PROJECTION, 
								Conditions.REQUEST_ID + "=?", new String[] { ""+id }, null);
					
					if(cc != null) {
						if(cc.getCount() > 0) {
							cc.moveToFirst();
							
							int usernameColumn = cc.getColumnIndex(Conditions.USER_NAME);
							int variableColumn = cc.getColumnIndex(Conditions.VARIABLE);
							int valueColumn = cc.getColumnIndex(Conditions.VALUE);
		
							r.setCondition(new Condition(
									cc.getString(usernameColumn), 
									cc.getString(variableColumn), 
									cc.getString(valueColumn)
									));
						}
					}
					
					Cursor vc = variablesProvider.query(Variables.CONTENT_URI, VARIABLE_PROJECTION,
								Variables.REQUEST_ID + "=?", new String[] { ""+id }, null);
					
					if(vc != null) {
						if(vc.getCount() > 0) {
							vc.moveToFirst();
							
							int nameColumn = vc.getColumnIndex(Variables.NAME);
							int valueColumn = vc.getColumnIndex(Variables.VALUE);
							
							for(int j=0; j<vc.getCount(); j++) {
								r.getVariables().put(vc.getString(nameColumn), 
										vc.getString(valueColumn));
							}
						}
					}
					
					dd.getRequests().add(r);
					cRequests.moveToNext();
				}
				cRequests.close();
			}
		}
		//dd.getRequests().addAll(dbAdapter.loadAllRequests());
		refreshFilteredRequests();		
		
		return true;
	}
	
	final String[] REQUEST_PROJECTION = 
		new String[] { 
			Requests._ID,
			Requests.QUALIFIER,
			Requests.ADDRESSED_USER_NAME,
			Requests.OPERATION,
			Requests.SERVICE,
			Requests.URL,
			Requests.COMPLETE_REQUEST_TEXT,
			Requests.TWEET_ID,
			Requests.SENDER_NAME,
			Requests.CREATED_AT
		};
	
	final String[] HASHTAG_PROJECTION = 
		new String[] { 
			HashTags.REQUEST_ID,
			HashTags.NAME
		};
	
	final String[] CONDITION_PROJECTION = 
		new String[] { 
			Conditions.REQUEST_ID,
			Conditions.USER_NAME,
			Conditions.VARIABLE,
			Conditions.VALUE
		};
	
	final String[] VARIABLE_PROJECTION = 
		new String[] { 
			Variables.REQUEST_ID,
			Variables.NAME,
			Variables.VALUE
		};
}
