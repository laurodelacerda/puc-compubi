package br.pucrio.inf.lac;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// TODO JSONMapper para converter um JSON para Java Object
// TODO Fazer exemplos do Esper para aprender

public class SportsManager implements UDIDataReaderListener<ApplicationObject> 
{

	// DEBUG
	private static final String TAG = SportsManager.class.getSimpleName();

	// SDDL Elements
    private Object receiveMessageTopic;
    private Object toMobileNodeTopic;
    private static SddlLayer core;
    
    // Mobile Hubs Data
    private static final Map<UUID, UUID> mMobileHubs = new HashMap<>();
        
	// Competition
    //    private List<UUID> nodes = new ArrayList<UUID>(mMobileHubs.keySet());
    //public static final LocalDateTime start_time = LocalDateTime.now();
    
    public static final Instant start_time = Instant.now();    

    // Competitors
    private static Map<UUID, Competitor> competitors = new HashMap<UUID, Competitor>();
    
    // Checkpoints and Sensors
    static String[] allowedCheckpoints = {"1-001B351177C1", "1-78A5048C1548"/*, "1-B4994C64BA9F"*/};
    static List<String> checkpointsList = new ArrayList<String>(Arrays.asList(allowedCheckpoints));    

    private static ArrayList<UUID> finalClassification = new ArrayList<UUID>();
        

    static int position;
    
    private StaffClient staff;
    
    
    private SportsManager() 
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
    	
    	String msgHum = "[{\"MEPAQuery\":" +
						"{\"type\":\"add\"," +
						"\"label\":\"SportsHumAVG\"," +
						"\"rule\":\"SELECT avg(sensorValue[0]) as value " +
						   		   "FROM SensorData(sensorName='Humidity').win:time_batch(10 sec)\"," +
			   		    "\"target\":\"global\"}}]";
    	
    	String msgAir = "[{\"MEPAQuery\":" +
						"{\"type\":\"add\"," +
						"\"label\":\"SportsAirAVG\"," +
						"\"rule\":\"SELECT avg(sensorValue[0]) as value " +
						   		   "FROM SensorData(sensorName='Barometer').win:time_batch(10 sec)\"," +
			   		    "\"target\":\"global\"}}]";
    	

    	appMsgTmp.setContentObject(msgTmp);
    	appMsgHum.setContentObject(msgHum);
    	appMsgAir.setContentObject(msgAir);
    	
    	sendUnicastMSG(appMsgTmp, nodeDest);
    	sendUnicastMSG(appMsgHum, nodeDest);
    	sendUnicastMSG(appMsgAir, nodeDest);
    	    	   	
    }
    
    public static void sendUnicastMSG(ApplicationMessage appMSG, UUID nodeID) 
    {
		PrivateMessage privateMSG = new PrivateMessage();
		privateMSG.setGatewayId(UniversalDDSLayerFactory.BROADCAST_ID);
		privateMSG.setNodeId(nodeID);
		privateMSG.setMessage(Serialization.toProtocolMessage(appMSG));
		
		sendCoreMSG(privateMSG);
    }
    
    private static void sendCoreMSG(PrivateMessage privateMSG) 
    {
        core.writeTopic(PrivateMessage.class.getSimpleName(), privateMSG);
    } 
    
       
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		
		// sincronize_timers();
		
		// find_competitors();
		
		// start_race();
		
		
        // Initializing Grid
		// Grid raceCourse = new Grid();
		
		position = 1;
        
		System.out.println("Início da Maratona às " + start_time);
		
		new SportsManager();
		
	}
	    
	@Override
	public void onNewData(ApplicationObject topicSample) 
	{
		Message msg = null;
		
		if(topicSample instanceof Message) {
			
			msg = (Message) topicSample;
			UUID nodeId = msg.getSenderId();
			UUID gatewayId = msg.getGatewayId();
						
			if((!mMobileHubs.containsKey(nodeId)))
			{				
				Competitor competitor = new Competitor(start_time, allowedCheckpoints); 
				mMobileHubs.put(nodeId, gatewayId);
				competitors.put(nodeId, competitor);
//				addMEPAEvents(nodeId);
				System.out.println(">>" + TAG + " >> " + competitors.get(nodeId).getName() 
										+ " é um novo competidor e possui o hub " + nodeId) ;
			}
							
			String content = new String(msg.getContent());
			
			JSONParser parser = new JSONParser();
			
//			showLog((String) content);
			
			try 
			{
	        	JSONObject object = (JSONObject) parser.parse(content);
	        	
	        	String tag = (String) object.get("tag");
	        	
	        	switch(tag) 
	        	{     			
	        		case "SensorData":
	        			handleSensorData(nodeId, object);
	        		break;
	        		
	        		case "LocationData":
	        			handleLocationData(nodeId, object);
	        		break;
	        		
//	        		case "EventData": // Environment Events
//	        			showLog((String) content);
//	        			handleEventData(object);
	        			
	        		case "ClientData":
	        			handleClientData(nodeId, object);
	        			
        			break;	        			
	        	}
			} catch(Exception ex) {
				System.out.println(ex.getMessage());
			}
			
		}
	}

	
