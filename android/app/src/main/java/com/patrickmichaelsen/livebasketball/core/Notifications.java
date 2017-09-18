package com.patrickmichaelsen.livebasketball.core; 

import java.util.Map;
import java.util.HashMap;
import java.lang.StringBuilder;
import java.util.Iterator;

public class Notifications{ 
	private Map<String, Notification> notifications; 
	public Notifications(){ notifications = new HashMap<String, Notification>(); }; 
	public Map<String, Notification> getNotifications(){ return notifications; }
	public void setNotifications(Map<String, Notification> notifications){ this.notifications = notifications; } 
	public Iterator<Notification> iterator(){ return notifications.values().iterator(); }

	public Notification add(Notification notification){ 
		String id = notification.hashId();
		return notifications.put(id, notification); 
	}

	public Notification remove(Notification notification){ 
		return notifications.remove(notification.getId()); 
	}

	@Override
	public String toString(){ 
		StringBuilder sb = new StringBuilder();
		if(notifications != null){
			Iterator<Notification> it = notifications.values().iterator();
			while(it.hasNext()){
				Notification notification = (Notification)it.next();
				sb.append(notification.toString());
				sb.append("\n");
			} 
		} 
		return sb.toString();
	}
}
