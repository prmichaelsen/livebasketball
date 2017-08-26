import com.google.gson.Gson;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.io.DataOutputStream;
import java.net.URLEncoder;
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
import java.io.File;
import java.io.IOException;
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
import java.util.concurrent.BlockingQueue;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonObjectParser; 
import java.util.List;

public class Main {

	//static Hashtable<String,Match> matches;
	static BlockingQueue<Notification> notifications;
	static Leagues leagues;
	//static Server server;
	static Toolkit tk;
	static boolean playSounds;
	static boolean displayPopups;
	static ServerSocket serverSocket;
	static JFrame frm;
	static JTextArea matchesTextArea;
	static int looking;

	public static void main(String args[]){ 
		try{
			getLeagues();
		} catch (Exception e){
			e.printStackTrace();
		}
		//handle uncaught exceptions
		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				String msg =  "There was a non-fatal error in Livebasketball. No user action is required as the application will resume normally. \n\nUncaught exception: \n" + ex ;
				System.out.println(msg);
				System.out.println("Exiting and restarting thread..."); 
			}
		};

		//handle any init or clean up from prev instances
		/* Place code here for handling prev instances */

		// init gui
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
			String msg =  "There is already an instance of Livebasketball running!";
			System.err.println(msg);
			System.exit(0);
		}

		//add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				//add any program clean up here
				

				//TODO nice to have: should be removed from tray in the event of a
				//non graceful exit
				/*
				try {
					tray.remove(trayIcon);
				} catch (AWTException e) {
					System.out.println("TrayIcon could not be added.");
					return;
				} 
				*/
			}
		}));

		//initialize program options
		playSounds = true;
		displayPopups = true; 

		//initialize global var & utils
		//matches = new Hashtable<String,Match>();	
		//leagues = new Leagues();	
		tk = Toolkit.getDefaultToolkit(); 

		System.out.println("Initialized.");
		System.out.println("Running..."); 

		//start worker threads
		Thread scoreListener = new Thread(new Main().new ScoreListener(), "Livebasketball ScoreListener");
		scoreListener.setUncaughtExceptionHandler(h);
		scoreListener.start();

		Thread client = new Thread(new Main().new Client(), "Livebasketball Client");
		client.setUncaughtExceptionHandler(h);
		client.start();
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

	public static class Leagues{ 
		private Hashtable<String,League> leagues; 

		public Leagues(){ 
			leagues = new Hashtable<String,League>();
		};

		public Hashtable<String,League> getLeagues(){ return leagues; }
		public void setLeagues(Hashtable<String,League> leagues){
			this.leagues = leagues;
		} 
	}

	private static void createAndShowGUI() {
		//Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();
		final Image iconImage = createImage("icon.gif", "tray icon");
		final TrayIcon trayIcon = new TrayIcon(iconImage);
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
		trayIcon.setToolTip("Livebasketball");

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
				System.exit(0);
			}
		});

		frm = new JFrame("Livebasketball");
		frm.setIconImage(iconImage);
		String msg = "Starting Up...";
		matchesTextArea = new JTextArea(msg);
		JScrollPane scrollPane = new JScrollPane(matchesTextArea);  
		matchesTextArea.setLineWrap(true);  
		matchesTextArea.setWrapStyleWord(true); 
		matchesTextArea.setEditable(false); 
		matchesTextArea.setMargin( new Insets(10,10,10,10) );
		scrollPane.setPreferredSize( new Dimension( 500, 300 ) );
		frm.getContentPane().add(scrollPane);

		trayIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				frm.setVisible(true);
				frm.setExtendedState(JFrame.NORMAL);
			} 
		});
		frm.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frm.setVisible(false);
				frm.setExtendedState(JFrame.ICONIFIED);
			}
		});
		frm.setSize(500, 500);
		frm.setLocationRelativeTo(null);
		frm.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frm.setVisible(true);
	}

	//Obtain the image URL
	protected static Image createImage(String path, String description) {
		return (new ImageIcon("icon.gif", description)).getImage();
	} 

	public class Client implements Runnable {
		public void run(){
			//start tcp websocket
			String msg = null;
			notifications = new LinkedBlockingQueue<Notification>();
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			Socket clientSocket = null; 
			try{
				clientSocket = new Socket("ec2-34-213-227-13.us-west-2.compute.amazonaws.com", 6789);
			System.out.println("connected!");
			} catch ( UnknownHostException e){
				e.printStackTrace();
			} catch ( IOException e){
				e.printStackTrace();
			}
			DataOutputStream outToServer = null;
			BufferedReader inFromServer = null;
			if(clientSocket != null){
				try{
					outToServer = new DataOutputStream(clientSocket.getOutputStream());
					inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				} catch ( IOException e){
					e.printStackTrace();
				} 
			} 
			while(true){ 
				if(inFromServer != null){
					try{
						msg = inFromServer.readLine();
					} catch ( IOException e){
						e.printStackTrace();
					}
					if(msg != null){
						System.out.println("FROM SERVER: " + msg);
						Gson lgson = new Gson(); 
						Notification notification = lgson.fromJson(msg, Notification.class);  
						if(notification != null){
							notifications.offer(notification);
						} 
					}
				}
			}
			//since the above is a json, we can use
			//gson/json to get back our match objects
			//exactly. Or, we can use a new object, for instance
			//notification object, to just get pertintent info

			//clientSocket.close(); //we cannot close the socket bc of the for loop
			//this needs fixing some day
		} 
	} 

	public class ScoreListener implements Runnable {
		public void run(){ 
			while(true){
				if(notifications != null){
					try{
						Notification notification = notifications.take();
						displayNotification(notification); 
					} catch (InterruptedException e){
						e.printStackTrace();
					} 
				} 
			}
		} 
	}

	public static void displayNotification(Notification notification){
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
					JOptionPane.showMessageDialog(frm,
							notification.getMessage()); 
				}
			});
			popupThread.start();
		} 
	}

	public class Notification{
		private String matchName;
		private String condition;

		public Notification(){}

		public void setMatchName(String matchName){ this.matchName = matchName; }
		public void setCondition(String condition){ this.condition = condition; }

		public String getCondition(String condition){ return condition; }
		public String getMatchName(String matchName){ return matchName; } 

		public String getMessage(){
			return condition + ":\n" + matchName;
		} 
	} 

	public static Leagues getLeagues() throws Exception {
		HttpRequestFactory requestFactory =
			new NetHttpTransport().createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
					request.setParser(new JsonObjectParser(new GsonFactory()));
				}
			});
		GenericUrl url = new GenericUrl("http://ec2-34-211-119-222.us-west-2.compute.amazonaws.com/livebasketball/leagues");
		HttpRequest request = requestFactory.buildGetRequest(url);
		Leagues leagues = new Gson().fromJson(request.execute().parseAsString(), Leagues.class);
		if(leagues != null){
			System.out.println(leagues);
			System.out.println(leagues.getLeagues());
		}
		return leagues;
	} 
} 
