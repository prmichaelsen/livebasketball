package com.patrickmichaelsen.livebasketball;

import java.time.LocalDateTime;
import java.text.DateFormat;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.Calendar; 
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import com.google.gson.Gson;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
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
import java.util.Iterator;
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
	static boolean playSounds;
	static boolean displayPopups;
	static ServerSocket serverSocket;
	static JFrame frm;
	static JTextArea matchesTextArea;
	static int looking;
	static Runtime mainRuntime; 
	static String sport;
	static String stage;
	static boolean Windows, Linux, Mac = false;
	static File push_notifications_py = null;

	public static void main(String args[]){
		//prevent additional instances of this app from running
		try{
			serverSocket = new ServerSocket(33533);
		}catch(IOException e){
			System.err.println("Could not listen on port: 33533");
			String msg =  "Could not start Flashscores Live Basketball on port 33133.\nIs the port in use? Please close any processes that may be using this port. Other processes that may be using this port may include, for example, other instances of Flashscores Live Basketball.";
			System.err.println(msg);
			System.exit(0);
		}

		//initialize program options
		sport = Constants.Sport.BASKETBALL;
		stage = Constants.Stage.LIVE;

		//get os
		String OS_name = System.getProperty("os.name");
		if(OS_name.startsWith("Windows")){
			Windows = true;
		}else if (OS_name.startsWith("Mac")){
			Mac = true;
			System.exit(0);
		}else if(OS_name.startsWith("Linux")){
			Linux = true;
		}

		//load resources
		push_notifications_py = explodeExecutableResource("push_notifications.py");

		// set uncaught exception handler
		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable e) {
				if(driver != null){
					driver.quit(); 
				}
				String msg =  "There was a fatal error in Flashscores Live Basketball. No user action is required as the application will resume normally. \n\nUncaught exception: \n"; 
				System.err.println(msg);
				e.printStackTrace();
				System.err.println("Exiting..."); 
				//need to stop all threads
				System.exit(1); 
			}
		};

		//add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				System.out.println("Shutting down!");
				if(driver != null){ 
					driver.quit(); 
				}
				System.out.println("Main thread stopped!");
				System.exit(1);
			}
		}));

		//config driver
		String driver_path = "";
		if(Windows){
			driver_path = "phantomjs.exe";
		} else if(Mac){
			System.out.println("Mac not supported");
			System.exit(0);
		} else if(Linux){
			driver_path = "phantomjs"; 
		}
		File phantom_driver = explodeExecutableResource(driver_path);
		System.setProperty("phantomjs.binary.path", phantom_driver.getAbsolutePath()); 

		//set up driver
		System.out.println("Initializing...");
		driver = new PhantomJSDriver();
		driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);

		//start driver
		System.out.println("Initialized.");
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		System.out.println(dtf.format(LocalDateTime.now()) + ": Driver starting...");
		System.out.println("Running..."); 

		//start main program
		//matches are initialized once
		Hashtable<String,Match> matches = new Hashtable<String,Match>();	
		//timestamp league last forever
		League timestamp = new League();
		while(true){
			driver.get("http://www.flashscore.com/"+sport+"/"); 

			//set the timezone
			WebElement tzDropdownDOM = null;
			try{
				tzDropdownDOM = driver.findElement(By.cssSelector("#tzactual"));
				if(tzDropdownDOM != null){ 
					tzDropdownDOM.click();
				}
			}
			catch(NoSuchElementException e){}
			catch(StaleElementReferenceException e){}
			try{
				TimeUnit.SECONDS.sleep(3); 
			}catch(InterruptedException e2){};
			WebElement tzDOM = null;
			try{
				tzDOM = driver.findElement(By.cssSelector("#tzcontent > li:nth-child(14) > a"));
				if(tzDOM != null){ 
					tzDOM.click();
				}
			}
			catch(NoSuchElementException e){}
			catch(StaleElementReferenceException e){} 

			//leagues are intialized every loop
			Leagues leagues = new Leagues();	

			//ensure driver is connected
			if(driver == null){
				System.err.println("No driver found. Exiting...");
				System.exit(0); 
			} 

			//get the league tables scheduled for today
			try {
				tables = driver.findElements(By.cssSelector(".fs-table>.table-main>."+sport));
			}
			catch (NoSuchElementException e){ } 
			for(WebElement table : tables){ 
				//get the league for this table
				League league = getLeague(table); 
				leagues.add(league);

				//get the match rows in this league table
				try {
					rows = table.findElements(By.cssSelector("tbody>tr."+stage));
				}
				catch (NoSuchElementException e){ } 

				//get matches
				for(WebElement row : rows){
					Match match = getMatch(row, league, matches);
					matches.put(match.getId(), match);
				}	
			}

			//get timestamp league from file if it exists
			Leagues l = null;
			Gson gson = new Gson();
			try (Reader reader = new FileReader("../data/leagues.json")) { 
				// Convert JSON to Java Object
				l = (Leagues) gson.fromJson(reader, Leagues.class);
			} catch (IOException e) { 
				e.printStackTrace();
			} 
			if(l != null){
				Iterator<League> it1 = l.getLeagues().values().iterator();	
				while(it1.hasNext()){
					League league = it1.next();
					if(league.getId().indexOf('#') != -1){
						timestamp = league;
						it1.remove();
					}
					//last ditch effort to update leagues
					if(leagues.get(league.getId()) != null){
						leagues.get(league.getId()).setEnabled(league.getEnabled());
					}
				}
				timestamp.setCountry("# Select All ");
				TimeZone tz = java.util.TimeZone.getTimeZone("Europe/Warsaw");
				Calendar c = java.util.Calendar.getInstance(tz);
				c.setTimeZone(tz);
				timestamp.setName("(Last Updated: " + c.get(Calendar.DAY_OF_MONTH)+" "+c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " " + c.get(Calendar.HOUR_OF_DAY)+":"+String.format("%1$02d",c.get(Calendar.MINUTE))+")");
				timestamp.setId(timestamp.getCountry()+timestamp.getName());
				leagues.add(timestamp); 
			}

			//save leagues to file
			try (FileWriter writer = new FileWriter("../data/leagues.json")) { 
				gson.toJson(leagues, writer); 
			} catch (IOException e) {
				e.printStackTrace();
			}

			//send out notifications
			Iterator<Match> it = matches.values().iterator(); 
			while(it.hasNext()){
				Match match = it.next();
				System.out.println(match); 
				boolean notificationsEnabled = false;
				League league = leagues.get(match.getLeagueId());
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
				//remove a match if it is more than 4 hours old
				if(match.getLastUpdated() < ( System.currentTimeMillis() - 1000*60*60*4) ){ 
					it.remove();
				}
			}
			try{
				TimeUnit.SECONDS.sleep(5); 
			}catch(InterruptedException e2){};

			System.out.println(dtf.format(LocalDateTime.now()) + ": Driver resetting...");
		} 
		System.out.println("ScoreChecker thread stopped!");
	} 

	public static void sendMobileNotifications(Match match){
		try{
			String s = null;
			String[] cmd = new String[] {
				"python3", 
				push_notifications_py.getAbsolutePath(), 
				match.getCondition(), 
				match.getMatchName()
			};
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
		Gson gson = new Gson();
		Notifications notifications = null;

		// read notifs
		try (Reader reader = new FileReader("../data/notifications.json")) { 
			notifications = (Notifications) gson.fromJson(reader, Notifications.class);
		} catch (IOException e) { 
			e.printStackTrace();
		} 

		if(notifications == null){
			notifications = new Notifications();
		}

		Notification notification = new Notification(match.getCondition(), match.getMatchName());
		notifications.add(notification);

		// save notifs
		try (FileWriter writer = new FileWriter("../data/notifications.json")) { 
			gson.toJson(notifications, writer); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static League getLeague(WebElement table){ 
		List<WebElement> leaguesDOM = null;
		League league = new League();

		//read leagues from file
		Leagues leagues = null;
		Gson gson = new Gson();
		try (Reader reader = new FileReader("../data/leagues.json")) { 
			// Convert JSON to Java Object
			leagues = (Leagues) gson.fromJson(reader, Leagues.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//get league info from DOM
		try {
			leaguesDOM = table.findElements(By.cssSelector("thead > tr > td.head_ab > span.country.left > span.name"));
		}
		catch (NoSuchElementException e){ } 

		if(leaguesDOM == null){
			return null;
		}
	
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
		} 

		if(league == null){
			return null;
		}

		if(league.hashId() == null){
			return null;
		} 

		//check to see if league is saved
		if(leagues != null){
			if(leagues.containsLeague(league)){
				return leagues.get(league.getId());
			}
		}

		//otherwise return new league
		return league;
	} 

	// extracts data from the row and pairs it
	// with a corresponding match found in the 
	// matches hashtable
	public static Match getMatch(WebElement row, League league, Hashtable<String, Match> matches){
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
		if(match_id == null){
			return null;
		}
		Match match = matches.get(match_id);
		if(match == null){
			match = new Match(match_id, league.getId());
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

		if(isHomeTeam && match.getHomeTeam().isEmpty()){
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
		if(isAwayTeam && match.getAwayTeam().isEmpty()){
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
	return match;
	}

	// voodoo magic
	// extracts the file located at path
	// from the jar and places it in
	// the working directory
	// and makes it executable
	public static File explodeExecutableResource(String name){
		File file = null;
		try{
			file = File.createTempFile(name, null);
			Path path = Paths.get(file.getAbsolutePath());
			//load from jar
			OutputStream out = new FileOutputStream(file);
			InputStream in = Main.class.getClassLoader().getResourceAsStream("resources/"+name);
			//write to file
			byte[] buffer = new byte[1024*100];
			int len;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			out.close();
			in.close();
			//set respective os perms
			if(Linux){ 
				Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
				permissions.add(PosixFilePermission.OWNER_EXECUTE);
				Files.setPosixFilePermissions(path, permissions);
			}
			if(Windows){
				file.setExecutable(true, false);
			}
		}	catch (Exception e){
			e.printStackTrace();
		}
		file.deleteOnExit();
		return file;
	}
}

