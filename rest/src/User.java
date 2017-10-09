package com.patrickmichaelsen.livebasketball;

public class User {
	private String username;
	private String pushToken;
	private String id;

	public User(){
		username = "";
		pushToken = "";
		id = "";
	};

	public void setUsername(String username){ this.username = username; };
	public void setPushToken(String pushToken){ this.pushToken = pushToken; };
	public void setId(String id){ this.id = id; };

	public String getUsername(){ return username; }
	public String getPushToken(){ return pushToken; }
	public String getId(){
		if(id.equals("")){
			this.setId(username);
		}
		return id;
	}

	@Override
	public String toString(){ return this.getUsername(); }
}
