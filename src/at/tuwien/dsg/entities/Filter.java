package at.tuwien.dsg.entities;

public class Filter {

	// change for git :)
	private String name;
	private String pattern;
	
	public Filter() {
	}
	
	public Filter(String name, String pattern) {
		super();
		this.name = name;
		this.pattern = pattern;
	}
	
	public void setRegexFromString(String word) {
		this.pattern = "\\.*" + word + "\\.*";
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
