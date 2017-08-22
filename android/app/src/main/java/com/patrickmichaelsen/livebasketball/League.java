package com.patrickmichaelsen.livebasketball;

/**
 * Created by Patrick on 8/21/2017.
 */

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

    public String getCountry(){ return country; }
    public String getName(){ return name; }
    public String getId(){ return id = country + name; }

    @Override
    public int compareTo(League league){
        int countryCompare = this.country.compareTo(league.getCountry());
        if(countryCompare == 0){
            return this.name.compareTo(league.getName());
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

