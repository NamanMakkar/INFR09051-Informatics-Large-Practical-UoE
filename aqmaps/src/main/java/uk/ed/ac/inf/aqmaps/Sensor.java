package uk.ed.ac.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;


public class Sensor {
	String location;
	double battery;
	String reading;
	Point longlat;
	
public Sensor(String location, double battery, String reading, Point longlat) {
		this.location = location;
		this.battery = battery;
		this.reading = reading;
		this.longlat = longlat;
	} 
	


public double[] returnCoordinates(Words w3w) {
	double[] coordinates = new double[2];
	if(this.location.equals(w3w.words)) {
		coordinates[0] = w3w.coordinates.lng;
		coordinates[1] = w3w.coordinates.lat;
	}
	return coordinates;
}


}
