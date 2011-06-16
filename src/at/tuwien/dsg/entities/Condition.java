package at.tuwien.dsg.entities;

import java.io.Serializable;

public class Condition implements Serializable {

	private static final long serialVersionUID = 391086674453868361L;
	
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
