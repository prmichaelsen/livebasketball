package com.patrickmichaelsen.livebasketball; 

import java.util.Hashtable;

public class Leagues{ 
	private Hashtable<String,League> leagues; 

	public Leagues(){ 
		leagues = new Hashtable<String,League>();
	};

	public Hashtable<String,League> getLeagues(){ return leagues; }
	public void setLeagues(Hashtable<String,League> leagues){
		this.leagues = leagues;
	} 
}
