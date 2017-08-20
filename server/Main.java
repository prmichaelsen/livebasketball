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
import java.awt.Toolkit;
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
	static WebElement tables;
	static List<WebElement> rows;
	static WebElement num2Field;
	static WebElement submitBtn;
	static Hashtable<String,Match> matches;
	static Leagues leagues;
	static List<Client> clients;
	static Toolkit tk;
	static boolean playSounds;
	static boolean displayPopups;
	static ServerSocket serverSocket;
	static JFrame frm;
	static JTextArea matchesTextArea;
	static int looking;
	static Runtime mainRuntime;
	//static MinimalServerRest server;

	public static void main(String args[]){ 
		//mainRuntime = Runtime.getRuntime();
		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				System.out.println("Uncaught exception: " + ex);
				System.out.println("Quitting any existing drivers...");
				if(driver != null){
					driver.quit(); 
				} else {
					//kill any existing drivers
					System.out.println("Killing any existing drivers...");
					/*
					try{
						//Runtime.getRuntime().exec("taskkill /F /PID phantomjs.exe");
					}catch(IOException e){
						System.err.println("Could not kill headless drivers");
						System.err.println(e);
					} 
					*/
				}
				System.out.println("Done.");
				String msg =  "There was a non-fatal error in Flashscores Live Basketball. No user action is required as the application will resume normally. \n\nUncaught exception: \n" + ex ;
				System.out.println(msg);
				System.out.println("Exiting and restarting thread..."); 
			}
		};
		//handle any init or clean up from prev instances
		//Schedule a job for the event-dispatching thread:
		//adding TrayIcon.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//createAndShowGUI();
			}
		});
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
				//kill any existing drivers
				//try{
					//Runtime.getRuntime().exec("taskkill /F /PID phantomjs.exe");
				//}catch(IOException e){
					//System.err.println("Could not kill headless drivers");
					//System.err.println(e);
				//}
			}
		}));
		//kill any existing drivers
		//try{
			//Runtime.getRuntime().exec("taskkill /F /PID phantomjs.exe");
		//}catch(IOException e){
			//System.err.println("Could not kill headless drivers");
			//System.err.println(e);
		//}

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
			ScoreNotifier scoreNotifier = new Main().new ScoreNotifier();
			Thread scoreNotifierThread = new Thread(scoreNotifier, "Flashscores Live Basketball ScoreNotifier"); 
			scoreNotifierThread.start();
			try{
				t.join();
			}catch(InterruptedException e){ };
		}
	} 

	public class Match implements Comparable<Match>{
		private String homeTeam;
		private String awayTeam;
		private List<Integer> homeScores;
		private List<Integer> awayScores; 
		private String key;
		private String roundStatus;
		private int time;
		private boolean conditionOneMet;
		private boolean conditionTwoMet;
		private boolean potentialMatch;
		private long lastUpdated;
		
		public Match(String key){
			this.key = key;
			homeScores = new ArrayList<Integer>();
			awayScores = new ArrayList<Integer>(); 
			homeTeam = "";
			awayTeam = ""; 
			roundStatus = ""; 
			time = 0;
			conditionOneMet = false;
			conditionTwoMet = false;
			potentialMatch = true;
			lastUpdated = 0;
		}

		public void setHomeScores(List<Integer> scores){
			this.homeScores = scores; 
		}

		public void setAwayScores(List<Integer> scores){
			this.awayScores = scores; 
		}

		public void setHomeTeam(String name){
			this.homeTeam = name; 
		}

		public void setAwayTeam(String name){
			this.awayTeam = name; 
		}

		public void setRoundStatus(String status){
			if(status.contains("Quarter")){
				if(status.contains("1st")){
					this.roundStatus = "1st Quarter"; 
				}
				if(status.contains("2nd")){
					this.roundStatus = "2nd Quarter"; 
				}
				if(status.contains("3rd")){
					this.roundStatus = "3rd Quarter"; 
				}
				if(status.contains("4th")){
					this.roundStatus = "4th Quarter"; 
				}
				try{
					String time = status.substring(status.indexOf(";")+1, status.indexOf("<span"));
					if(time != null){
						this.time = Integer.parseInt(time); 
					}
				}catch(StringIndexOutOfBoundsException e){ }
				catch(NumberFormatException e){}; 
			} else if (status.contains("Half Time")){
				this.roundStatus = "Half Time"; 
			} else if (status.contains("Finished")){
				this.roundStatus = "Finished"; 
			} else if (status.contains("Overtime")){
				this.roundStatus = "Overtime"; 
				try{
					String time = status.substring(status.indexOf(";")+1, status.indexOf("<span"));
					if(time != null){
						this.time = Integer.parseInt(time); 
					}
				}catch(StringIndexOutOfBoundsException e){ }
				catch(NumberFormatException e){}; 
			}
		} 

		public void setLastUpdated(long timeInMillis){
			this.lastUpdated = timeInMillis;
		} 

		public boolean doesMeetConditionOne(){
			boolean hasPositive = false;
			boolean hasNegative = false;
			boolean hasZero = false; 
			for(int j = 0; j < homeScores.size(); j++){
				int diff = homeScores.get(j) - awayScores.get(j); 
				if(diff > 0){
					hasPositive = true;		
				}
				else if(diff < 0){
					hasNegative = true; 
				} 
				else {
					hasZero = true;
				}
			} 
			if( (!hasPositive || !hasNegative)  && !hasZero ){ 
				if( roundStatus.equals("3rd Quarter") && time == 10){
					if(!conditionOneMet){
						return conditionOneMet = true;
					}
				} 
			}
			return false; 
		}

		public boolean doesMeetConditionTwo(){
			boolean hasPositive = false;
			boolean hasNegative = false;
			boolean hasZero = false; 
			for(int j = 0; j < homeScores.size() - 1; j++){
				int diff = homeScores.get(j) - awayScores.get(j); 
				if(diff > 0){
					hasPositive = true;		
				}
				else if(diff < 0){
					hasNegative = true; 
				} 
				else {
					hasZero = true;
				}
			} 
			if( (!hasPositive || !hasNegative)  && !hasZero ){ 
				if( roundStatus.equals("4th Quarter") && time == 1){
					if(!conditionTwoMet){
						return conditionTwoMet = true;
					}
				} 
			}
			return false;
		}

		public String getMatchName(){
			return homeTeam + " vs. " + awayTeam; 
		}
		
		public String getCondition(){
			if(conditionTwoMet){ 
				return "Round 4 Starting with 3-Round-Favor";
			}
			else if(conditionOneMet){
				return "Round 3 Ending with 3-Round-Favor";
			}
			return "";
		} 

		public String getMessage(){
			if(conditionTwoMet){ 
				return "Round 4 Starting with 3-Round-Favor:\n"+this.getMatchName();
			}
			else if(conditionOneMet){
				return "Round 3 Ending with 3-Round-Favor:\n"+this.getMatchName();
			}
			return "";
		} 

		public String toString(){
			return 
				((conditionOneMet | conditionTwoMet)? " * " : "") + 
				//((potentialMatch & !(conditionOneMet || conditionTwoMet))? " + " : "") + 
				this.getMatchName() + "\n" +
				roundStatus + " - " + time + "'\n" +
				homeTeam + ": " + homeScores + "\n" + 
				awayTeam + ": " + awayScores + "\n"; 
		} 
		
		public long getLastUpdated(){ 
			return lastUpdated;
		}

		@Override
		public int compareTo(Match match){ 
			return (int) (this.lastUpdated - match.getLastUpdated());
		}
	} 

	public class ScoreNotifier implements Runnable {
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

	public class ScoreChecker implements Runnable {

		public void run(){ 
			//initialize program options
			playSounds = true;
			displayPopups = true;
			String sport = "basketball";
			String stage = "stage-live"; //can be stage-live, stage-finished, stage-interrupted, or stage-scheduled
			int numRounds = 3; //the round to send alert when finishing that round

			//set up driver
			System.out.println("Initializing...");
			driver = new PhantomJSDriver();
			driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
			//start driver
			driver.get("http://www.flashscore.com/"+sport+"/");

			//initialize global var & utils
			matches = new Hashtable<String,Match>();	
			leagues = new Leagues();	
			tk = Toolkit.getDefaultToolkit(); 

			System.out.println("Initialized.");
			System.out.println("Running..."); 
			while(true){
				if(driver == null){
					System.err.println("No driver found. Exiting...");
					System.exit(0); 
				} 

				//set up common elements
				try {
					rows = driver.findElements(By.cssSelector(".fs-table>.table-main>."+sport+">tbody>tr."+stage));
				}
				catch (NoSuchElementException e){ } 

				//read leagues from file
				Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
        try (Reader reader = new FileReader("../data/leagues.json")) { 
					// Convert JSON to Java Object
            leagues = gson.fromJson(reader, Leagues.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

				//update leagues
				getLeagues();

				//save leagues to file
				try (FileWriter writer = new FileWriter("../data/leagues.json")) { 
					gson.toJson(leagues, writer); 
				} catch (IOException e) {
					e.printStackTrace();
				} 

				//print saved leagues
				if(leagues != null){
					Enumeration leagues_e = leagues.getLeagues().elements(); 
					while(leagues_e.hasMoreElements()){
						League league = (League)leagues_e.nextElement();
						System.out.println(league);
					} 
				} 

				//get scores
				for(WebElement web_el : rows){
					boolean isHomeTeam = false;
					boolean isAwayTeam = false;
					String row_id = null;
					try{ 
						row_id = web_el.getAttribute("id");
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
							match = new Match(match_id);
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
								roundStatusDOM = web_el.findElement(By.cssSelector("td.timer>span"));	
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
								homeTeamNameDOM = web_el.findElement(By.cssSelector("td.team-home>span"));	
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
								awayTeamNameDOM = web_el.findElement(By.cssSelector("td.team-away>span"));	
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
								scoreDOMs = web_el.findElements(By.cssSelector("td.part-bottom"));	
							}
							catch (NoSuchElementException e){ } 
						}
						if(isAwayTeam){ 
							try{ 
								scoreDOMs =web_el.findElements(By.cssSelector("td.part-top"));	
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
				Enumeration e = matches.elements(); 
				while(e.hasMoreElements()){
					Match match = (Match)e.nextElement();
					System.out.println(match); 
					// send all match info to java client
				  if(clients != null){
						for(Client client : clients){
							try{
								Gson lgson = new Gson(); 
								Notification notification = new Notification(match.getMatchName(), "Live");
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
					if(match.doesMeetConditionOne() || match.doesMeetConditionTwo()){
						System.out.println( "------\n------\n MATCH\n------\n------\n");
						//send mobile notifications
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
						//send java client notifications
						if(clients != null){
							for(Client client : clients){
								//try{
									//Gson lgson = new Gson(); 
									//String msg = lgson.toJson(match);  
									//client.writeToClient(msg+"\n"); 
								//} catch (IOException e2){
									//e2.printStackTrace();
								//}
							}
						}
					}
				}
				try{
					TimeUnit.SECONDS.sleep(5); 
				}catch(InterruptedException e2){};
			}
		} 
	}


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
			int countryCompare = this.country.compareTo(league.country);
			if(countryCompare == 0){
				return this.name.compareTo(league.name);
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

	public static void getLeagues(){ 
		List<WebElement> leaguesDOM = null;

		try {
			leaguesDOM = driver.findElements(By.cssSelector("#fs > div > table > thead > tr > td.head_ab > span.country.left > span.name"));
		}
		catch (NoSuchElementException e){ } 

		if(leaguesDOM != null){
			//get scores
			for(WebElement leagueDOM : leaguesDOM){
				WebElement countryDOM = null;
				WebElement nameDOM = null;
				League league = new Main().new League();
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
					}
				} 
			} 
		} 
	}

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

	public class Client{
		private ServerSocket welcomeSocket;
		private Socket connectionSocket;
		private BufferedReader inFromClient;
		private DataOutputStream outToClient;

		public Client(ServerSocket socket){
			welcomeSocket = socket;
		}

		public boolean waitForConnection() throws IOException{
			if(welcomeSocket != null){
				Socket connectionSocket = welcomeSocket.accept();
				inFromClient =
					new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				return true;
			} 
			return false;
		}

		public String readLineFromClient() throws IOException{
			if(inFromClient != null){ 
				String line = inFromClient.readLine();
				return line; 
			}
			else throw new IOException("inFromClient never initialized");
		}
		
		public void writeToClient(String line) throws IOException{
			if(outToClient != null){
				outToClient.writeBytes(line); 
			}
			else throw new IOException("outToClient never initialized");
		} 
	}

	public class Notification{
		private String matchName;
		private String condition;

		public Notification(){}
		public Notification( String matchName, String condition){
			this.matchName = matchName;
			this.condition = condition;
		}

		public void setMatchName(String matchName){ this.matchName = matchName; }
		public void setCondition(String condition){ this.condition = condition; }

		public String getCondition(String condition){ return condition; }
		public String getMatchName(String matchName){ return matchName; } 

		public String getMessage(){
			return condition + ":\n" + matchName;
		} 
	} 
}

