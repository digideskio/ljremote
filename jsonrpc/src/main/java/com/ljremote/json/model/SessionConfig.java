package com.ljremote.json.model;

public class SessionConfig {
	int id;
	long timeOut;

	public SessionConfig() {
	}
	
	public SessionConfig(int id, long timeOut) {
		super();
		this.id = id;
		this.timeOut = timeOut;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

}