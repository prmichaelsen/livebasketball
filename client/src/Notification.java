package com.patrickmichaelsen.livebasketball;

public class Notification{
	private String matchName;
	private String condition;

	public Notification(){}

	public void setMatchName(String matchName){ this.matchName = matchName; }
	public void setCondition(String condition){ this.condition = condition; }

	public String getCondition(String condition){ return condition; }
	public String getMatchName(String matchName){ return matchName; } 

	public String getMessage(){
		return condition + ":\n" + matchName;
	} 
} 
