package com.patrickmichaelsen.livebasketball; 

import java.util.Hashtable;
import java.lang.StringBuilder;
import java.util.Iterator;

public class Leagues{ 
	private Hashtable<String,League> leagues; 

	public Leagues(){ 
		leagues = new Hashtable<String,League>();
	};

	public Hashtable<String,League> getLeagues(){ return leagues; }
	public void setLeagues(Hashtable<String,League> leagues){
		this.leagues = leagues;
	} 

	public League get(String leagueId){ 
		return leagues.get(leagueId);
	}
	public League add(League league){ 
		return leagues.put(league.getId(), league); 
	}
	public boolean containsLeague(League league){
		return leagues.contains(league.getId());
	}

	@Override
	public String toString(){ 
		StringBuilder sb = new StringBuilder();
		if(leagues != null){
			Iterator<League> it = leagues.values().iterator();
			while(it.hasNext()){
				League league = (League)it.next();
				sb.append(league.toString());
				sb.append("\n");
			} 
		} 
		return sb.toString();
	}
}
