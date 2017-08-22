package com.patrickmichaelsen.livebasketball;

import java.util.Hashtable;

/**
 * Created by Patrick on 8/21/2017.
 */

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

