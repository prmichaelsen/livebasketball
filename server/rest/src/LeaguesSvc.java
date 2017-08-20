package rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dto.Response;
import dto.Leagues;
import dto.League;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader; 
import java.io.Reader; 

@Path("/leagues")
public class LeaguesSvc {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response<Leagues> getLeagues() { 
		Leagues leagues = new Leagues();
		//read leagues from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
		try (Reader reader = new FileReader("../data/leagues.json")) { 
			// Convert JSON to Java Object
			leagues = gson.fromJson(reader, Leagues.class);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		Response<Leagues> response = new Response<Leagues>();
		response.setReturnData(leagues);
		return response;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response<String> postLeagues(League league) {
		System.out.println("Output json server .... \n");
		System.out.println(league);
		Leagues leagues = new Leagues();

		java.nio.file.Path currentRelativePath = java.nio.file.Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + s);

		//read leagues from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
		try (Reader reader = new FileReader("../data/leagues.json")) { 
			// Convert JSON to Java Object
			leagues = gson.fromJson(reader, Leagues.class);
		} catch (IOException e) {
			e.printStackTrace();
		} 

		//add or update new league
		if(league.getId() != null){
			leagues.getLeagues().put(league.getId(), league); 
		} 

		//save leagues to file
		try (FileWriter writer = new FileWriter("../data/leagues.json")) { 
			gson.toJson(leagues, writer); 
		} catch (IOException e) {
			e.printStackTrace(); 
		} 

		//print saved leagues
		System.out.println(leagues);

		return new Response<String>("Succesfully inserted league settings: " + league);
	} 
} 

