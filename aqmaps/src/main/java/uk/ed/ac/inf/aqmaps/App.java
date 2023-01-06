package uk.ed.ac.inf.aqmaps;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.mapbox.geojson.*;

public class App 
{
	
    public static void main(String[] args) throws Exception 
    {
    	WebServer web = new WebServer(Integer.parseInt(args[6]));
    	
        
        ArrayList<Polygon> buildings = web.getNoFlyZones();
        
        var sensors = web.getSensor(args[0],args[1],args[2]);
        var w3w = web.getWords(sensors);
        
        
        var updatedSensors = web.updateSensors(sensors,w3w);
        var droneStartLong = Double.parseDouble(args[4]);
        var droneStartLat = Double.parseDouble(args[3]);
        
        Point droneStart = Point.fromLngLat(droneStartLong, droneStartLat);
        
        Drone drone = new Drone(0,droneStart);
        
        var newPointsNewAlgo = new ArrayList<Point>();
        newPointsNewAlgo.add(droneStart); //Start Node
        
        for(Sensor sensor:updatedSensors) {
        	newPointsNewAlgo.add(sensor.longlat);  //Sensors
        }
        
        newPointsNewAlgo.add(droneStart); //End Node
        
        Algorithm algo = new Algorithm(updatedSensors,newPointsNewAlgo,drone,buildings);
        var graph = algo.makeGraph(newPointsNewAlgo);
        //var sensorList = algo.getSensorList(graph);
        var graphPath = algo.path2opt(graph);
        
        List<Point> tour = graphPath.getVertexList();
        List<Point> droneTour = algo.travel(tour,buildings);
        
       
        ArrayList<Feature> droneFeaturePoints = new ArrayList<>();
        ArrayList<Point> droneListPoints = new ArrayList<>();
        
        LineString dronePathStr = LineString.fromLngLats(droneTour);
        Geometry lineStringGeoDrone = (Geometry)dronePathStr;
        Feature featureLineStr = Feature.fromGeometry(lineStringGeoDrone);
        droneFeaturePoints.add(featureLineStr);
      
        ArrayList<Sensor> notVisitedSensors = new ArrayList<Sensor>();
        for(Sensor sensor : updatedSensors) {
        		if(!algo.visitedSensors.contains(sensor))
        			notVisitedSensors.add(sensor);
        }
        
        if(!notVisitedSensors.isEmpty()) {
        	for(Sensor sensor: notVisitedSensors) {
        		Point notVisitedPoint = sensor.longlat;
        		Geometry g = (Geometry) notVisitedPoint;
        		Feature featureNotVisited = Feature.fromGeometry(g);
        		featureNotVisited.addStringProperty("rgb-string", "aaaaaa");
        		featureNotVisited.addStringProperty("marker-color", "aaaaaa");
        		featureNotVisited.addStringProperty("fill", "#aaaaaa");
        		droneFeaturePoints.add(featureNotVisited);
        	}
        }
        
        for(Sensor sensor:algo.visitedSensors) {
        	Point visitedPoint = sensor.longlat;
        	Geometry geo = (Geometry) visitedPoint;
        	Feature feature = Feature.fromGeometry(geo);
        	var battery = sensor.battery;
        	if(battery <= 10) {
				feature.addStringProperty("marker-symbol", "cross");
				feature.addStringProperty("rgb-string" , "000000");
				feature.addStringProperty("marker-color" , "000000");
				feature.addStringProperty("fill" , "#000000");
				droneFeaturePoints.add(feature);
				continue;
			}
        	
        	var reading = Double.parseDouble(sensor.reading);
        	if(reading < 0 || reading >= 256) {
				throw new Exception("Invalid Reading");				
			}
        	
        	if(0 <= reading && reading < 128) {
				//Lighthouse symbol
				feature.addStringProperty("marker-symbol", "lighthouse");
				
				if(reading < 32) {
					feature.addStringProperty("rgb-string" , "00ff00");
					feature.addStringProperty("marker-color" , "00ff00");
					feature.addStringProperty("fill" , "#00ff00");				
					droneFeaturePoints.add(feature);
					continue;
				}
				if(reading < 64) {
					feature.addStringProperty("rgb-string" , "40ff00");
					feature.addStringProperty("marker-color" , "40ff00");
					feature.addStringProperty("fill" , "#40ff00");		
					droneFeaturePoints.add(feature);				
					continue;
				}
				if(reading < 96) {
					feature.addStringProperty("rgb-string" , "80ff00");
					feature.addStringProperty("marker-color" , "80ff00");
					feature.addStringProperty("fill" , "#80ff00");	
					droneFeaturePoints.add(feature);					
					continue;
				}
				else {
					feature.addStringProperty("rgb-string" , "c0ff00");
					feature.addStringProperty("marker-color" , "c0ff00");
					feature.addStringProperty("fill" , "#c0ff00");	
					droneFeaturePoints.add(feature);				
					continue;
				}
			}
        	
        	if(reading >= 128 && reading < 256) {
        		feature.addStringProperty("marker-symbol", "danger");
				if(reading < 160) {
					feature.addStringProperty("rgb-string" , "ffc000");
					feature.addStringProperty("marker-color" , "ffc000");
					feature.addStringProperty("fill" , "#ffc000");
					droneFeaturePoints.add(feature);					
					continue;
				}
				if(reading < 192) {
					feature.addStringProperty("rgb-string" , "ff8000");
					feature.addStringProperty("marker-color" , "ff8000");
					feature.addStringProperty("fill" , "#ff8000");
					droneFeaturePoints.add(feature);				
					continue;
				}
				if(reading < 224) {
					feature.addStringProperty("rgb-string" , "ff4000");
					feature.addStringProperty("marker-color" , "ff4000");
					feature.addStringProperty("fill" , "#ff4000");
					droneFeaturePoints.add(feature);					
					continue;
				}
				else {
					feature.addStringProperty("rgb-string" , "ff0000");
					feature.addStringProperty("marker-color" , "ff0000");
					feature.addStringProperty("fill" , "#ff0000");
					droneFeaturePoints.add(feature);			
					continue;
				}
			}
        }
        
        
        for(Polygon building : buildings) {
    		Feature buildingsFeature = Feature.fromGeometry((Geometry) building);
    		buildingsFeature.addStringProperty("rgb-string", "ff0000");
    		buildingsFeature.addStringProperty("fill", "#ff0000");
    		droneFeaturePoints.add(buildingsFeature);
    	}
        
        
        FeatureCollection dronePathFeatures = FeatureCollection.fromFeatures(droneFeaturePoints);
        String droneGeoJson = dronePathFeatures.toJson();
      
        File droneJsonFile = new File("readings" + "-" + args[0] + "-" + args[1] + "-" + args[2] + ".geojson");
        
        File flightPathtxt = new File("flightpath" + "-" + args[0] + "-" + args[1] + "-" + args[2] + ".txt");
		System.out.println("Two new files created");
		
		var flightPath = drone.flightLog;
		try {
    		FileWriter jsonWriter = new FileWriter(droneJsonFile);
			jsonWriter.write(droneGeoJson);
			jsonWriter.close();
    	}
    	catch(IOException e) {
    		e.getStackTrace();
    	}
		try {
    		FileWriter txtWriter = new FileWriter(flightPathtxt);
    		for(String line: flightPath) {
    			txtWriter.write(line);
    		}
			txtWriter.close();
    	}
    	catch(IOException e) {
    		e.getStackTrace();
    	}
	
        
    }
}
