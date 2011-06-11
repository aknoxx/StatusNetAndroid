package at.tuwien.dsg.common;

import java.util.Date;

public class Status {
	
	private String sender;
	private String text;
	private Date date;
	private long id;
	
	public Status(String sender, String text, Date date, long id) {
		this.sender = sender;
		this.text = text;
		this.date = date;
		this.id = id;
	}
	
	public Date getCreatedAt() {
		return date;
	}

	public long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getSender() {
		return sender;
	}
}
