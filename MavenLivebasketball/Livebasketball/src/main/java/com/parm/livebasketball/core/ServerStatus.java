package com.parm.livebasketball.core;

public class ServerStatus {
	private long timestamp;

	public ServerStatus(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString(){
		return "Server Status:\nTimestamp: "+getTimestamp();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
