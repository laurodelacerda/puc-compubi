package br.pucrio.inf.lac;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.junit.runner.Request;
//
//import com.google.gson.*;
//import com.google.gson.stream.JsonReader;
 
import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;

public class SportsClient implements NodeConnectionListener {
	
	private static String 		gatewayIP 	= "192.168.0.20";
	private static int 	  		gatewayPort = 4500;
	private static MrUdpNodeConnection	connection;
	
//	private Double temperature;
//	private Double humidity;
//	private Double airPressure;
	
	public static void sendPersonalData()
	{
		ApplicationMessage appMsgPersonal = new ApplicationMessage();
		appMsgPersonal.setPayloadType(PayloadSerialization.JSON);

		/* TODO JSON deve ser randômico, talvez tirado de uma lista de JSON ou criar uma classe para gerar usuários
		 * Observar:
		 * http://www.objgen.com/json?demo=true
		 * https://codebeautify.org/java-escape-unescape
		 */
		
		String msgData = "{\"tag\": \"ClientData\", " +
				"		\n \"id\": \"12345\"," +
				"		\n \"username\": \"Lauro de Lacerda Caetano\"," +
				"		\n \"sources\": [\"1-78A5048C1548\"]," +
				"		\n \"sportsOfInterest\":[\"football\",\"jogging\",\"volleyball\",\"hiking\"]," +
				"		\n \"currentCity\": \"Rio de Janeiro\"," +
				"		\n \"weight\": 80," +
				"		\n \"birthdate\": \"1992-12-13T02:00:00.000Z\"," +
				"		\n \"visibility\": true}";
		
		System.out.print(msgData);
		
		appMsgPersonal.setContentObject(msgData);
    	
		try {
	        connection.sendMessage(appMsgPersonal);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
	}

	public SportsClient() 
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
	
	public void connected(NodeConnection remoteCon) 
	{
	    ApplicationMessage msgStatus = new ApplicationMessage();
	    String serializableContent = "status" ;
	    msgStatus.setContentObject(serializableContent);
	    System.out.println(serializableContent);
	         
	    try {
	        connection.sendMessage(msgStatus);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void newMessageReceived(NodeConnection remoteCon, Message message) 
	{
		System.out.println(message.getContentObject());
		
//		String content = new String(message.getContent());
		
//	    System.out.println(Serialization.fromJavaByteStream(message.getContent()));	
		
		// TODO Converter Serial para JSON  
		
//		
//		JSONParser parser = new JSONParser();
//    	
//		try {
//			JSONObject object = (JSONObject) parser.parse(content);
//	    	temperature = (Double) object.get("temperature");
//	    	humidity = (Double) object.get("humidity");
//	    	airPressure = (Double) object.get("airPressure");
//	    	
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println(temperature);
//		System.out.println(humidity);
//		System.out.println(airPressure);
//
//    	
	}
	
	public static void main(String[] args) 
	{
	      Logger.getLogger("").setLevel(Level.OFF);
	      new SportsClient();
	      sendPersonalData();
	}
	
	public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {}
	  
	public void disconnected(NodeConnection remoteCon) {}
	 
	public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {}
	
	public void internalException(NodeConnection remoteCon, Exception e) {}
	
	/*
	 * TODO Pin or draw a retangle on Google Maps API
	 */
	public void showOnGoogleMap()
	{
		
	} 
	
}