//----------------------------------------------------------------------------------------------------------------    
// Handlers
	
    private void handleClientData(UUID nodeId, JSONObject object) {
    	
    	String request = (String) object.get("request");
		ApplicationMessage appMsg = new ApplicationMessage();
    	appMsg.setPayloadType(PayloadSerialization.JSON);
    	
    	switch(request)
    	{  	
			case "performance":
				String data = getCompetitorsData();
				appMsg.setContentObject(data);
				sendUnicastMSG(appMsg, nodeId);
			break;
			case "weather":
				String weather = getWeatherConditions();
				appMsg.setContentObject(weather);
				sendUnicastMSG(appMsg, nodeId);
			break;
    	}
		
	}

	private void handleLocationData(UUID nodeId, JSONObject object) 
    {
    	competitors.get(nodeId).collectLocationData(object);
	}
	
	
	private void handleSensorData(UUID nodeId, JSONObject object) 
    {
		String source = (String) object.get("source");
		Long signal = (Long) object.get("signal");
		Long timestamp = (Long) object.get("timestamp");
		
		Competitor competitor = competitors.get(nodeId);
		
		int currentCheckpoint;
						
//		if(Arrays.asList(allowedCheckpoints).contains(source))
//		{
//			if((signal > -75) /*&& (timestamp > competitor.getLastCheckpointTime())*/)									
//			{
		currentCheckpoint = competitor.updatePerformance(object);
								
		if(currentCheckpoint == -1)
		{
			finalClassification.add(nodeId);
			showStats(nodeId);
		}	
//		}
//			else
//			{
//				competitor.collectWeatherStats(object);
//			}
//		}
	}
	   
//----------------------------------------------------------------------------------------------------------------

// Util Functions
	

	private String getCompetitorsData() 
	{
		String data = "";
		
		for (UUID uuid : competitors.keySet()) {
			
			Competitor competitor = competitors.get(uuid);
			
			data += "\n\n>>>>>>>> Competitor: " + competitor.getName() + "\n";	
			for (Map.Entry<String, Long> entry : competitor.getLastVisitedCheckpoints().entrySet())
			{
			    data+= entry.getKey() + "|" + competitor.convertTimestamp(entry.getValue()) + "\n";
			}	
		}
		return data;
	}
	
	private String getWeatherConditions() 
	{
		String data = "";
		
		for (UUID uuid : competitors.keySet()) {
			
			Competitor competitor = competitors.get(uuid);
			
			data += "\n\n>>>>>>>> Competitor: " + competitor.getName() + "\n";	
			data += competitor.getWeatherStatus();	
		}
		return data;
	}
	
	

	
	private void showStats(UUID nodeId)
	{
		Competitor competitor = competitors.get(nodeId);
		
		System.out.println(">>>>>>>> Winner: " + competitor.getName());
		System.out.println(competitor.getWeatherStatus());
				
		for (Map.Entry<String, Long> entry : competitor.getLastVisitedCheckpoints().entrySet())
		{
		    System.out.println(entry.getKey() + "|" + competitor.convertTimestamp(entry.getValue()));
		}	
		System.exit(0);
		
	}
	    
	private void showLog(String info)
    {
    	System.out.println(info);
    }
	
}
	