package com.parm.livebasketball.core;

public class Log {
	private String leagueName;
	private String leagueCountry;
	private String homeTeam;
	private String awayTeam;
	private boolean isHomeTeamWinner;

	public Log(String leagueName, String leagueCountry, String homeTeam, String awayTeam, boolean isHomeTeamWinner) {
		this.setLeagueName(leagueName);
		this.setLeagueCountry(leagueCountry);
		this.setHomeTeam(homeTeam);
		this.setAwayTeam(awayTeam);
		this.setHomeTeamWinner(isHomeTeamWinner);
	}

	public void setLeagueName(String leagueName){ this.leagueName = leagueName; }

	public String getLeagueName(){ return leagueName; }

	public String getLeagueCountry() {
		return leagueCountry;
	}

	public void setLeagueCountry(String leagueCountry) {
		this.leagueCountry = leagueCountry;
	}

	public String getHomeTeam() {
		return homeTeam;
	}

	public void setHomeTeam(String homeTeam) {
		this.homeTeam = homeTeam;
	}

	public String getAwayTeam() {
		return awayTeam;
	}

	public void setAwayTeam(String awayTeam) {
		this.awayTeam = awayTeam;
	}

	public boolean isHomeTeamWinner() {
		return isHomeTeamWinner;
	}

	public void setHomeTeamWinner(boolean homeTeamWinner) {
		isHomeTeamWinner = homeTeamWinner;
	}
}
