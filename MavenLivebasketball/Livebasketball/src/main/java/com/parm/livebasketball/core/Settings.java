package com.parm.livebasketball.core;

public class Settings {
	private boolean enableLeaguesByDefault;

	public Settings(boolean enableLeaguesByDefault) {
		this.setEnableLeaguesByDefault(enableLeaguesByDefault);
	}


	public boolean isEnableLeaguesByDefault() {
		return enableLeaguesByDefault;
	}

	public void setEnableLeaguesByDefault(boolean enableLeaguesByDefault) {
		this.enableLeaguesByDefault = enableLeaguesByDefault;
	}
}
