package com.parm.livebasketball.core;

public class ExpoNotification {
	private String to;
	private String title;
	private String body;

	public ExpoNotification(String to, String title, String body) {
		this.to = to;
		this.title = title;
		this.body = body;
	}

	public void setTo(String to){ this.to = to; }
	public void setTitle(String title){ this.title = title; }
	public void setBody(String body){ this.body = body; }

	public String getTo(){ return to; }
	public String getTitle(){ return title; }
	public String getBody(){ return body; }
}
