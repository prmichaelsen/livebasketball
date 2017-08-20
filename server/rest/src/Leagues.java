package dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Hashtable;
import java.util.Enumeration;
import java.lang.StringBuilder;

@XmlRootElement
public class Leagues{ 
	private Hashtable<String,League> leagues; 

	public Leagues(){ 
		leagues = new Hashtable<String,League>();
	};

	public Hashtable<String,League> getLeagues(){ return leagues; }
	public void setLeagues(Hashtable<String,League> leagues){
		this.leagues = leagues;
	} 

	@Override
	public String toString(){ 
		StringBuilder sb = new StringBuilder();
		if(leagues != null){
			Enumeration e = leagues.elements(); 
			while(e.hasMoreElements()){
				League league = (League)e.nextElement();
				sb.append(league.toString());
				sb.append("\n");
			} 
		} 
		return sb.toString();
	}
}
