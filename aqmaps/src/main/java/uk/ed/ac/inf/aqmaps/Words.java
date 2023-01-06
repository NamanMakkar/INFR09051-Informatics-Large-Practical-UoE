package uk.ed.ac.inf.aqmaps;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;


public class Words {
	String country;
	Square square;
	
	//Map map = new Map();
	public Words(String words) {
		this.words = words;
	}
	
	public class Square{
		Coordinates northEast;
		Coordinates southWest;
	}
	
	
	
	String nearestPlace;
	Coordinates coordinates;
	String words;
	String language;
	String map;

}
