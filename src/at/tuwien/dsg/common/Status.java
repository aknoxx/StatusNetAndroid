package at.tuwien.dsg.common;

import java.util.Date;

public class Status {
	
	private User user;
	private String text;
	private Date date;
	private long id;
	
	public Status(User user, String text, Date date, long id) {
		this.user = user;
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

	public User getUser() {
		return user;
	}
}
