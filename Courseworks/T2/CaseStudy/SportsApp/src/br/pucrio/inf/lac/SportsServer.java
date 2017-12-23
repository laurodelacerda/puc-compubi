package br.pucrio.inf.lac;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;
import lac.cnclib.sddl.serialization.Serialization;
import lac.cnet.sddl.objects.ApplicationObject;
import lac.cnet.sddl.objects.Message;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory.SupportedDDSVendors;
import lac.cnet.sddl.udi.core.listener.UDIDataReaderListener;

public class SportsServer implements UDIDataReaderListener<ApplicationObject> 
{

	/** DEBUG */
	private static final String TAG = SportsServer.class.getSimpleName();

	/** SDDL Elements */
    private Object receiveMessageTopic;
    private Object toMobileNodeTopic;
    private static SddlLayer core;
    
    /** Mobile Hubs Data */
    private static final Map<UUID, UUID> mMobileHubs = new HashMap<>();
    
    /** Input reader */
    private static Scanner sc = new Scanner( System.in );
    
    /** Vectors for each sensor value */
    private static Vector<Double> vTmp = new Vector<Double>();
    private static Vector<Double> vHum = new Vector<Double>();
    private static Vector<Double> vAir = new Vector<Double>();
    
    /** TimeStamp */
    private static Vector<Long> timeStamp = new Vector<Long>();
    	 
    private SportsServer() 
    {
    	// Create a layer and participant
        core = UniversalDDSLayerFactory.getInstance(SupportedDDSVendors.OpenSplice);
        core.createParticipant(UniversalDDSLayerFactory.CNET_DOMAIN);
        
        // Receive and write topics to domain
        core.createPublisher();
        core.createSubscriber();
              
        // ClientLib Events
        receiveMessageTopic = core.createTopic(Message.class, 
        									   Message.class.getSimpleName());
       
        core.createDataReader(this, 
        					  receiveMessageTopic);
        
        // To ClientLib
        toMobileNodeTopic = core.createTopic(PrivateMessage.class, 
        									 PrivateMessage.class.getSimpleName());
        
        core.createDataWriter(toMobileNodeTopic);
    }
        
    private static void addMEPAEvents(UUID nodeDest)
    {
    	
    	// actions {add| remove | start | stop | clear | get}
    	
    	ApplicationMessage appMsgTmp = new ApplicationMessage();
    	ApplicationMessage appMsgHum = new ApplicationMessage();
    	ApplicationMessage appMsgAir = new ApplicationMessage();
	    
    	appMsgTmp.setPayloadType(PayloadSerialization.JSON);
    	appMsgHum.setPayloadType(PayloadSerialization.JSON);
    	appMsgAir.setPayloadType(PayloadSerialization.JSON);
	         
    	String msgTmp = "[{\"MEPAQuery\":" +
    					"{\"type\":\"add\"," +
    					"\"label\":\"SportsTmpAVG\"," +
    					"\"rule\":\"SELECT avg(sensorValue[1]) as value " +
								   "FROM SensorData(sensorName='Temperature').win:time_batch(10 sec)\"," +
					    "\"target\":\"global\"}}]";
    	
    	System.out.println(msgTmp);
    	
    	String msgHum = "[{\"MEPAQuery\":" +
						"{\"type\":\"add\"," +
						"\"label\":\"SportsHumAVG\"," +
						"\"rule\":\"SELECT avg(sensorValue[0]) as value " +
						   		   "FROM SensorData(sensorName='Humidity').win:time_batch(10 sec)\"," +
			   		    "\"target\":\"global\"}}]";

    	System.out.println(msgHum);
    	
    	String msgAir = "[{\"MEPAQuery\":" +
						"{\"type\":\"add\"," +
						"\"label\":\"SportsAirAVG\"," +
						"\"rule\":\"SELECT avg(sensorValue[0]) as value " +
						   		   "FROM SensorData(sensorName='Barometer').win:time_batch(10 sec)\"," +
			   		    "\"target\":\"global\"}}]";
    	
    	System.out.println(msgAir);

    	appMsgTmp.setContentObject(msgTmp);
    	appMsgHum.setContentObject(msgHum);
    	appMsgAir.setContentObject(msgAir);
    	
    	sendUnicastMSG(appMsgTmp, nodeDest);
    	sendUnicastMSG(appMsgHum, nodeDest);
    	sendUnicastMSG(appMsgAir, nodeDest);
    	   	
    }
       
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{

		new SportsServer();	
		
		do {
			
			UUID nodeDest = null;
			String message = null, result;
			
			// List of keys (UUID of the M-Hubs)
        	List<UUID> nodes = new ArrayList<UUID>(mMobileHubs.keySet());
        	
			// Destination options to select
        	System.out.println("\nSelect a node to send Sports Events:");
        	
        	for(int i = 0; i < nodes.size(); i++) 
        		System.out.println(i + ": " + nodes.get(i));
        	
        	System.out.println("r: refresh");
        	System.out.println("q: quit");
        	       	
        	// Input the value
        	do {
        		result = sc.nextLine();
        		
        		if(isNumber(result)) 
        		{
        			int option = Integer.parseInt(result);
        			
        			// If the number is outside range print an error message.
        			if(option < 0 || option >= nodes.size())
        				System.out.println("Input doesn't match specifications. Try again.");
        			else 
        			{
        				nodeDest = nodes.get(option);
        				break;
        			}
        		}
            } while( !result.equals( "r" ) && !result.equals( "q" ) );
        	
        	if(result.equals("r"))
        		continue;
        	else if(result.equals("q"))
        		break;
        	
        	// Creating MEPA Events
        	System.out.println( "\nInsert the message:" );
        	message = sc.nextLine();
//        	addMEPAEvents(nodeDest);
        	ApplicationMessage appMsg = new ApplicationMessage();
		    appMsg.setPayloadType( PayloadSerialization.JSON );
		    appMsg.setContentObject( "[" + message + "]" );
		    sendUnicastMSG( appMsg, nodeDest );
		    System.out.println("Events created!");
		    
		} while(true);
		
		 if(sc != null)
	        sc.close();

	}
    
