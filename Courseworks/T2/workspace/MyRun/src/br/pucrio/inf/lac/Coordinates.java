
package br.pucrio.inf.lac;

import java.util.ArrayList;

public class Coordinates {
    
	public static double PI = 3.14159265;
    public static double TWOPI = 2*PI;	
    
    private final double latitude;
    private final double longitude;

    public Coordinates(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public boolean node_is_inside_checkpoint_area(Coordinates node, ArrayList<Coordinates> area_checkpoint){
		
		
		
		return false;
		
		
	}
	
 
}