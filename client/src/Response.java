package com.patrickmichaelsen.livebasketball;

public class Response {
	private String returnData;
	public Response(String returnData){
		this.returnData = returnData;
	}
	public String getReturnData(){return returnData;}
	public void setReturnData(String returnData){this.returnData = returnData;}
}
