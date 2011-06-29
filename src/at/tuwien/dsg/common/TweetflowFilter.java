package at.tuwien.dsg.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.tuwien.dsg.entities.Condition;
import at.tuwien.dsg.entities.OperationExecutionStatus;
import at.tuwien.dsg.entities.Request;

public class TweetflowFilter {
	
	private SimpleDateFormat twitterDate = new SimpleDateFormat(
            "EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
	
	private final String requestPatternString = 
		"(\\[)?" +	// optional begin of closed sequence
		"[A-Z]{2}" +
		"( @\\w+)?" +						// optional @addressedUser
		" \\w+\\.\\w+" +						// operation.service
		"( http://[\\S\\./]+)?" +			// optional url
		"(\\?\\w+=\\S+(&\\w+=\\S+)+)?" +	// optional querystring
		"( \\[@\\w+\\.\\w+\\?=\\w+\\])?" +	// optional condition
		"( #\\w+)*" +	// optional #hashtags
		"( \\|)?" + 	// optional pipe
		"( \\])?";		// optional end of closed sequence
	
	private final String variableAssignmentString = 
					"VA " +
					"\\S+ " +	// varname
					"\\S+";		// value
	
	private final static String operationServiceAlone = " \\S+\\.\\w+";
	
	private static Pattern requestPattern;
	private static Pattern hashTagPattern;
	private static Pattern urlPattern;
	private static Pattern userPattern;
	private static Pattern queryStringPattern;
	private static Pattern operationServicePattern;
	private static Pattern operationServiceAlonePattern;
	private static Pattern logServiceExecutionStatusPattern;
	private static Pattern conditionPattern;
	
	private static Pattern variableAssignmentPattern;
	private static Pattern variableAccessPattern;
	private static Pattern serviceResultAccessPattern;
	
	private List<Request> results = new ArrayList<Request>();
	
	private static boolean closedSequenceStarted = false;
	private static long predecessorWithPipeId = 0;
	private static List<Request> closedSequenceRequests = new ArrayList<Request>();
	
	public TweetflowFilter() {
		requestPattern = Pattern.compile(requestPatternString);
		hashTagPattern = Pattern.compile("#\\w+");
		// \S A non-whitespace character: [^\s]
		// http://[\\S\\./]+
		// http://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/[\\S]+)*
		urlPattern = Pattern.compile("http://[\\S\\./]+");
		userPattern = Pattern.compile("@\\w+");
		// location=Vienna,1020&date=today&time=20:00
		queryStringPattern = Pattern.compile("\\?\\w+=\\S+(&\\w+=\\S+)+( )?");
		
		// TODO what if no " " nor ? ??
		//operationServicePattern = Pattern.compile(" \\S+\\.\\w+[ ?]?"); // ending with " " followed by a url or with "?" followed by a querystring
		
		// e.g.: " recommend.restaurant?" or " get.Availability "
		operationServicePattern = Pattern.compile(operationServiceAlone + "[ \\?]");
		// e.g.: " get.Availability"
		operationServiceAlonePattern = Pattern.compile(operationServiceAlone);
		logServiceExecutionStatusPattern = Pattern.compile("did(Begin|Finish|Cancle)\\w*");
		
		conditionPattern = Pattern.compile("\\[@\\w+\\.\\w+\\?=\\w+\\]");
		
		variableAssignmentPattern = Pattern.compile(variableAssignmentString);
		variableAccessPattern = Pattern.compile("@\\w+\\.\\w+\\?");
		serviceResultAccessPattern = Pattern.compile("@\\w+\\.\\w+\\.\\w+\\?");
	}
	
	public List<Request> extractRequestFromBloaStatus(ConnManager.UserStatus status) throws ParseException {
		final String statusText = status.getText();
		Request request = new Request();
		
		request.setClosedSequence(closedSequenceStarted);
		if(!closedSequenceStarted) {
			predecessorWithPipeId = 0;
		}
		
		// SR, SF, TF, LG
		Matcher matcher = requestPattern.matcher(statusText);
		// TODO -> do this in loop to find more requests in same tweets ?!?
		if(matcher.find()) { 
		
			final String requestText = matcher.group();
			
			if(requestText.startsWith("[")) {
				closedSequenceStarted = true;
				request.setClosedSequence(true);
				request.setQualifier(requestText.substring(1, 3));
			}
			else {
				request.setQualifier(requestText.substring(0, 2));
			}
			
			request.setCompleteRequestText(requestText);
		
			matcher = userPattern.matcher(requestText);
			if(matcher.find()) {	// optional
				request.setAddressedUser(new String(matcher.group().substring(1)));
			}		
			
			matcher = operationServicePattern.matcher(requestText);
			if(matcher.find()) {
				String[] operationService = matcher.group().split("\\.");
				// trim to remove the preceding " "
				request.setOperation(operationService[0].trim());
				
				if(request.getQualifier().equals("LG")) {
					matcher = logServiceExecutionStatusPattern.matcher(request.getOperation());
					if(matcher.find()) {
						String SEStatus = matcher.group().substring(3);
						
						if(SEStatus.startsWith(OperationExecutionStatus.Begin.toString())) {
							request.setOperationExecutionStatus(OperationExecutionStatus.Begin.toString());
						}
						else if(SEStatus.startsWith(OperationExecutionStatus.Cancel.toString())) {
							request.setOperationExecutionStatus(OperationExecutionStatus.Cancel.toString());
						}
					}
					else {
						request.setOperationExecutionStatus(OperationExecutionStatus.Finish.toString());
					}
				}	
				
				// removing the " " or "?" following the service
				//request.setService(operationService[1].substring(0, operationService[1].length()-1));
				
				// TODO ...?dat=27 wtf????
				String service = operationService[1].trim();
				if(service.endsWith("?")) {
					request.setService(operationService[1].substring(0, operationService[1].length()-1));
				}
				else {
					request.setService(service);
				}
				
			}
			else {
				matcher = operationServiceAlonePattern.matcher(requestText);
				if(matcher.find()) {
					String[] operationService = matcher.group().split("\\.");
					// trim to remove the preceding " "
					request.setOperation(operationService[0].trim());
					
					if(request.getQualifier().equals("LG")) {
						matcher = logServiceExecutionStatusPattern.matcher(request.getOperation());
						if(matcher.find()) {
							String SEStatus = matcher.group().substring(3);
							
							if(SEStatus.startsWith(OperationExecutionStatus.Begin.toString())) {
								request.setOperationExecutionStatus(OperationExecutionStatus.Begin.toString());
							}
							else if(SEStatus.startsWith(OperationExecutionStatus.Cancel.toString())) {
								request.setOperationExecutionStatus(OperationExecutionStatus.Cancel.toString());
							}
						}
						else {
							request.setOperationExecutionStatus(OperationExecutionStatus.Finish.toString());
						}
					}					
					
					// removing the " " or "?" following the service
					//request.setService(operationService[1].substring(0, operationService[1].length()-1));
					
					// TODO ...?dat=27 wtf????
					String service = operationService[1].trim();
					request.setService(service);
					
				}
				else {
					reset();
					return null;
				}				
			}
			
			matcher = urlPattern.matcher(requestText);
			if(matcher.find()) {
				request.setUrl(matcher.group());
			}
			
			matcher = queryStringPattern.matcher(requestText);
			if(matcher.find()) {
				// remove leading "?" and possible trailing blank
				String queryString = matcher.group().substring(1).trim();
				String[] queryArguments = queryString.split("&");
				
				for (String arg : queryArguments) {
					String[] assignment = arg.split("=");
					request.getVariables().put(assignment[0], assignment[1]);
				}
			}
			
			if(request.getQualifier().equals("SR")
					|| request.getQualifier().equals("SF")) {
				// TODO 
				
				// only url or querystring allowed
				//if(request.getUrl()==null && request.getVariables().size()==0
				//		|| request.getUrl()!=null && request.getVariables().size()>0) {
//				if(request.getUrl()!=null && request.getVariables().size()>0) {
//					reset();
//					return null;
//				}
			}
			
			// e.g. [@ikangai.availability?=true]
			matcher = conditionPattern.matcher(requestText);
			if(matcher.find()) {	// optional
				String[] condition = matcher.group().split("=");			
				String[] userVariable = condition[0].split("\\.");
				
				Condition con = new Condition(
						userVariable[0].substring(2),	// remove [@
						userVariable[1].substring(0, userVariable[1].length()-1), // remove ?
						condition[1].substring(0, condition[1].length()-1)); // remove ]
				request.setCondition(con);
			}
			
			matcher = hashTagPattern.matcher(requestText);
			// Find all matches
			while (matcher.find()) { 	// optional
				// Get the matching string
				String match = matcher.group();
				request.getHashTags().add(match);
			}
			
			request.setTweetId(status.getId());
			request.setCreatedAt(twitterDate.parse(status.getCreatedAt()));
			request.setRequester(status.getUserName());
			
			if(closedSequenceStarted) {
				if(predecessorWithPipeId == 0) {	
					request.setPredecessorTweetId(0);
				}
				else {
					request.setPredecessorTweetId(predecessorWithPipeId);
				}
			}
			
			if(closedSequenceStarted) {
				if(requestText.endsWith("|")) {
					
					
					// TODO tweetId or dbId????? dont know dbId yet and what if both parts in same tweet?
					// -> tweetId impossible too...
					// HA! -> do it in TweetFlowManager!!!!
					
					// save tweetId for following requests
					predecessorWithPipeId = request.getTweetId();
					
				}
				else if(requestText.endsWith("]")) {
					
					closedSequenceStarted = false;
					predecessorWithPipeId = 0;
				}
			}			
			
			results.clear();
			results.add(request);
			return results;
		}
		
		// VA ExplicitVariableAssignment
		// VA varname value
		matcher = variableAssignmentPattern.matcher(statusText);	
		if(matcher.find()) {
			final String requestText = matcher.group();
			
			request.setQualifier(requestText.substring(0, 2));
			request.setCompleteRequestText(requestText);
			
			String[] parts = requestText.split(" ");
			request.getVariables().put(parts[1], parts[2]);			

			request.setTweetId(status.getId());
			request.setCreatedAt(twitterDate.parse(status.getCreatedAt()));
			request.setRequester(status.getUserName());	

			results.clear();
			results.add(request);
			return results;
		}
		
		// @user.varname?
		// Access variable (will be created if not existent)
		matcher = variableAccessPattern.matcher(statusText);	
		if(matcher.find()) {
			final String requestText = matcher.group();
			
			request.setQualifier("AccessVariable");
			request.setCompleteRequestText(requestText);
			
			String[] parts = requestText.split("\\.");
			request.setAddressedUser(new String(parts[0].substring(1)));	// remove @
			request.getVariables().put(parts[1].substring(0, parts[1].length()-1), null); // remove ?
			
			request.setTweetId(status.getId());
			request.setCreatedAt(twitterDate.parse(status.getCreatedAt()));
			request.setRequester(status.getUserName());		

			results.clear();
			results.add(request);
			return results;
		}
		
		//@user.operation.Service?	
		//Access service results (Implicit) / ask user to retweet results
		matcher = serviceResultAccessPattern.matcher(statusText);	
		if(matcher.find()) {
			final String requestText = matcher.group();
			
			request.setQualifier("AccessServiceResult");
			request.setCompleteRequestText(requestText);
			
			String[] parts = requestText.split("\\.");
			request.setAddressedUser(new String(parts[0].substring(1)));	// remove @
			request.setOperation(parts[1]);
			request.setService(parts[2].substring(0, parts[2].length()-1)); // remove ?			

			request.setTweetId(status.getId());
			request.setCreatedAt(twitterDate.parse(status.getCreatedAt()));
			request.setRequester(status.getUserName());		

			results.clear();
			results.add(request);
			return results;
		}
		reset();
		return null;
	}

	public Request extractRequest(Status status) {
		final String statusText = status.getText();
		Request request = new Request();
		
		// SR, SF, TF, LG
		Matcher matcher = requestPattern.matcher(statusText);		
		if(matcher.find()) {
		
			final String requestText = matcher.group();
			
			request.setQualifier(requestText.substring(0, 2));
			request.setCompleteRequestText(requestText);
		
			matcher = userPattern.matcher(requestText);
			if(matcher.find()) {	// optional
				request.setAddressedUser(new String(matcher.group().substring(1)));
			}		
			
			matcher = operationServicePattern.matcher(requestText);
			if(matcher.find()) {
				String[] operationService = matcher.group().trim().split("\\.");
				request.setOperation(operationService[0]);
				request.setService(operationService[1]);
			}
			else {
				reset();
				return null;
			}
			
			matcher = urlPattern.matcher(requestText);
			if(matcher.find()) {
				request.setUrl(matcher.group());
			}
			
			matcher = queryStringPattern.matcher(requestText);
			if(matcher.find()) {
				String[] queryArguments = matcher.group().split("&");
				
				for (String arg : queryArguments) {
					String[] assignment = arg.split("=");
					request.getVariables().put(assignment[0], assignment[1]);
				}
			}
			
			if(request.getQualifier().equals("SR")
					|| request.getQualifier().equals("SF")) {
				// only url or querystring allowed
				if(request.getUrl()==null && request.getVariables().size()==0
						|| request.getUrl()!=null && request.getVariables().size()>0) {
					reset();
					return null;
				}
			}
			
			// e.g. [@ikangai.availability?=true]
			matcher = conditionPattern.matcher(requestText);
			if(matcher.find()) {	// optional
				String[] condition = matcher.group().split("=");			
				String[] userVariable = condition[0].split("\\.");
				
				Condition con = new Condition(
						userVariable[0].substring(2),	// remove [@
						userVariable[1].substring(0, userVariable[1].length()-1), // remove ?
						condition[1].substring(0, condition[1].length()-1)); // remove ]
				request.setCondition(con);
			}
			
			matcher = hashTagPattern.matcher(requestText);
			// Find all matches
			while (matcher.find()) { 	// optional
				// Get the matching string
				String match = matcher.group();
				request.getHashTags().add(match);
			}
			
			request.setTweetId(status.getId());
			request.setCreatedAt(status.getCreatedAt());
			request.setRequester(status.getSender());		

			return request;
		}
		
		// VA ExplicitVariableAssignment
		// VA varname value
		matcher = variableAssignmentPattern.matcher(statusText);	
		if(matcher.find()) {
			final String requestText = matcher.group();
			
			request.setQualifier(requestText.substring(0, 2));
			request.setCompleteRequestText(requestText);
			
			String[] parts = requestText.split(" ");
			request.getVariables().put(parts[1], parts[2]);			

			request.setTweetId(status.getId());
			request.setCreatedAt(status.getCreatedAt());
			request.setRequester(status.getSender());		

			return request;
		}
		
		// @user.varname?
		// Access variable (will be created if not existent)
		matcher = variableAccessPattern.matcher(statusText);	
		if(matcher.find()) {
			final String requestText = matcher.group();
			
			request.setQualifier("AccessVariable");
			request.setCompleteRequestText(requestText);
			
			String[] parts = requestText.split("\\.");
			request.setAddressedUser(new String(parts[0].substring(1)));	// remove @
			request.getVariables().put(parts[1].substring(0, parts[1].length()-1), null); // remove ?
			
			request.setTweetId(status.getId());
			request.setCreatedAt(status.getCreatedAt());
			request.setRequester(status.getSender());		

			return request;
		}
		
		//@user.operation.Service?	
		//Access service results (Implicit) / ask user to retweet results
		matcher = serviceResultAccessPattern.matcher(statusText);	
		if(matcher.find()) {
			final String requestText = matcher.group();
			
			request.setQualifier("AccessServiceResult");
			request.setCompleteRequestText(requestText);
			
			String[] parts = requestText.split("\\.");
			request.setAddressedUser(new String(parts[0].substring(1)));	// remove @
			request.setOperation(parts[1]);
			request.setService(parts[2].substring(0, parts[2].length()-1)); // remove ?			

			request.setTweetId(status.getId());
			request.setCreatedAt(status.getCreatedAt());
			request.setRequester(status.getSender());		

			return request;
		}
		reset();
		return null;
	}
	
	private void reset() {
		closedSequenceRequests.clear();
		closedSequenceStarted = false;
	}
}
