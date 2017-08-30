package com.patrickmichaelsen.livebasketball;

public class League implements Comparable<League>{
	private String country;
	private String name;
	private String id;
	private boolean active;

	public League(){
		country = "";
		name = "";
		id = "";
		active = false; 
	};

	public void enable(){ this.active = true; };
	public void disable(){ this.active = false; };

	public void setCountry(String country){ this.country = country; };
	public void setName(String name){ this.name = name; };
	public void setId(String id){ this.id = id; }
	public void setEnabled(boolean active){ this.active = active; }

	public String getCountry(){ return country; }
	public String getName(){ return name; }
	public String getId(){ return id = country + name; }
	public boolean getEnabled(){ return active; }

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
		return this.getId() + ": " + ((active)? "Enabled" : "Disabled");
	}
}
