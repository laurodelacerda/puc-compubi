package br.pucrio.inf.lac;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;
import lac.cnclib.sddl.message.Message;

public class StaffClient implements NodeConnectionListener {
	
	private static String 		gatewayIP 	= "139.82.153.50";
	private static int 	  		gatewayPort = 4500;
	private static MrUdpNodeConnection	connection;

		
	public StaffClient() 
	{
		InetSocketAddress address = new InetSocketAddress(gatewayIP, gatewayPort);
		try {
			connection = new MrUdpNodeConnection();
			connection.addNodeConnectionListener(this);
			connection.connect(address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void connected(NodeConnection remoteCon) 
	{
	    ApplicationMessage msgStatus = new ApplicationMessage();
	    msgStatus.setPayloadType(PayloadSerialization.JSON);

	         
	    try {
	        connection.sendMessage(msgStatus);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	@Override
	public void newMessageReceived(NodeConnection remoteCon, Message message) {
		// TODO Auto-generated method stub
		
		System.out.println(message.getContentObject());
		
	}
	
	public static void sendRequest()
	{	
		ApplicationMessage msgStatus = new ApplicationMessage();
	    msgStatus.setPayloadType(PayloadSerialization.JSON);
	    
	    String msgData = "{\"tag\":\"ClientData\"," +
	    				 "\"request\":\"performance\"}";
	    
//	    String serializableContent = request ;
//	    msgStatus.setContentObject(serializableContent);
	    
	    msgStatus.setContentObject(msgData);
	    
	    try {
	        connection.sendMessage(msgStatus);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	@Override
	public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {}
	  
	@Override
	public void disconnected(NodeConnection remoteCon) {}
	 
	@Override
	public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {}
	
	@Override
	public void internalException(NodeConnection remoteCon, Exception e) {}
	
	public static void main(String[] args) 
	{
	      Logger.getLogger("").setLevel(Level.OFF);      
//	      int option = 0; 
	      new StaffClient();
	      
	      sendRequest();
//	      while(true)
//	      {
//	    	  switch(option){
//	    	  	case 1:
//	    	  		sendRequest("weather");
//    	  		break;
//	    	  	case 2:
//	    	  		sendRequest();
//    	  		break;
//	    	  }
//	    	  
//	      }

	}

	

}