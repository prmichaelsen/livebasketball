package com.patrickmichaelsen.livebasketball; 

import java.util.List;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.util.Iterator;

public class Notifications{ 
	private List<Notification> notifications; 
	public Notifications(){ notifications = new ArrayList<Notification>(); }; 
	public List<Notification> getNotifications(){ return notifications; }
	public void setNotifications(List<Notification> notifications){ this.notifications = notifications; } 
	public Notification add(Notification notification){ return notifications.add(notification); }

	@Override
	public String toString(){ 
		StringBuilder sb = new StringBuilder();
		if(notifications != null){
			Iterator<Notification> it = notifications.iterator();
			while(it.hasNext()){
				Notification notification = (Notification)it.next();
				sb.append(notification.toString());
				sb.append("\n");
			} 
		} 
		return sb.toString();
	}
}