	@Override
	public void onNewData(ApplicationObject topicSample) 
	{
		Message msg = null;
		
		if(topicSample instanceof Message) {
			
			msg = (Message) topicSample;
			UUID nodeId = msg.getSenderId();
			UUID gatewayId = msg.getGatewayId();
			
			if(!mMobileHubs.containsKey(nodeId)){
				mMobileHubs.put(nodeId, gatewayId);
				System.out.println(">>" + TAG + ": Client connected : " + nodeId);
			}
					
			String content = new String(msg.getContent());
//			String content = new String(msg.getContent()).replace("\\","");
			
//			System.out.println(content);
			
			if(content.contains("status"))
			{
				sendStatus(nodeId, gatewayId);
			}
					
			// TODO Verificar se não atrapalha as mensagens com json 
//			if (Serialization.fromJavaByteStream(msg.getContent()).equals("status"))
//			{
//				System.out.println("É um status");
//				sendStatus(nodeId, gatewayId);
//			}							
			
			JSONParser parser = new JSONParser();
			
			try 
			{
	        	JSONObject object = (JSONObject) parser.parse(content);
    			
//	        	showTimeStamps(object);
	        	
	        	String tag = (String) object.get("tag");
	        		        	
	        	switch(tag) 
	        	{
	        		// Se conteúdo da mensagem for uma solicitação de Status, envie um JSON com temp, hum, air	        			
	        		case "SensorData":
			        	showLog((String) content);
			        	handleSensorData(object);
	        		break;
	        		
	        		case "EventData":
//	        			showLog((String) content);
//	        			System.out.println("New Event!");
	        			handleEvent(object);
		        	break;
		        	
	        		case "ClientData":
//	        			showLog((String) content);
//	        			System.out.println("New Event!");
	        			handleEvent(object);
		        	break;
		        	
	        		case "ReplyData":
	        			// Do nothing
	        			
	        		case "ErrorData":
	        			// Do nothing
	        			
			        break;
	        	}
			} catch(Exception ex) {
				System.out.println(ex.getMessage());
			}
			
		}
	}
    
    public static void sendUnicastMSG(ApplicationMessage appMSG, UUID nodeID) 
    {
		PrivateMessage privateMSG = new PrivateMessage();
		privateMSG.setGatewayId(UniversalDDSLayerFactory.BROADCAST_ID);
		privateMSG.setNodeId(nodeID);
		privateMSG.setMessage(Serialization.toProtocolMessage(appMSG));
		
		sendCoreMSG(privateMSG);
    }
    
    /**
     * Writes the message (send)
     * @param privateMSG The message
     */
    private static void sendCoreMSG(PrivateMessage privateMSG) 
    {
        core.writeTopic(PrivateMessage.class.getSimpleName(), privateMSG);
    }   
    
