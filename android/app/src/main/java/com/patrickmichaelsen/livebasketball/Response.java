package com.patrickmichaelsen.livebasketball;

/**
 * Created by Patrick on 8/23/2017.
 */

public class Response {
    private String returnData;
    public Response(String returnData){
       this.returnData = returnData;
    }
    public String getReturnData(){return returnData;}
    public void setReturnData(String returnData){this.returnData = returnData;}
}
