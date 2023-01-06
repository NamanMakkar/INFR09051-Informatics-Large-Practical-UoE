package uk.ed.ac.inf.aqmaps;

public class Coordinates {
	double lng;
	double lat;
	boolean visited;
	public Coordinates(double lng, double lat, boolean visited) {
		this.lng = lng;
		this.lat = lat;
		this.visited = visited;
	}
	public void setVisitedTrue() {
		this.visited = true;
	}
	public double getLong() {
		return lng;
	}
	public double getLat() {
		return lng;
	}
	public String toString() {
		return "(" + lng + ", " + lat + ")";
	}
}
