package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patrickmichaelsen.livebasketball.User;
import com.patrickmichaelsen.livebasketball.Users;
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

@Path("/user")
public class UserService {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Users getUsers() {
		System.out.println("Received request: getUsers()");
		Users users = new Users();
		//read users from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (Reader reader = new FileReader("../../server/data/users.json")) {
			// Convert JSON to Java Object
			users = gson.fromJson(reader, Users.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return users;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response<String> postUser(User user) {
		if(user.getUsername() == null || user.getUsername().equals("")){
			return new Response<String>("Username required");
		}

		Users users = new Users();

		//read users from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (Reader reader = new FileReader("../../server/data/users.json")) {
			// Convert JSON to Java Object
			users = gson.fromJson(reader, Users.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//if the user exists, then the user cannot be set
		if(users.containsUser(user)){
			return new Response<String>("User with username already exists");
		}
		//add or update new user
		else if( user.getId() != null && !user.getId().equals("") ){
			users.add(user);
		}

		//save users to file
		try (FileWriter writer = new FileWriter("../../server/data/users.json")) {
			gson.toJson(users, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new Response<String>("Succesfully added new user: " + user);
	}

	@POST
    @Path("/push-token")
	@Produces(MediaType.APPLICATION_JSON)
	public Response<String> postPushToken(User user) {
		if(user.getPushToken() == null || user.getPushToken().equals("")){
		    return new Response<String>("Push Token required");
		}

		Users users = new Users();

		//read users from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (Reader reader = new FileReader("../../server/data/users.json")) {
			// Convert JSON to Java Object
			users = gson.fromJson(reader, Users.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//if the user exists, set the token
		if(users.containsUser(user)){
			users.get(user.getId()).setPushToken(user.getPushToken());
		}
		//add or update new user
		else if( user.getId() != null && !user.getId().equals("") ){
			users.add(user);
		}

		//save users to file
		try (FileWriter writer = new FileWriter("../../server/data/users.json")) {
			gson.toJson(users, writer);
		} catch (IOException e) {
			e.printStackTrace(); 
		} 

		return new Response<String>("Succesfully added push token for user: " + user);
	} 
} 

