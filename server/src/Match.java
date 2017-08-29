package com.patrickmichaelsen.livebasketball;

import java.util.List;
import java.util.ArrayList;

public class Match implements Comparable<Match>{
	private String homeTeam;
	private String awayTeam;
	private List<Integer> homeScores;
	private List<Integer> awayScores; 
	private String id;
	private String leagueId;
	private String roundStatus;
	private int time;
	private boolean conditionOneMet;
	private boolean conditionTwoMet;
	private boolean potentialMatch;
	private long lastUpdated;

	public Match(String id, String leagueId){
		this.id = id;
		this.leagueId = leagueId;
		homeScores = new ArrayList<Integer>();
		awayScores = new ArrayList<Integer>(); 
		homeTeam = "";
		awayTeam = ""; 
		roundStatus = ""; 
		time = 0;
		conditionOneMet = false;
		conditionTwoMet = false;
		potentialMatch = true;
		lastUpdated = 0;
	}

	public void setId(String id){ this.id = id; }

	public void setHomeScores(List<Integer> scores){
		this.homeScores = scores; 
	}

	public void setAwayScores(List<Integer> scores){
		this.awayScores = scores; 
	}

	public void setHomeTeam(String name){
		this.homeTeam = name; 
	}

	public void setAwayTeam(String name){
		this.awayTeam = name; 
	}

	public void setLeagueId(String leagueId){
		this.leagueId = leagueId;
	}

	public void setRoundStatus(String status){
		if(status.contains("Quarter")){
			if(status.contains("1st")){
				this.roundStatus = "1st Quarter"; 
			}
			if(status.contains("2nd")){
				this.roundStatus = "2nd Quarter"; 
			}
			if(status.contains("3rd")){
				this.roundStatus = "3rd Quarter"; 
			}
			if(status.contains("4th")){
				this.roundStatus = "4th Quarter"; 
			}
			try{
				String time = status.substring(status.indexOf(";")+1, status.indexOf("<span"));
				if(time != null){
					this.time = Integer.parseInt(time); 
				}
			}catch(StringIndexOutOfBoundsException e){ }
			catch(NumberFormatException e){}; 
		} else if (status.contains("Half Time")){
			this.roundStatus = "Half Time"; 
		} else if (status.contains("Finished")){
			this.roundStatus = "Finished"; 
		} else if (status.contains("Overtime")){
			this.roundStatus = "Overtime"; 
			try{
				String time = status.substring(status.indexOf(";")+1, status.indexOf("<span"));
				if(time != null){
					this.time = Integer.parseInt(time); 
				}
			}catch(StringIndexOutOfBoundsException e){ }
			catch(NumberFormatException e){}; 
		}
	} 

	public void setLastUpdated(long timeInMillis){
		this.lastUpdated = timeInMillis;
	} 

	public String getHomeTeam(){ return homeTeam; }
	public String getAwayTeam(){ return awayTeam; }

	public long getLastUpdated(){ 
		return lastUpdated;
	}

	public String getId(){ return id; }

	public String getLeagueId(){ return leagueId; }

	public boolean doesMeetConditionOne(){
		boolean hasPositive = false;
		boolean hasNegative = false;
		boolean hasZero = false; 
		for(int j = 0; j < homeScores.size(); j++){
			int diff = homeScores.get(j) - awayScores.get(j); 
			if(diff > 0){
				hasPositive = true;		
			}
			else if(diff < 0){
				hasNegative = true; 
			} 
			else {
				hasZero = true;
			}
		} 
		if( (!hasPositive || !hasNegative)  && !hasZero ){ 
			if( roundStatus.equals("3rd Quarter") && time == 10){
				if(!conditionOneMet){
					return conditionOneMet = true;
				}
			} 
		}
		return false; 
	}

	public boolean doesMeetConditionTwo(){
		boolean hasPositive = false;
		boolean hasNegative = false;
		boolean hasZero = false; 
		for(int j = 0; j < homeScores.size() - 1; j++){
			int diff = homeScores.get(j) - awayScores.get(j); 
			if(diff > 0){
				hasPositive = true;		
			}
			else if(diff < 0){
				hasNegative = true; 
			} 
			else {
				hasZero = true;
			}
		} 
		if( (!hasPositive || !hasNegative)  && !hasZero ){ 
			if( roundStatus.equals("4th Quarter") && time == 1){
				if(!conditionTwoMet){
					return conditionTwoMet = true;
				}
			} 
		}
		return false;
	} 

	public String getMatchName(){
		return homeTeam + " vs. " + awayTeam; 
	}

	public String getCondition(){
		if(conditionTwoMet){ 
			return "Round 4 Starting with 3-Round-Favor";
		}
		else if(conditionOneMet){
			return "Round 3 Ending with 3-Round-Favor";
		}
		return "";
	} 

	public String getMessage(){
		if(conditionTwoMet){ 
			return "Round 4 Starting with 3-Round-Favor:\n"+this.getMatchName();
		}
		else if(conditionOneMet){
			return "Round 3 Ending with 3-Round-Favor:\n"+this.getMatchName();
		}
		return "";
	} 

	public String toString(){
		return 
			((conditionOneMet | conditionTwoMet)? " * " : "") + 
			this.getMatchName() + "\n" +
			roundStatus + " - " + time + "'\n" +
			homeTeam + ": " + homeScores + "\n" + 
			awayTeam + ": " + awayScores + "\n" +
			"League" + ": " + leagueId + "\n";
	} 


	@Override
	public int compareTo(Match match){ 
		return (int) (this.lastUpdated - match.getLastUpdated());
	}

} 
