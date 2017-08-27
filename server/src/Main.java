package com.patrickmichaelsen.livebasketball;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.InterruptedException;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.*;
import org.openqa.selenium.phantomjs.*;
import org.openqa.selenium.support.ui.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.io.DataOutputStream;
import java.net.URLEncoder;

public class Main {

	static WebDriver driver;
	static List<WebElement> rows;
	static List<WebElement> tables;
	static WebElement num2Field;
	static WebElement submitBtn;
	static Hashtable<String,Match> matches;
	static Leagues leagues;
	static List<Client> clients;
	static boolean playSounds;
	static boolean displayPopups;
	static ServerSocket serverSocket;
	static JFrame frm;
	static JTextArea matchesTextArea;
	static int looking;
	static Runtime mainRuntime; 
	static String sport;
	static String stage;

	public static void main(String args[]){ 
			//initialize program options
			playSounds = true;
			displayPopups = true;
			sport = Constants.Sport.BASKETBALL;
			stage = Constants.Stage.LIVE;

		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				System.out.println("Uncaught exception: " + ex);
				System.out.println("Quitting any existing drivers...");
				if(driver != null){
					driver.quit(); 
				}
				System.out.println("Done.");
				String msg =  "There was a non-fatal error in Flashscores Live Basketball. No user action is required as the application will resume normally. \n\nUncaught exception: \n" + ex ;
				System.out.println(msg);
				System.out.println("Exiting and restarting thread..."); 
			}
		};
		//prevent additional instances of this app from running
		try{
			serverSocket = new ServerSocket(33533);
		}catch(IOException e){
			System.err.println("Could not listen on port: 33533");
			String msg =  "Could not start Flashscores Live Basketball on port 33133.\nIs the port in use? Please close any processes that may be using this port. Other processes that may be using this port may include, for example, other instances of Flashscores Live Basketball.";
			System.err.println(msg);
			System.exit(0);
		}
		//add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				if(driver != null){
					driver.quit(); 
				}
			}
		}));

		//config driver
		String OS_name = System.getProperty("os.name");
		String driver_path = "";
		if(OS_name.startsWith("Windows")){
			driver_path = "phantomjs.exe";
		}else if (OS_name.startsWith("Mac")){
			System.out.println("Mac not supported");
			System.exit(0);
		}else if(OS_name.startsWith("Linux")){
			driver_path = "phantomjs"; 
		}
		File file = new File(driver_path);
		System.setProperty("phantomjs.binary.path", file.getAbsolutePath());

		//start threads
		ScoreChecker scoreChecker = new Main().new ScoreChecker();
		while(true){
			Thread t = new Thread(scoreChecker, "Flashscores Live Basketball ScoreChecker"); 
			t.setUncaughtExceptionHandler(h);
			t.start();
			NewClientListener newClientListener = new Main().new NewClientListener();
			Thread newClientListenerThread = new Thread(newClientListener, "Flashscores Live Basketball NewClientListener"); 
			newClientListenerThread.start();
			try{
				t.join();
			}catch(InterruptedException e){ };
		}
	} 

	// listens for new connections
	public class NewClientListener implements Runnable {
		public void run(){
			//start tcp websocket
			String clientSentence;
			String capitalizedSentence;
			ServerSocket welcomeSocket = null;
			clients = new ArrayList<Client>();
			try{
				welcomeSocket = new ServerSocket(6789);
			} catch (IOException e){
				e.printStackTrace();
			}

			while(true){ 
				Client client = new Client(welcomeSocket);
				try{
					System.out.println("Waiting for new connection...");
					if(client.waitForConnection()){
						System.out.println("New client connected to server!");
						clients.add(client); 
					}
				}catch(IOException e){
					e.printStackTrace();
				} 
			} 
		} 
	}

	// essentially the main class for this program
	// handles scraping of webpage and sends notifications
	// to any registered clients
	public class ScoreChecker implements Runnable { 
		public void run(){ 
			//set up driver
			System.out.println("Initializing...");
			driver = new PhantomJSDriver();
			driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
			//start driver
			driver.get("http://www.flashscore.com/"+sport+"/");

			//initialize global var & utils
			matches = new Hashtable<String,Match>();	
			leagues = new Leagues();	

			System.out.println("Initialized.");
			System.out.println("Running..."); 
			while(true){
				if(driver == null){
					System.err.println("No driver found. Exiting...");
					System.exit(0); 
				} 

				//set up common elements
				try {
					tables = driver.findElements(By.cssSelector(".fs-table>.table-main>."+sport));
				}
				catch (NoSuchElementException e){ } 
				for(WebElement table : tables){ 
					//set up common elements
					try {
						rows = table.findElements(By.cssSelector("tbody>tr."+stage));
					}
					catch (NoSuchElementException e){ } 

					League league = getLeague(table);  //updates static leagues variable
					//get scores
					for(WebElement row : rows){
						getMatch(row, league); //updates static match variable
					}	
				}
				Enumeration e = matches.elements(); 
				while(e.hasMoreElements()){
					Match match = (Match)e.nextElement();
					System.out.println(match); 
					boolean notificationsEnabled = false;
					League league = leagues.getLeagues().get(match.getLeagueId());
					if(league != null){
						notificationsEnabled = league.getEnabled();
					}
					if(notificationsEnabled){
						if(match.doesMeetConditionOne() || match.doesMeetConditionTwo()){
							System.out.println( "------\n------\n MATCH\n------\n------\n");
							// send java client notifications
							sendClientNotifications(match);
							//send mobile notifications
							sendMobileNotifications(match);
						}
					}
				}
				try{
					TimeUnit.SECONDS.sleep(5); 
				}catch(InterruptedException e2){};
			}
		} 
	} 

	public static void sendMobileNotifications(Match match){
		try{
			String s = null;
			String[] cmd = new String[] {"python3", "push_notifications.py", match.getCondition(), match.getMatchName()};
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
					InputStreamReader(p.getErrorStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			// read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
			System.out.println(cmd);
			System.out.println("Sent push notifications");
		}catch(IOException er){
			System.err.println("Could not send push notifications");
			System.err.println(er);
		} 
	} 

	public static void sendClientNotifications(Match match){
		if(clients != null){
			for(Client client : clients){
				try{
					Gson lgson = new Gson();
					Notification notification = new Notification(match.getCondition(), match.getMatchName());
					String msg = lgson.toJson(notification);
					client.writeToClient(msg+"\n");
				} catch (SocketException e2){
					System.err.println("Client is no longer connected! You should find a way to remove this client from the list of connected clients");
					e2.printStackTrace();
				} catch (IOException e2){
					e2.printStackTrace();
				}
			}
		} 
	}

	public static League getLeague(WebElement table){ 
		//read leagues from file
		Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
		try (Reader reader = new FileReader("../data/leagues.json")) { 
			// Convert JSON to Java Object
			leagues = gson.fromJson(reader, Leagues.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<WebElement> leaguesDOM = null;
		League league = new League();

		try {
			leaguesDOM = table.findElements(By.cssSelector("thead > tr > td.head_ab > span.country.left > span.name"));
		}
		catch (NoSuchElementException e){ } 

		if(leaguesDOM != null){
			//get scores
			for(WebElement leagueDOM : leaguesDOM){
				WebElement countryDOM = null;
				WebElement nameDOM = null;
				try{
					countryDOM = leagueDOM.findElement(By.cssSelector("span.country_part"));
					if(countryDOM != null){ 
						league.setCountry(countryDOM.getAttribute("innerHTML"));
					}
				}
				catch(NoSuchElementException e){}
				catch(StaleElementReferenceException e){}
				try{
					nameDOM = leagueDOM.findElement(By.cssSelector("span.tournament_part"));
					if(nameDOM != null){ 
						league.setName(nameDOM.getAttribute("innerHTML"));
					}
				}
				catch(NoSuchElementException e){}
				catch(StaleElementReferenceException e){}
				if(league.getId() != null){
					League stored_league = leagues.getLeagues().get(league.getId());
					if(stored_league == null){
						leagues.getLeagues().put(league.getId(), league); 
						//save leagues to file
						try (FileWriter writer = new FileWriter("../data/leagues.json")) { 
							gson.toJson(leagues, writer); 
						} catch (IOException e) {
							e.printStackTrace();
						} 
						return league;
					} 
					return stored_league;

				} 
			} 
		} 
		return league;
	} 

	public static void getMatch(WebElement row, League league){
		boolean isHomeTeam = false;
		boolean isAwayTeam = false;
		String row_id = null;
		try{ 
			row_id = row.getAttribute("id");
		}
		catch (StaleElementReferenceException e){ }
		String match_id = null;
		String team = null;
		if(row_id != null){
			team = row_id.substring(0,1); //first character indicates home/away team
			match_id = row_id.substring(2); //remaining str is match id
		}
		if(team != null){
			if(team.equals("g")){
				isHomeTeam = true; isAwayTeam = false;
			}
			else if (team.equals("x")){
				isAwayTeam = true; isHomeTeam = false; 
			}
		}
		if(match_id != null){
			Match match = matches.get(match_id);
			if(match == null){
				match = new Match(match_id, league.getId());
				matches.put(match_id, match); 
			}
			match.setLastUpdated(System.currentTimeMillis());
			WebElement homeTeamNameDOM = null;
			WebElement awayTeamNameDOM = null;
			WebElement roundStatusDOM = null;
			String homeTeamName = null;
			String awayTeamName = null;
			String roundStatus = null;
			if(isHomeTeam){
				try{
					roundStatusDOM = row.findElement(By.cssSelector("td.timer>span"));	
					if(roundStatusDOM != null){ 
						roundStatus = (roundStatusDOM.getAttribute("innerHTML"));
						match.setRoundStatus(roundStatus);
					}
				}
				catch (NoSuchElementException e){ } 
				catch (StaleElementReferenceException e){ }
			}

			if(isHomeTeam){
				try{
					homeTeamNameDOM = row.findElement(By.cssSelector("td.team-home>span"));	
					if(homeTeamNameDOM != null){ 
						homeTeamName = (homeTeamNameDOM.getAttribute("innerHTML"));
						match.setHomeTeam(homeTeamName);
					}
				}
				catch (NoSuchElementException e){ }
				catch (StaleElementReferenceException e){ }
			}
			if(isAwayTeam){
				try{ 
					awayTeamNameDOM = row.findElement(By.cssSelector("td.team-away>span"));	
					if(awayTeamNameDOM != null){ 
						awayTeamName = (awayTeamNameDOM.getAttribute("innerHTML"));
						match.setAwayTeam(awayTeamName);
					}
				}
				catch (NoSuchElementException e){ }
				catch (StaleElementReferenceException e){ }
			}
			List<WebElement> scoreDOMs = new ArrayList<WebElement>();
			List<Integer> scores = new ArrayList<Integer>(); 
			if(isHomeTeam){
				try{ 
					scoreDOMs = row.findElements(By.cssSelector("td.part-bottom"));	
				}
				catch (NoSuchElementException e){ } 
			}
			if(isAwayTeam){ 
				try{ 
					scoreDOMs =row.findElements(By.cssSelector("td.part-top"));	
				}
				catch (NoSuchElementException e){ }
			} 

			for(WebElement scoreTd : scoreDOMs ){
				try{
					int score = Integer.parseInt(scoreTd.getAttribute("innerHTML"));
					scores.add(score); 
				}
				catch(NumberFormatException e){} 
				catch (StaleElementReferenceException e){ }
			}
			if(isHomeTeam){ 
				match.setHomeScores(scores);
			}else if(isAwayTeam){
				match.setAwayScores(scores);
			}
		}
	}
}

