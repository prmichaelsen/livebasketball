package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patrickmichaelsen.livebasketball.League;
import com.patrickmichaelsen.livebasketball.Leagues;
import dto.Response;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader; 
import java.io.Reader; 
import java.util.Iterator; 
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/leagues")
public class LeaguesSvc {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Leagues getLeagues() { 
		System.out.println("Received request: getLeagues()");
		Leagues leagues = new Leagues();
		//read leagues from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
		try (Reader reader = new FileReader("../../server/data/leagues.json")) { 
			// Convert JSON to Java Object
			leagues = gson.fromJson(reader, Leagues.class);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return leagues;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response<String> postLeagues(League league) {
		System.out.println(league);
		Leagues leagues = new Leagues();

		//read leagues from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
		try (Reader reader = new FileReader("../../server/data/leagues.json")) { 
			// Convert JSON to Java Object
			leagues = gson.fromJson(reader, Leagues.class);
		} catch (IOException e) {
			e.printStackTrace();
		} 

		//add or update new league
		if( league.getId() != null && !league.getId().equals("") ){
			leagues.getLeagues().put(league.getId(), league); 
			if( league.getId().indexOf('#') != -1 ){
				boolean active = league.getEnabled();
				Iterator<League> it = leagues.getLeagues().values().iterator();	
				while(it.hasNext()){ 
					it.next().setEnabled(active);		
				}
				//save leagues to file
				try (FileWriter writer = new FileWriter("../../server/data/leagues.json")) { 
					gson.toJson(leagues, writer); 
				} catch (IOException e) {
					e.printStackTrace(); 
				} 
				if(active){ 
					return new Response<String>("Succesfully enabled notifications for all leagues");
				}
				else{ 
					return new Response<String>("Succesfully disabled notifications for all leagues");
				}
			}
		} 
		else{
			return new Response<String>("Could not update league settings because there was no valid league id");
		} 

		//save leagues to file
		try (FileWriter writer = new FileWriter("../../server/data/leagues.json")) { 
			gson.toJson(leagues, writer); 
		} catch (IOException e) {
			e.printStackTrace(); 
		} 

		return new Response<String>("Succesfully updated league settings: " + league);
	} 
} 

