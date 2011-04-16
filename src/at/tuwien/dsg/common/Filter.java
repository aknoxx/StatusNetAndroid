package at.tuwien.dsg.common;

public class Filter {

	private String name;
	private String regex;
	
	public Filter() {
	}
	
	public Filter(String name, String regex) {
		super();
		this.name = name;
		this.regex = regex;
	}
	
	public void setRegexFromString(String word) {
		this.regex = "\\.*" + word + "\\.*";
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
}
