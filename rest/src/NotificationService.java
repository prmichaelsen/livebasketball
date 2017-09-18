package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patrickmichaelsen.livebasketball.core.Notification;
import com.patrickmichaelsen.livebasketball.core.Notifications;
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

@Path("/notifications")
public class NotificationService {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Notifications getNotifications() { 
		System.out.println("Received request: getNotifications()");
		Notifications notifications = new Notifications();
		//read notifications from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
		try (Reader reader = new FileReader("../../server/data/notifications.json")) { 
			// Convert JSON to Java Object
			notifications = gson.fromJson(reader, Notifications.class);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return notifications;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response<String> deleteNotifications(Notification notification) {
		System.out.println(notification);
		Notifications notifications = new Notifications();

		//read notifications from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
		try (Reader reader = new FileReader("../../server/data/notifications.json")) { 
			// Convert JSON to Java Object
			notifications = gson.fromJson(reader, Notifications.class);
		} catch (IOException e) {
			e.printStackTrace();
		} 

		//delete notification
		notifications.remove(notification);

		//save notifications to file
		try (FileWriter writer = new FileWriter("../../server/data/notifications.json")) { 
			gson.toJson(notifications, writer); 
		} catch (IOException e) {
			e.printStackTrace(); 
		} 

		return new Response<String>("Succesfully removed notification: " + notification);
	} 
} 

