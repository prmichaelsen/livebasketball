package dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class League implements Comparable<League>{
	private String country;
	private String name;
	private String id;
	private boolean active;

	public League(){
		country = "";
		name = "";
		id = "";
		active = true; 
	};

	public void enable(){ this.active = true; };
	public void disable(){ this.active = false; };

	public void setCountry(String country){ this.country = country; };
	public void setName(String name){ this.name = name; };

	public boolean getActive(){ return active; }
	public String getCountry(){ return country; }
	public String getName(){ return name; }
	public String getId(){ return (id != "") ? id : country + name; }

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
