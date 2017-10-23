package com.parm.livebasketball.core;

public class League implements Comparable<League>{
	private String country;
	private String name;
	private String id;

	public League(String country, String name, String id) {
		this.country = country;
		this.name = name;
		this.id = id;
	}

	public League(){
		country = "";
		name = "";
		id = "";
	}

	public void setCountry(String country){ this.country = country; }
	public void setName(String name){ this.name = name; }
	public void setId(String id){ this.id = id; }

	public String getCountry(){ return country; }
	public String getName(){ return name; }
	public String getId(){ return id; }

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
