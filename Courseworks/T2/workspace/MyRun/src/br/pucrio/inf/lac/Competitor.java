package br.pucrio.inf.lac;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Competitor {

	
	// Competitors
    String[] competitors_names = {"Lauro", "Sheriton", "Guilherme", "Miguel", "Micaele"};
    List<String> names = Arrays.asList(competitors_names);
	private String competitor_name;
	

	// Checkpoints
	private Instant competitor_start_time;
    private List<String> checkpointList = new ArrayList<String>();
 	private int currentCheckpoint;
 		   
    // This competitor's location info
    private static Vector<Coordinates> coordinates = new Vector<Coordinates>();
    
    // This competitor's perfomance
    private static List<Long> checkpointTimes = new ArrayList<Long>();
    private static Map<String, Long> visitedCheckpoints = new HashMap<String, Long>();
    
    // Sensors
    static String[] allowedSensors = {"Temperature", "Humidity", "Barometer"};
    static List<String> sensorList = new ArrayList<String>(Arrays.asList(allowedSensors));
	private static Vector<Double> vTmp = new Vector<Double>();
    private static Vector<Double> vHum = new Vector<Double>();
    private static Vector<Double> vAir = new Vector<Double>();
        
    // Constructor
	public Competitor(Instant start, String[] listOfCheckpoints)
	{
		//TODO start position ?
		competitor_name = generateName(names);	
		
		checkpointList = Arrays.asList(listOfCheckpoints);
		
		currentCheckpoint = 0;
		
		competitor_start_time = start;
		
	}
	
	
	
	public void collectLocationData(JSONObject object)
	{
    	Double latitude	 = (Double) object.get("latitude");
    	Double longitude = (Double) object.get("longitude");
    	
    	coordinates.add(new Coordinates(latitude, 
    									longitude));
    	    	
//    	System.out.println("Coordenadas de " + getName() + " : " 
//    						+ coordinates.lastElement().getLatitude() + "  |  "  
//    						+ coordinates.lastElement().getLongitude());
	}
	
// ---------------------------------------------------------------------------------------------------
	
	public int updatePerformance(JSONObject object)
	{	
		
		String checkpoint = (String) object.get("source");	
		String sensor_name = (String) object.get("sensor_name");
		Long signal = (Long) object.get("signal");
		
		if(sensorList.contains(sensor_name))
		{
			collectWeatherStats(object);
		}
		
		if((checkpointList.get(currentCheckpoint).contains(checkpoint)) && ((signal > -75)))
		{
			Long timestamp = (Long) object.get("timestamp");
			
			checkpointTimes.add(timestamp);	
			
			visitedCheckpoints.put(checkpoint, timestamp);
			
			System.out.println(">> " + this.getName() + " passou pelo checkpoint " 
							+ checkpoint + " com o tempo de " + convertTimestamp(getLastCheckpointTime()));
					
			currentCheckpoint++;
			
			if(currentCheckpoint == checkpointList.size())
			{
				return -1;
			}
		}
		
		return currentCheckpoint;
		
	}
	
	
	
// ---------------------------------------------------------------------------------------------------
	
	public void collectWeatherStats(JSONObject object)
	{
		String sensor_name = (String) object.get("sensor_name");
		
		double sensor_data = 0;
		
		JSONArray jArray;
		
		if (sensorList.contains(sensor_name))
		{
	    	switch(sensor_name)
	    	{  	
				case "Temperature": // em graus Celcius
					jArray = ((JSONArray) object.get("sensor_value"));
					sensor_data = round((double) handleJSONDoublearray(jArray, 0), 2);
					vTmp.add(sensor_data);
				break;
		
				case "Humidity": // em qtde de moléculas por m²(?)
					jArray = ((JSONArray) object.get("sensor_value"));		
					sensor_data = round((double) handleJSONDoublearray(jArray, 0), 2);
					vHum.add(sensor_data);
				break;
		
				case "Barometer": // em milbares
					jArray = ((JSONArray) object.get("sensor_value"));		
					sensor_data = round((double) handleJSONDoublearray(jArray, 0), 2);
					vAir.add(sensor_data);
				break;
	    	}
		}
//		System.out.println(this.getName() + " hub collected " + sensor_name + " : " + sensor_data);
	}
		
	
	
	/**
	 * Returns a random name
	 * @param myList
	 * @return
	 */
	private String generateName(List<String> myList)
	{
		
		Random randomizer = new Random();
		String name = myList.get(randomizer.nextInt(myList.size()));
		
		return name;
	}
	
		
	
	
// ---------------------------------------------------------------------------------------------------------------

// Public Getters and Setters
		
	public String getName()
	{
		return competitor_name;
	}
	
		
	public int getNumberOfCheckpoints()
	{
		return currentCheckpoint;
	}


	public Duration convertTimestamp(Long timestamp)
	{
		Instant checkpoint_time = Instant.ofEpochMilli(timestamp);
		
		Duration time = Duration.between(competitor_start_time, checkpoint_time);
		
		return time;
	} 
	
	
	/**
	 * @return the visitedCheckpoints
	 */
	public Long getLastCheckpointTime() 
	{
		return checkpointTimes.get(checkpointTimes.size() - 1);
	}
	
	public Map<String, Long> getLastVisitedCheckpoints()
	{
		return visitedCheckpoints;
	}
	
	/**
	 * Parse a JSON Array to a Double Array
	 * @param jArray
	 * @param sensor_number
	 * @return
	 */
	private double handleJSONDoublearray(JSONArray jArray, int sensor_number)
	{
			
		Double[] sensor_values = new Double[jArray.size()];
		
		for (int i = 0; i < jArray.size(); i++){
			sensor_values[i] = (Double)jArray.get(i);
		}
		
		Double value = sensor_values[sensor_number];
		
		return value;
		
	}
	
	/**
	 * Round a Double to a 2-place number
	 * @param value
	 * @param places
	 * @return
	 */
	private Double round(Double value, int places) 
	{
		
	    if (places < 0) throw new IllegalArgumentException();

	    Double factor = (Double) Math.pow(10, places);
	    value = value * factor;
	    Double tmp = (double) Math.round(value);
	    
	    return (Double) tmp / factor;
	}
	
	
	public String getWeatherStatus()
	{	
		String data = "Weather: " + vTmp.lastElement() 
					  +  " C | Air: " + vHum.lastElement() 
					  +  " % | Pressure: " + vAir.lastElement();
		return data;
		
	}
	
	
}