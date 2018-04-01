package com.parm.livebasketball.core;

public class League implements Comparable<League>{
	private String country;
	private String name;
	private String id;
	private boolean enabled;

	public League(String country, String name, String id) {
		this.country = country;
		this.name = name;
		this.id = id;
		this.enabled = true;
	}

	public League(){
		country = "";
		name = "";
		id = "";
		enabled = true;
	}

	public String createId() { return this.id = this.country + this.name; }

	public void setCountry(String country){ this.country = country; }
	public void setName(String name){ this.name = name; }
	public void setId(String id){ this.id = id; }
	public void setEnabled(boolean enabled){ this.enabled = enabled; }

	public String getCountry(){ return country; }
	public String getName(){ return name; }
	public String getId(){ return id; }
	public boolean getEnabled(){ return enabled; }

	@Override
	public int compareTo(League league){
		int countryCompare = this.country.compareTo(league.country);
		if(countryCompare == 0){
			return this.name.compareTo(league.name);
		}
		else{ 
			return countryCompare;
		}
	}

	@Override
	public String toString(){
		return country + name;
	}
} 
