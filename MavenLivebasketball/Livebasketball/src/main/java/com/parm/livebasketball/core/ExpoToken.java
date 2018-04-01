package com.parm.livebasketball.core;

public class ExpoToken {
	private String ExponentPushToken;

	public ExpoToken(String ExponentPushToken) {
		this.ExponentPushToken = ExponentPushToken;
	}

	public void setExponentPushToken(String ExponentPushToken){ this.ExponentPushToken = ExponentPushToken; }

	public String getExponentPushToken(){ return ExponentPushToken; }
}
