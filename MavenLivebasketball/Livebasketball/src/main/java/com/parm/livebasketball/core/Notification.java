package com.parm.livebasketball.core;

public class Notification{
	private String matchName;
	private String condition;
	private String id;

	public Notification(){}
	public Notification( String matchName, String condition){
		this.matchName = matchName;
		this.condition = condition;
	}

	public String hashId(){ return id = matchName + ": " +  condition; }

	public void setMatchName(String matchName){ this.matchName = matchName; }
	public void setCondition(String condition){ this.condition = condition; }
	public void setId(String id){ this.id = id; }

	public String getCondition(){ return condition; }
	public String getMatchName(){ return matchName; } 
	public String getId(){ return id; } 

	public String toMessage(){
		return condition + ":\n" + matchName;
	} 
} 
