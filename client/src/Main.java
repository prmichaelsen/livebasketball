package com.patrickmichaelsen.livebasketball;

import com.patrickmichaelsen.livebasketball.core.*;
import com.google.api.client.http.ByteArrayContent;
import javax.imageio.ImageIO;
import java.io.InputStream;
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
import java.util.Iterator;
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

	static Leagues leagues;
	static ServerSocket serverSocket;
	
	static Toolkit tk;
	static JTable table;
	static JFrame frm;
	static JTextArea matchesTextArea; 
	final static PopupMenu popup = new PopupMenu();
	final static Image iconImage = createImage("icon.gif", "tray icon");
	final static TrayIcon trayIcon = new TrayIcon(iconImage);
	final static SystemTray tray = SystemTray.getSystemTray();

	static boolean playSounds;
	static boolean displayPopups;

	//PROD settings
	final static String HOST = "ec2-35-167-51-118.us-west-2.compute.amazonaws.com";
	final static String URI = "http://ec2-35-167-51-118.us-west-2.compute.amazonaws.com";
	final static int PORT = 6789;

	/* 
	//DEV setting
	final static String HOST = "localhost";
	final static String URI = "http://localhost:8081";
	final static int PORT = 6789;
	*/

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

		//start worker thread
		Thread refresher = new Thread(new Main().new Refresher(), "Livebasketball Refresher");
		refresher.setUncaughtExceptionHandler(h);
		refresher.start();
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
		InputStream stream = Main.class.getClassLoader().getResourceAsStream("resources/icons/"+path); 
		Image image = null;
		try{
			image = new ImageIcon(ImageIO.read(stream), description).getImage(); 
		} catch (IOException e){
			e.printStackTrace();
		}
		return image;
	} 

	public class Refresher implements Runnable {
		public void run(){
			while(true){
				//handle notifications
				Notifications notifications = null;
				try{
					notifications = getNotifications();
				}
				catch( Exception e ){
					e.printStackTrace();
				}
				if(notifications != null){
					Iterator<Notification> it = notifications.iterator();	
					while(it.hasNext()){
						Notification notification = it.next();
						displayNotification(notification);
						try{
							deleteNotification(notification);
						}
						catch( Exception e ){
							e.printStackTrace();
						}
					}
				}
				//handle leagues
				try{
					if(table != null){ 
						table.setModel(new LeagueTableModel(getLeagues()));
						table.getColumnModel().getColumn(0).setMaxWidth(20);
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				//space out requests
				try{
					TimeUnit.SECONDS.sleep(1);
				} catch(InterruptedException e){
					e.printStackTrace();
				}; 
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
							notification.toMessage()); 
				}
			});
			popupThread.start();
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
		GenericUrl url = new GenericUrl(URI+"/livebasketball/leagues");
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
		GenericUrl url = new GenericUrl(URI+"/livebasketball/leagues");
		HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString("application/json", requestBody));
		Response response = new Gson().fromJson(request.execute().parseAsString(), Response.class);
		System.out.println(response.getReturnData());
		return response.getReturnData();
	} 

	public static Notifications getNotifications() throws Exception {
		HttpRequestFactory requestFactory =
			new NetHttpTransport().createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
					request.setParser(new JsonObjectParser(new GsonFactory()));
				}
			});
		GenericUrl url = new GenericUrl(URI+"/livebasketball/notifications");
		HttpRequest request = requestFactory.buildGetRequest(url);
		Notifications notifications = new Gson().fromJson(request.execute().parseAsString(), Notifications.class);
		if(notifications != null){
			System.out.println("GET notifications successful.");
		}
		return notifications;
	} 

	public static String deleteNotification(Notification notification) throws Exception {
		String requestBody = new Gson().toJson(notification, Notification.class);
		HttpRequestFactory requestFactory =
			new NetHttpTransport().createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
					request.setParser(new JsonObjectParser(new GsonFactory()));
				}
			});
		GenericUrl url = new GenericUrl(URI+"/livebasketball/notifications");
		HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString("application/json", requestBody));
		Response response = new Gson().fromJson(request.execute().parseAsString(), Response.class);
		System.out.println(response.getReturnData());
		return response.getReturnData();
	} 
} 
