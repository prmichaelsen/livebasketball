package com.patrickmichaelsen.livebasketball;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser; 
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.InterruptedException;
import java.lang.Thread;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

public class Main {

	//static Hashtable<String,Match> matches;
	static BlockingQueue<Notification> notifications;
	static Leagues leagues;
	static JTable table;
	static Toolkit tk;
	static boolean playSounds;
	static boolean displayPopups;
	static ServerSocket serverSocket;
	static JFrame frm;
	static JTextArea matchesTextArea;
	static int looking;

	final static PopupMenu popup = new PopupMenu();
	final static Image iconImage = createImage("icon.gif", "tray icon");
	final static TrayIcon trayIcon = new TrayIcon(iconImage);
	final static SystemTray tray = SystemTray.getSystemTray();

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
			serverSocket = new ServerSocket(24156);
		}catch(IOException e){
			System.err.println("Could not listen on port: 24156");
			String msg =  "There is already an instance of Livebasketball running!";
			System.err.println(msg);
			JOptionPane.showMessageDialog(frm, msg); 
			System.exit(0);
		}

		//add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				//add any program clean up here 
				if(tray != null){ 
					tray.remove(trayIcon);
				}
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

		Thread refresher = new Thread(new Main().new Refresher(), "Livebasketball Refresher");
		refresher.setUncaughtExceptionHandler(h);
		refresher.start();

		Thread client = new Thread(new Main().new Client(), "Livebasketball Client");
		client.setUncaughtExceptionHandler(h);
		client.start();
	} 

	public static class League implements Comparable<League>{
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
		public void setId(String id){ this.id = id; };
		public void setEnabled(boolean active){ this.active = active; };

		public String getCountry(){ return country; }
		public String getName(){ return name; }
		public String getId(){ return id = country + name; }
		public boolean getEnabled(){ return active; }

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
			return this.getId();
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
		try{ 
			//Create and set up the content pane.
			table = new JTable(new LeagueTableModel(leagues = getLeagues()));
			//table.getModel().			//table.setPreferredScrollableViewportSize(new Dimension(500, 500));
			table.setFillsViewportHeight(true); 
			//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
			table.getColumnModel().getColumn(0).setMaxWidth(20);
			//Create the scroll pane and add the table to it.
			JScrollPane scrollPane = new JScrollPane(table); 
			//Add the scroll pane to this panel.
			frm.getContentPane().add(scrollPane);
		} catch (Exception e){
			e.printStackTrace();
		}

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
		frm.pack();
		frm.setVisible(true);
	}

	public static class LeagueTableModel extends AbstractTableModel {
		private boolean DEBUG = false;
		private String[] columnNames = {
			"",
			"League"
		};
		private Object[][] data;

		public LeagueTableModel(Leagues leagues){
			Collection<League> collection = leagues.getLeagues().values();
			List<League> list = new ArrayList<League>(collection);
			Collections.sort(list); 
			Object[][] array = new Object[list.size()][];
			for (int i = 0; i < list.size(); i++) {
				League league = list.get(i);
				Object[] row = { 
					league.getEnabled(),
					league
				};
				array[i] = row;
			}
			data = array;
			this.addTableModelListener(new TableModelListener() { 
				@Override
				public void tableChanged(TableModelEvent e) {
					LeagueTableModel table = (LeagueTableModel) e.getSource();
					if(e.getColumn() == 0 && e.getFirstRow()>-1){
						System.out.println(
								"Row : " + e.getFirstRow() +
								" value :" + table.getValueAt(e.getFirstRow(), e.getColumn()));
						boolean active = (boolean) table.getValueAt(e.getFirstRow(), 0);
						League league = (League) table.getValueAt(e.getFirstRow(), 1);
						league.setEnabled(active);
						try{
							postLeague(league);
						} catch (Exception e2){
							e2.printStackTrace();
						}
					}
				}
			}); 
		}

		public int getColumnCount() { return columnNames.length; } 
		public int getRowCount() { return data.length; } 
		public String getColumnName(int col) { return columnNames[col]; } 
		public Object getValueAt(int row, int col) { return data[row][col]; }

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's
		 * editable.
		 */
		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant,
			//no matter where the cell appears onscreen.
			if (col > 1) {
				return false;
			} else {
				return true;
			}
		}

		/*
		 * Don't need to implement this method unless your table's
		 * data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			if (DEBUG) {
				System.out.println("Setting value at " + row + "," + col
						+ " to " + value
						+ " (an instance of "
						+ value.getClass() + ")");
			}

			data[row][col] = value;
			fireTableCellUpdated(row, col);

			if (DEBUG) {
				System.out.println("New value of data:");
				printDebugData();
			}
		}

		private void printDebugData() {
			int numRows = getRowCount();
			int numCols = getColumnCount();

			for (int i=0; i < numRows; i++) {
				System.out.print("    row " + i + ":");
				for (int j=0; j < numCols; j++) {
					System.out.print("  " + data[i][j]);
				}
				System.out.println();
			}
			System.out.println("--------------------------");
		}

	}


	//Obtain the image URL
	protected static Image createImage(String path, String description) {
		return (new ImageIcon("icon.gif", description)).getImage();
	} 

	public class Refresher implements Runnable {
		public void run(){
			while(true){
				try{
					if(table != null){ 
						table.setModel(new LeagueTableModel(getLeagues()));
						table.getColumnModel().getColumn(0).setMaxWidth(20);
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				try{
					TimeUnit.SECONDS.sleep(60);
				} catch(InterruptedException e){
					e.printStackTrace();
				}; 
			}
		}
	}

	public class Client implements Runnable {
		public void run(){
			//start tcp websocket
			String msg = null;
			notifications = new LinkedBlockingQueue<Notification>();
			Server server = new Server("ec2-35-167-51-118.us-west-2.compute.amazonaws.com", 6789);
			while(true){
				server.connect();
				while(server.isConnected()){ 
					msg = server.readLine();
					if(msg != null){
						System.out.println("FROM SERVER: " + msg);
						Gson lgson = new Gson(); 
						Notification notification = lgson.fromJson(msg, Notification.class);  
						if(notification != null){
							notifications.offer(notification);
						} 
					}
				}
				server.close();
			}
			//since the above is a json, we can use
			//gson/json to get back our match objects
			//exactly. Or, we can use a new object, for instance
			//notification object, to just get pertintent info

			//clientSocket.close(); //we cannot close the socket bc of the for loop
			//this needs fixing some day
		} 
	} 

	public class Server {
			private Socket clientSocket = null; 
			private DataOutputStream outToServer = null;
			private BufferedReader inFromServer = null;
			private String host = null;
			private int port = 0;
			private boolean isConnected = false;

			public Server(String host, int port){
				this.host = host;
				this.port = port;
			}

			public void connect(){
				try{
					clientSocket = new Socket(host, port);
					isConnected = true;
					System.out.println("Connected to TCP server!");
				} catch ( UnknownHostException e){
					e.printStackTrace();
				} catch ( IOException e){
					e.printStackTrace();
				}
				if(clientSocket != null){
					try{
						outToServer = new DataOutputStream(clientSocket.getOutputStream());
						inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					} catch ( IOException e){
						e.printStackTrace();
					} 
				} 
			}

			public boolean isConnected(){ return isConnected; }

			public String readLine(){
				String line = null;
				if( inFromServer != null){
					try{
						line = inFromServer.readLine();
					} catch ( IOException e ){
						e.printStackTrace(); 
					}
				}
				if( line == null ){
					isConnected = false; 
				}
				return line;
			}

			public void close(){
				if(clientSocket != null){
					try{
						clientSocket.close();
					} catch (IOException e){
						e.printStackTrace();
					} 
					clientSocket = null;
				}
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
		GenericUrl url = new GenericUrl("http://ec2-35-167-51-118.us-west-2.compute.amazonaws.com/livebasketball/leagues");
		HttpRequest request = requestFactory.buildGetRequest(url);
		Leagues leagues = new Gson().fromJson(request.execute().parseAsString(), Leagues.class);
		if(leagues != null){
			System.out.println("GET leagues successful.");
		}
		return leagues;
	} 

	public static String postLeague(League league) throws Exception {
		String requestBody = new Gson().toJson(league, League.class);
		HttpRequestFactory requestFactory =
			new NetHttpTransport().createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
					request.setParser(new JsonObjectParser(new GsonFactory()));
				}
			});
		GenericUrl url = new GenericUrl("http://ec2-35-167-51-118.us-west-2.compute.amazonaws.com/livebasketball/leagues");
		HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString("application/json", requestBody));
		Response response = new Gson().fromJson(request.execute().parseAsString(), Response.class);
		System.out.println(response.getReturnData());
		return response.getReturnData();
	} 
	
	public class Response {
		private String returnData;
		public Response(String returnData){
			this.returnData = returnData;
		}
		public String getReturnData(){return returnData;}
		public void setReturnData(String returnData){this.returnData = returnData;}
	}
} 