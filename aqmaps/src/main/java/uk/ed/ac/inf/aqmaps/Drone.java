package uk.ed.ac.inf.aqmaps;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import com.mapbox.geojson.Point;


public class Drone {

	private  int numMoves;
	private Point location;
	public ArrayList<String> flightLog;
	final  int maxMoves = 150;
	final  double lenPerMove = 0.0003;
	final  double minDistSensor = 0.0002;
	
	
	public Drone(int numMoves, Point location) {
		this.numMoves = numMoves;
		this.location = location;
		this.flightLog = new ArrayList<String>();
		
	}
	
	public int returnNumLegalMoves(int numMoves) {
		return maxMoves - numMoves;
	}
	public boolean isHeadingValid(double heading) {
		return (heading % 10 == 0) && !(heading < 0) && (heading <= 360);
	}
	public Point getPosition() {
		return this.location;
	}
	public double getLat() {
		return this.location.latitude();
	}
	public double getLong() {
		return this.location.longitude();
	}
	public int getNumMovesMade() {
		return numMoves;
	}
	public Point nextLoc(double heading) {
		var lat = this.location.latitude();
		lat += (Math.sin(Math.PI*heading/180))*GlobalVariables.RADIUS;
		var lng = this.location.longitude() ;
		lng += (Math.cos(Math.PI*heading/180))*GlobalVariables.RADIUS;
		var loc = Point.fromLngLat(lng,lat);
		return loc;
	}
	public void move(double heading) throws Exception {
		if(this.numMoves >= GlobalVariables.MAX_MOVES) {
			throw new IllegalArgumentException("Out of moves !");
		}
		if(!isHeadingValid(heading)) {
			throw new IllegalArgumentException("Heading must be a multiple of 10 such that 0 <= heading <= 360 !");
		}
		var lat = this.location.latitude();
		lat += (Math.sin(Math.PI*heading/180))*GlobalVariables.RADIUS;
		var lng = this.location.longitude() ;
		lng += (Math.cos(Math.PI*heading/180))*GlobalVariables.RADIUS;
		this.location = Point.fromLngLat(lng, lat);
		this.numMoves ++;
	}
	public boolean isInRange(Point sensor, Drone drone) {
		var range = Point2D.distance(sensor.coordinates().get(0), sensor.coordinates().get(1), drone.getPosition().coordinates().get(0), drone.getPosition().coordinates().get(1));
		if(range <= GlobalVariables.DIST_FOR_READING)
			return true;
		return false;
	}
	public void updateFlightLog(String line) {
		this.flightLog.add(line);
		this.flightLog.add("\n");
	}
}
