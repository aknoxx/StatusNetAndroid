package at.tuwien.dsg.entities;

public class Condition {

	private String username;
	private String variable;
	private String value;
	
	public Condition() {
		
	}
	
	public Condition(String username, String variable, String value) {
		super();
		this.username = username;
		this.variable = variable;
		this.value = value;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
