package uk.ed.ac.inf.aqmaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.mapbox.geojson.*;
import org.jgrapht.graph.*;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.tour.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Algorithm {
	
	public ArrayList<Sensor> sensors ;
	public Drone drone;
	public ArrayList<Polygon> noFlyZones;
	public ArrayList<Sensor> visitedSensors;
	public ArrayList<Point> points;
	
	public Algorithm(ArrayList<Sensor> sensors,ArrayList<Point> points,Drone drone, ArrayList<Polygon> noFlyZone) {
		this.drone = drone;
		this.sensors = sensors;
		this.noFlyZones = noFlyZone;
		this.visitedSensors = new ArrayList<Sensor>();
		this.points = points;
	}
		
	public Sensor getSensorByCoordinate(Point droneLoc, List<Sensor> sensors) throws Exception {
		for(Sensor sensor : sensors) {
			if(droneLoc.coordinates().equals(sensor.longlat.coordinates())){
				return sensor;
			}	
		}
		return new Sensor("Null", 0.0, "Null", Point.fromLngLat(0.0, 0.0));
	}
	
	public double findAngleFromSensor(Point droneLocation, Point sensorLocation) {
		var droneLong = droneLocation.longitude();
		var droneLat = droneLocation.latitude();
		var sensorLong = sensorLocation.longitude();
		var sensorLat = sensorLocation.latitude();
		
		var deltaLong = Math.abs(droneLong - sensorLong);
		var deltaLat = Math.abs(droneLat - sensorLat);
		
		if(deltaLong == 0) {
			//Checking if drone and sensor are on same longitude
			if(droneLong <= sensorLong) // This means the the sensor is exactly to the east of our drone
				return 0;
			else 
				return 180; // Sensor is exactly to the west of our drone
		}
		
		if(deltaLat == 0) {
			//Checking if drone and sensor are on same latitude
			if(droneLat <= sensorLat) //This means that the sensor is exactly in the north of our drone
				return 90;
			else
				return 3*90; //Sensor is exactly to the south of the drone 
		}
		
		var theta = (Math.atan2(deltaLat, deltaLong)*180)/Math.PI;
		//Checking for quadrants now 
		//We are in quadrant 1 if sensorLong > droneLong and sensorLat > droneLat
		//We are in quadrant 2 if sensorLong < droneLong and sensorLat > droneLat
		//We are in quadrant 3 if sensorLong < droneLong and sensorLat < droneLat
		//We are in quadrant 4 if sensorLong > droneLong and sensorLat < droneLat
		
		if((sensorLong > droneLong) && (sensorLat > droneLat))
			return theta ; //Quadrant 1
		if((sensorLong < droneLong) && (sensorLat > droneLat))
			return 180-theta; //Quadrant 2
		if((sensorLong < droneLong) && (sensorLat < droneLat))
			return 180+theta; //Quadrant 3 
		else
			return 360-theta;
	}
	
	
	
	public ArrayList<Point> travel(List<Point> points,ArrayList<Polygon>noFlyZones)throws Exception{
		var droneTour = new ArrayList<Point>();
		var startLocDrone = drone.getPosition().coordinates();
		var distancesFromDrone = new ArrayList<Double>();
		var words = "";
		for(Point sensorPoint:points) {
			var sensorLocation = sensorPoint.coordinates();
			distancesFromDrone.add(Point2D.distance(startLocDrone.get(0), startLocDrone.get(1), sensorLocation.get(0), sensorLocation.get(1)));
		}
		var idxNearestSensor = distancesFromDrone.indexOf(Collections.min(distancesFromDrone));
		var moveNumber = drone.getNumMovesMade();
		
		for(int idx = 0; idx < points.size()+1; idx++) {
			Point nextSensorPoint = points.get((idxNearestSensor + idx) % (points.size())); 
			//We use mod since we have to come back to the sensors in the list indexed before the nearest sensor
			
			Sensor thisSensor = getSensorByCoordinate(nextSensorPoint,this.sensors);
			if(drone.isInRange(nextSensorPoint, drone) && !(thisSensor.longlat.longitude()==0.0 && thisSensor.longlat.latitude() == 0.0)) {
				words = thisSensor.location;
				visitedSensors.add(thisSensor);
		   }
			
			while(!drone.isInRange(nextSensorPoint, drone)){
				if(drone.getNumMovesMade() >= GlobalVariables.MAX_MOVES)
					break;
				Point droneLoc = drone.getPosition();
				var angle = findAngleFromSensor(droneLoc,nextSensorPoint);
				angle = Math.round(angle/10.0)*10;
				droneTour.add(droneLoc);
				var nextDroneLoc = drone.nextLoc(angle);
				if(checkCollision(droneLoc,nextDroneLoc,noFlyZones)) {
					var edge = checkCollisionDrone(droneLoc,nextDroneLoc,noFlyZones);
					angle = angleOnCollision(edge);
				}
				
				drone.move(angle);
				Point newDroneLoc = drone.getPosition();
				droneTour.add(newDroneLoc);
				moveNumber++;
				Sensor newSensor = getSensorByCoordinate(nextSensorPoint,this.sensors);
				if(drone.isInRange(nextSensorPoint, drone) && !(newSensor.longlat.longitude()==0.0 && newSensor.longlat.latitude() == 0.0)) {
					words = newSensor.location;
					visitedSensors.add(newSensor);
			   }
				else 
					words=null;
				var moveNum = "" + moveNumber + ",";
				var location = "" + droneLoc.longitude() + "," + droneLoc.latitude() + ",";
				var theta = "" + angle + ",";
				var newLoc = "" + newDroneLoc.longitude() + "," + newDroneLoc.latitude() + ",";
				var stringLine = moveNum + location + theta + newLoc + words;
				System.out.println(stringLine);
				drone.updateFlightLog(stringLine);
			}
			 
		}
		return droneTour;
	}
	
	public boolean checkCollision(Point coords1, Point coords2, ArrayList<Polygon> noFlyZones) {
		//Line segment between 2 sensors	
		var long1 = coords1.longitude();
		var lat1 = coords1.latitude();
		var long2 = coords2.longitude();
		var lat2 = coords2.latitude();
		
		var line = new Line2D.Double(long1,lat1,long2,lat2);
		for(Polygon zone: noFlyZones) {
			var vertices = zone.coordinates().get(0);
			
			for(int idx = 0; idx < vertices.size() - 1; idx++) {
				var edge = new Line2D.Double(vertices.get(idx).longitude(),vertices.get(idx).latitude(),vertices.get(idx+1).longitude(),vertices.get(idx+1).latitude());
				if(line.intersectsLine(edge) || edge.intersectsLine(line)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Line2D checkCollisionDrone(Point coords1, Point coords2, ArrayList<Polygon> noFlyZones) {
		//Line segment between 2 nodes	
		var long1 = coords1.longitude();
		var lat1 = coords1.latitude();
		var long2 = coords2.longitude();
		var lat2 = coords2.latitude();
		
		var line = new Line2D.Double(long1,lat1,long2,lat2);
		for(Polygon zone: noFlyZones) {
			var vertices = zone.coordinates().get(0);
			
			for(int idx = 0; idx < vertices.size() - 1; idx++) {
				var edge = new Line2D.Double(vertices.get(idx).longitude(),vertices.get(idx).latitude(),vertices.get(idx+1).longitude(),vertices.get(idx+1).latitude());
				if(line.intersectsLine(edge) || edge.intersectsLine(line)) {
					return edge;
				}
			}
		}
		return line;
	}
	
	public double angleOnCollision(Line2D edge) {
		var p1 = edge.getP1();
		var p2 = edge.getP2();
		
		var x1 = p1.getX();
		var y1 = p1.getY();
		
		var x2 = p2.getX();
		var y2 = p2.getY();
		
		var deltaY = Math.abs(y1-y2);
		var deltaX = Math.abs(x1-x2);
		
		var angle = (Math.atan2(deltaY,deltaX));
		if(angle*180/Math.PI == 360)
			return 0;
		if(angle<0)
			return Math.round(((angle + 2*Math.PI)*180/Math.PI)/10)*10.0;
		return Math.round((angle*180/Math.PI)/10)*10.0;
	}
	
	
	
	
	public DefaultUndirectedWeightedGraph<Point, DefaultEdge> makeGraph(ArrayList<Point> coords){
		var graph = new DefaultUndirectedWeightedGraph(DefaultEdge.class);
		for(int i =0; i<coords.size(); i++) {
			graph.addVertex(coords.get(i));
		}
		for(int idx1 = 0; idx1 < coords.size(); idx1++) {
			for(int idx2 = 0; idx2 < coords.size(); idx2++) {
				var coords1 = coords.get(idx1);
				var coords2 = coords.get(idx2);
				if(coords1.equals(coords2))
					continue;
				else if(checkCollision(coords1,coords2,noFlyZones)) {
					graph.addEdge(coords1, coords2);
					graph.setEdgeWeight(coords1,coords2,1000000000000000000000000000000000000000.0);
				}
				
				else {
					graph.addEdge(coords1, coords2);
					var long1 = coords1.longitude();
					var lat1 = coords1.latitude();
					var long2 = coords2.longitude();
					var lat2 = coords2.latitude();
					
					var dist = Point2D.distance(long1,lat1,long2,lat2);
					graph.setEdgeWeight(coords1, coords2,dist);
				}
			}
		}
		return graph;
	}
	
	public GraphPath<Point, DefaultEdge> path2opt(DefaultUndirectedWeightedGraph<Point, DefaultEdge> graph){
		var twoOpt = new TwoOptHeuristicTSP();
		var tour = twoOpt.getTour(graph);
		var path = tour.getVertexList(); 
		return tour;
	}
	
}
