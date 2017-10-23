package com.parm.livebasketball.core;

import com.google.gson.internal.LinkedTreeMap;
import java.lang.StringBuilder;
import java.util.Iterator;
import java.util.Map;

public class Leagues{ 
	private LinkedTreeMap<String,League> leagues;

	public Leagues(){ 
		leagues = new LinkedTreeMap<String,League>();
	};

	public LinkedTreeMap<String,League> getLeagues(){ return leagues; }
	public void setLeagues(Map<String,League> leagues){
		if(leagues != null)
			this.leagues = (LinkedTreeMap) leagues;
	} 

	public League get(String leagueId){ 
		return leagues.get(leagueId);
	}
	public League add(League league){ 
		return leagues.put(league.getId(), league); 
	}
	public boolean containsLeague(League league){
		return leagues.containsKey(league.getId());
	}

	@Override
	public String toString(){ 
		StringBuilder sb = new StringBuilder();
		if(leagues != null){
			Iterator<League> it = leagues.values().iterator();
			while(it.hasNext()){
				League league = it.next();
				sb.append(league.toString());
				sb.append("\n");
			} 
		} 
		return sb.toString();
	}
}
