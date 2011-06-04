package at.tuwien.dsg.entities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.tuwien.dsg.common.Status;
import at.tuwien.dsg.common.User;

public class TweetflowPrimitive {

	private String qualifier;
	private String name;
	private String description;
	private String pattern;
	private String url;
	
	private static Pattern requestPattern;
	private static Pattern hashTagPattern;
	private static Pattern urlPattern;
	private static Pattern userPattern;
	private static Pattern queryStringPattern;
	private static Pattern operationServicePattern;
	private static Pattern conditionPattern;
	
	public TweetflowPrimitive() {
		hashTagPattern = Pattern.compile("#\\w+");
		// \S A non-whitespace character: [^\s]
		// http://[\\S\\./]+
		// http://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/[\\S]+)*
		urlPattern = Pattern.compile("http://[\\S\\./]+");
		userPattern = Pattern.compile("@\\w+");
		// location=Vienna,1020&date=today&time=20:00
		queryStringPattern = Pattern.compile("\\w+=\\S+(&\\w+=\\S+)+");
		operationServicePattern = Pattern.compile(" \\S+\\.\\S+ ");
		conditionPattern = Pattern.compile("\\[@\\w+\\.\\w+\\?=\\w+\\]");
	}
	
	public TweetflowPrimitive(String qualifier, String name,
			String description, String pattern) {
		this();
		requestPattern = Pattern.compile(pattern);
		this.qualifier = qualifier;
		this.name = name;
		this.description = description;
		this.pattern = pattern;
	}

	public Request extractRequest(Status status) {
		final String text = status.getText();
		Request request = new Request();
		request.setQualifier(qualifier);
		
		if(qualifier.equals("SR") || qualifier.equals("SF")) {
			Matcher requestMatcher = requestPattern.matcher(text);
			if(!requestMatcher.matches()) {
				return null;
			}
			
			Matcher matcher = userPattern.matcher(text);
			if(matcher.find()) {	// optional
				request.setAddressedUser(new User(matcher.group().substring(1)));
			}		
			
			matcher = operationServicePattern.matcher(status.getText());
			if(matcher.find()) {
				String[] operationService = matcher.group().trim().split("\\.");
				request.setOperation(operationService[0]);
				request.setService(operationService[1]);
			}
			else {
				return null;
			}
			
			matcher = urlPattern.matcher(text);
			if(matcher.find()) {
				request.setUrl(matcher.group());
			}
			
			matcher = queryStringPattern.matcher(text);
			if(matcher.find()) {
				String[] queryArguments = matcher.group().split("&");
				
				for (String arg : queryArguments) {
					String[] assignment = arg.split("=");
					request.getVariables().put(assignment[0], assignment[1]);
				}
			}
			
			// only url or querystring allowed
			if(request.getUrl()==null && request.getVariables().size()==0
					|| request.getUrl()!=null && request.getVariables().size()>0) {
				return null;
			}
			
			// e.g. [@ikangai.availability?=true]
			matcher = conditionPattern.matcher(text);
			if(matcher.find()) {	// optional
				String[] condition = matcher.group().split("=");			
				String[] userVariable = condition[0].split("\\.");
				
				Condition con = new Condition(
						userVariable[0].substring(2),	// remove [@
						userVariable[1].substring(0, userVariable[1].length()-1), // remove ?
						condition[1].substring(0, condition[1].length()-1)); // remove ]
				request.setCondition(con);
			}
			
			matcher = hashTagPattern.matcher(text);
			// Find all matches
			while (matcher.find()) { 	// optional
				// Get the matching string
				String match = matcher.group();
				request.getHashTags().add(match);
			}
			
			request.setTweetId(status.getId());
			request.setCreatedAt(status.getCreatedAt());
			request.setRequester(status.getUser().getName());		
		}

		return request;
	}
	
	public String getSymbol() {
		return qualifier;
	}
	public void setQualifier(String symbol) {
		this.qualifier = symbol;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}



	public void setUrl(String url) {
		this.url = url;
	}



	public String getUrl() {
		return url;
	}
}
