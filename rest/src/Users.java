package com.patrickmichaelsen.livebasketball;

import java.util.Hashtable;
import java.lang.StringBuilder;
import java.util.Iterator;

public class Users{
	private Hashtable<String,User> users;

	public Users(){
		users = new Hashtable<String,User>();
	};

	public Hashtable<String,User> getUsers(){ return users; }
	public void setUsers(Hashtable<String,User> users){
		this.users = users;
	} 

	public User get(String userId){
		return users.get(userId);
	}
	public User add(User user){
		return users.put(user.getId(), user);
	}
	public boolean containsUser(User user){
		return users.containsKey(user.getId());
	}

	@Override
	public String toString(){ 
		StringBuilder sb = new StringBuilder();
		if(users != null){
			Iterator<User> it = users.values().iterator();
			while(it.hasNext()){
				User user = (User)it.next();
				sb.append(user.toString());
				sb.append("\n");
			} 
		} 
		return sb.toString();
	}
}
