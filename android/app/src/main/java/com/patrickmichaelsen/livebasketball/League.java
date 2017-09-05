package com.patrickmichaelsen.livebasketball;

/**
 * Created by Patrick on 8/21/2017.
 */

public class League implements Comparable<League>{
    private String country;
    private String name;
    private String id;
    private boolean enabled;

    public League(){
        country = "";
        name = "";
        id = "";
        enabled = true;
    };

    public void setCountry(String country){ this.country = country; };
    public void setName(String name){ this.name = name; };
    public void setEnabled(boolean enabled){ this.enabled = enabled; }

    public String getCountry(){ return country; }
    public String getName(){ return name; }
    public String getId(){ return id = country + name; }
    public boolean getEnabled(){ return enabled; }

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
        return this.getId() + ": " + ((enabled)? "Enabled" : "Disabled");
    }
}

