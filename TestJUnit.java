import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.InterruptedException;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import org.junit.*;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.*;
import org.openqa.selenium.phantomjs.*;
import org.openqa.selenium.support.ui.*;
import static org.junit.Assert.assertEquals;

public class TestJUnit {

	static WebDriver driver;
	static WebElement tables;
	static List<WebElement> rows;
	static WebElement num2Field;
	static WebElement submitBtn;
	static Hashtable<String,Match> matches;
	static Toolkit tk;
	static boolean playSounds;
	static boolean displayPopups;
	static ServerSocket serverSocket;

	@BeforeClass
	public static void beforeClass(){
	}

	@Before 
	public void beforeEach(){
		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				System.out.println("Uncaught exception: " + ex);
				System.out.println("Quitting any existing drivers...");
				if(driver != null){
					driver.quit(); 
				}
				//kill any existing drivers
				System.out.println("Killing any existing drivers...");
				try{
					Runtime.getRuntime().exec("taskkill /F /PID phantomjs.exe");
				}catch(IOException e){
					System.err.println("Could not kill headless drivers");
					System.err.println(e);
				}
				System.out.println("Done.");
				JOptionPane.showMessageDialog(null,
						"There was an error and Flashscores Live Basketball must exit. Please restart the application. \n\n Uncaught exception: \n" + ex); 
				System.out.println("Exiting...");
				System.exit(1);
			}
		};
		Thread t = new Thread() {
			public void run() {
				//Schedule a job for the event-dispatching thread:
				//adding TrayIcon.
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						createAndShowGUI();
					}
				});
				//prevent additional instances of this app from running
				try{
					serverSocket = new ServerSocket(33133);
				}catch(IOException e){
					System.err.println("Could not listen on port: 33133");
					JOptionPane.showMessageDialog(null,
							"Could not start Flashscores Live Basketball on port 33133.\nIs the port in use? Please close any processes that may be using this port. \nOther processes that may be using this port may include, for example, other \ninstances of Flashscores Live Basketball."); 
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
				//kill any existing drivers
				try{
					Runtime.getRuntime().exec("taskkill /F /PID phantomjs.exe");
				}catch(IOException e){
					System.err.println("Could not kill headless drivers");
					System.err.println(e);
				}
				//set up driver
				System.out.println("Initializing...");
				String OS_name = System.getProperty("os.name");
				String driver_path = "";
				if(OS_name.startsWith("Windows")){
					driver_path = "phantomjs.exe";
				}else if (OS_name.startsWith("Mac")){
					System.out.println("Mac not supported");
					System.exit(0);
				}
				File file = new File(driver_path);
				System.setProperty("phantomjs.binary.path", file.getAbsolutePath());
				driver = new PhantomJSDriver();
				driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
				//initialize program options
				playSounds = true;
				displayPopups = true;
				String sport = "basketball";
				String stage = "stage-live"; //can be stage-live, stage-finished, stage-interrupted, or stage-scheduled
				int numRounds = 3; //the round to send alert when finishing that round
				//initialize global var & utils
				matches = new Hashtable<String,Match>();	
				tk = Toolkit.getDefaultToolkit();
				//start driver
				driver.get("http://www.flashscore.com/"+sport+"/");
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
						if(match.doesMeetCondition()){
							if(playSounds){
								Thread beepThread = new Thread(new Runnable(){
									public void run(){
										try{
											for(int i = 0; i < 20; i++){
												tk.beep(); 
												TimeUnit.SECONDS.sleep(1);
											}
										}catch(InterruptedException e){};
									}
								});
								beepThread.start();
							} 
							if(displayPopups){
								Thread popupThread = new Thread(new Runnable(){
									public void run(){
										JOptionPane.showMessageDialog(null,
											"Round 4 Starting with 3-Round-Favor:\n"+match.getMatchName()); 
									}
								});
								popupThread.start();
							} 
						} 
					}
				}
			}
		};
		t.setUncaughtExceptionHandler(h);
		t.start();
	}

	@After
	public void after(){
		driver.quit();
	}

	@AfterClass
	public static void afterClass(){
		driver.quit();
	}

	@Test
	public void test_001() throws InterruptedException {
		assertEquals(0,0);
	} 

	private static void createAndShowGUI() {
		//Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon =
			new TrayIcon(createImage("icon.gif", "tray icon"));
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a popup menu components
		CheckboxMenuItem cb1 = new CheckboxMenuItem("Display Popups", true);
		CheckboxMenuItem cb2 = new CheckboxMenuItem("Sound", true);
		MenuItem exitItem = new MenuItem("Exit");

		//Add components to popup menu
		popup.add(cb1);
		popup.add(cb2);
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);
		trayIcon.setImageAutoSize(true);
		trayIcon.setToolTip("Flashscores Live Basketball");

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		} 

		cb1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int cb1Id = e.getStateChange();
				if (cb1Id == ItemEvent.SELECTED){
					//turn on popups
					displayPopups = true;
				} else {
					//turn off popups
					displayPopups = false;
				}
			}
		});
		         
		 cb2.addItemListener(new ItemListener() {
			 public void itemStateChanged(ItemEvent e) {
				 int cb2Id = e.getStateChange();
				 if (cb2Id == ItemEvent.SELECTED){
					 //turn on sound
					 playSounds = true;
				 } else {
					 //turn off sound
					 playSounds = false;
				 }
			 }
		 }); 

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tray.remove(trayIcon);
				driver.quit(); //make sure to kill driver
				System.exit(0);
			}
		});
	}

	//Obtain the image URL
	protected static Image createImage(String path, String description) {
			return (new ImageIcon("icon.gif", description)).getImage();
	} 

	public class Match {
		private String homeTeam;
		private String awayTeam;
		private List<Integer> homeScores;
		private List<Integer> awayScores; 
		private String key;
		private String roundStatus;
		private int time;
		private boolean notified;
		
		public Match(String key){
			this.key = key;
			homeScores = new ArrayList<Integer>();
			awayScores = new ArrayList<Integer>(); 
			homeTeam = "";
			awayTeam = ""; 
			roundStatus = ""; 
			time = 0;
			notified = false;
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
			}
		} 

		public boolean doesMeetCondition(){
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
					if(!notified){
						return notified = true;
					}
				} 
			}
			//System.out.println("+/-/0: " + hasPositive+"/"+ hasNegative+"/"+ hasZero );
			return false;
		}

		public String getMatchName(){
			return homeTeam + " vs. " + awayTeam; 
		}

		public String toString(){
			return 
				((notified)? " * " : "") + this.getMatchName() + "\n" +
				"-------------------------\n" +
				roundStatus + " - " + time + "'\n" +
				"-------------------------\n" + 
				homeTeam + ": " + homeScores + "\n" + 
				awayTeam + ": " + awayScores + "\n"; 
		} 
	} 
}

