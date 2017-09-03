package dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Response<T> { 
	private T returnData = null; 

	public Response(){}

	public Response(T returnData){
		this.returnData = returnData;
	}

	public T getReturnData() { return returnData; }
	public void setReturnData(T returnData) { this.returnData = returnData; }
	@Override
	public String toString() {
		return "Response [returnData =" + returnData.toString()
			+ "]";
	} 
} 