    private void handleSensorData(JSONObject object)
    {
    	String sensor_name = (String) object.get("sensor_name");
    	   	
    	// TODO Add these values to a multidimensional array
//    	String timestamp, latitude, longitude, altitude;
    	
    	double temperature, humidity, airPressure;
    	
    	JSONArray jArray;
    	    	
//    	String tag = (String) object.get("tag");
    	   	
    	switch(sensor_name)
    	{  	
    		case "Temperature": // em graus Celcius
				jArray = ((JSONArray) object.get("sensor_value"));
				temperature = round((double) handleJSONDoublearray(jArray, 0), 2);
				vTmp.add(temperature);
				System.out.println("Input Tmp: " + temperature + "| Size: " + vTmp.size());
			break;
	
    		case "Humidity": // em qtde de moléculas por m²(?)
				jArray = ((JSONArray) object.get("sensor_value"));		
				humidity = round((double) handleJSONDoublearray(jArray, 0), 2);
				vHum.add(humidity);
				System.out.println("Input Hum: " + humidity + "| Size: " + vHum.size());
			break;

    		case "Barometer": // em milbares
				jArray = ((JSONArray) object.get("sensor_value"));		
				airPressure = round((double) handleJSONDoublearray(jArray, 0), 2);
				vAir.add(airPressure);
				System.out.println("Input Air: " + airPressure + "| Size: " + vAir.size());
			break;
    	}
    	
    }
    
    private void handleEvent(JSONObject object)
    {
    	String label = (String) object.get("label");
    	   	
    	// TODO Add these values to a multidimensional array
//    	String timestamp, latitude, longitude, altitude;
    	
    	double temperature, humidity, airPressure;
    	
    	JSONArray jArray;
    	    	
//    	String tag = (String) object.get("tag");
    	   	
    	switch(label)
    	{  	
    		case "SportsTmpAVG":
//				jArray = ((JSONArray) object.get("value"));
//				temperature = round((double) handleJSONDoublearray(jArray, 1), 2);
//				vTmp.add(temperature);
//				System.out.println("Input Tmp: " + temperature + "| Size: " + vTmp.size());
    			System.out.println("Evento: " + label);
			break;
	
    		case "SportsHumAVG":
//				jArray = ((JSONArray) object.get("value"));		
//				humidity = round((double) handleJSONDoublearray(jArray, 0), 2);
//				vHum.add(humidity);
//				System.out.println("Input Hum: " + humidity + "| Size: " + vHum.size());
    			System.out.println("Evento: " + label);
			break;

    		case "SportsAirAVG":
//				jArray = (JSONArray) object.get("value");
//				String teste = ((String) object.get("data[\"value\"]"));
//				System.out.println(teste);
//				airPressure = round((double) handleJSONDoublearray(jArray, 0), 2);
//				vAir.add(airPressure);
//				System.out.println("Input Air: " + airPressure + "| Size: " + vAir.size());
    			System.out.println("Evento: " + label);
			break;
    	}
    	
    }
    
    private void showLog(String info)
    {
    	System.out.println(info);
    }
	
	/**
	 * A simple check to see if a string is a valid number 
	 */
	public static Boolean isNumber(String s) 
	{
		try {
            Integer.parseInt(s);
        }
		catch(NumberFormatException e) {
			return false;			
		}
		return true;
	}
	
	public static double handleJSONDoublearray(JSONArray jArray, int sensor_number)
	{
			
		Double[] sensor_values = new Double[jArray.size()];
		
		for (int i = 0; i < jArray.size(); i++){
			sensor_values[i] = (Double)jArray.get(i);
		}
		
		Double value = sensor_values[sensor_number];
		
		return value;
		
	}
	
	private static String getStatus(/*location*/)
	{
		// TODO for randomness, remove it later
//		for(int i = 0; i < 10; i++){
//		    Random r = new Random(); 
//		    Double numTmp = 20.0 + r.nextDouble() * 30.0;
//		    Double numHum = 60.0 + r.nextDouble() * 30.0;
//		    Double numAir = 100000.0 + r.nextDouble() * 120000.0;
//			vTmp.add(numTmp);
//			vHum.add(numHum);
//			vAir.add(numAir);
//		}
		
		// TODO Criar exceção para vetor ainda não preenchido!

		Double valueTmp = vTmp.lastElement();	
		Double valueHum = vHum.lastElement();
		Double valueAir = vAir.lastElement();
		
		String status = analyseTemperature(valueTmp)  + "\n" 
					  + analyseHumidity(valueHum)     + "\n" 
					  + analyseAirPressure(valueAir);

		return status;
	}
	
	private static void sendStatus(UUID nodeId, UUID gatewayId)
	{
	    PrivateMessage privateMessage = new PrivateMessage();
	    
	    privateMessage.setGatewayId(gatewayId);
	    privateMessage.setNodeId(nodeId);
	    	    
		ApplicationMessage appMsg = new ApplicationMessage();
		
		String status = getStatus();
		
		appMsg.setContentObject(status);
		
		privateMessage.setMessage(Serialization.toProtocolMessage(appMsg));
		
		core.writeTopic(PrivateMessage.class.getSimpleName(), privateMessage);
	}
	
	public static Double round(Double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    Double factor = (Double) Math.pow(10, places);
	    value = value * factor;
	    Double tmp = (double) Math.round(value);
	    return (Double) tmp / factor;
	}
	
	/* TODO Finalizar função 
	 * Retorna um JSON que contém uma lista de usuários que praticam dado esporte passado como parâmetro 
	 */
	
	private static String analyseTemperature(double value)
	{
		String result = "";
		
		if(value > 40){
			result = "Valor: " + value + "°C | A temperatura está muito elevada, melhor praticar esportes indoors!";
		}
		else{	
		if((value > 30) && (value < 40)){	
			result = "Valor: " + value + "°C | A temperatura está quente, não se esqueça da hidratação e das pausas!";
		}
		else{
		if((value > 15) && (value < 30)){
			result = "Valor: " + value + "°C | A temperatura está ideal para a prática de esportes!";
		}
		else{
		if(value < 15){
			result = "Valor: " + value + "°C | Está um pouco frio. Não esqueça de pegar um casaco e de se hidratar!";
		}}}}
		
		return result;
	}
	
	private static String analyseHumidity(double value)
	{
		
		String result = "";
		
		if(value > 70){
			result = "Valor: " + value + "% | A umidade está muito elevada, há risco de chuva!";
		}
		else{
		if((value > 60) && (value < 70)){	
			result = "Valor: " + value + "% | A umidade está boa, mas cuidado com a escolha do tênis!";
		}
		else{
		if((value > 30) && (value < 60)){
			result = "Valor: " + value + "% | A umidade está ideal para a prática de esportes!";
		}
		else{
		if((value < 30)){
			result = "Valor: " + value + "% | Clima seco. Não esqueça da hidratar-se!";
		}}}}
		
		return result;	
	}
	
	private static String analyseAirPressure(double value)
	{	

		
		String result = "";
//		String result = "Value " + getLastAltitude() + " m" ;
		
		
		return result;
		
	}
	
	
	private void showTimeStamps(JSONObject object)
	{
		Long sensor_time = (Long)object.get("timestamp");
		
		timeStamp.add(sensor_time);
		
		Timestamp stamp = new Timestamp(sensor_time);
		
		Date date = new Date(stamp.getTime());
				
		System.out.println( timeStamp.lastElement() +  " - " + 
						  ( timeStamp.lastElement() - timeStamp.firstElement()) +  " - " + 
						    date);	
		
	}
	
	
	/*
	 * TODO Return a JSONObject with a group of users practicing a given sport
	 */
	private static JSONObject getUsersByCurrentSport(JSONObject object)
	{
		return object;
	}
	
	/*
	 * TODO Return a JSONObject with a group of users interested in a given sport 
	 */
	private static JSONObject getUsersBySportOfInterest(JSONObject object)
	{
		return object;
	}
	
	/*
	 * TODO Return a JSONObject with the best places for practicing a given sport
	 */
	private static JSONObject getBestVenues(JSONObject object)
	{
		return object;
	}
	
	/*
	 * TODO Add a new user to a list of active users
	 */
	private static void addNewUsers(JSONObject object)
	{
		
		
	}
	
	private static void addNewSportVenue(JSONObject object)
	{
		
	}
	
//	private static Double getLastTempCelsius()
//	{
//		return vTmp.lastElement();
//	}
//	
//	private static Double getLastAirPressure()
//	{
//		return vAir.lastElement();
//	}
//	
//	private static Double getLastAltitude()
//	{
//
//		Double alt = 44331.5 - 4946.62 * Math.pow((getLastAirPressure() / 100), 0.190263);
//		
//		return alt;
//	}
}

