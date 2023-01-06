package uk.ac.ed.inf.heatmap;

import com.mapbox.geojson.*;

import java.util.ArrayList;



public class Map {

	static int NumSquaresAlongLongitude = 10;
	//var list = new ArrayList<Integer>();
	static int NumSquaresAlongLatitude = 10;
	static double[] coordinates = {55.946233,(-3.192473),55.946233,(-3.184319),55.942617,(-3.192473),55.942617,(-3.184319)};
	
	public double getLenofRectAlongLatitude() {
		return (coordinates[0] - coordinates[4])/NumSquaresAlongLongitude;
	}
	public double getLenofRectAlongLongitude() {
		return (coordinates[3] - coordinates[1])/NumSquaresAlongLatitude;
	}
	
	static Point northWest = Point.fromLngLat(coordinates[1],coordinates[0]);
	static Point northEast = Point.fromLngLat(coordinates[3], coordinates[2]);
	static Point southWest = Point.fromLngLat(coordinates[5], coordinates[4]);
	static Point southEast = Point.fromLngLat(coordinates[7], coordinates[6]);
	
	static Point[] corners = {northWest,northEast,southWest,southEast};
	
	public ArrayList<Feature> buildMap(Point[] array, int[][] predictions) {
		
		ArrayList<Feature> map = new ArrayList<Feature>();
		double longitudeNW = array[0].longitude();
		double latitudeNW = array[0].latitude();
		double lng = getLenofRectAlongLongitude();
		double lat = getLenofRectAlongLatitude();
		
		for(int i = 0; i <= NumSquaresAlongLongitude - 1;i++) {
			for(int j = 0; j <= NumSquaresAlongLatitude - 1;j++) {
				// This collects the corners of each square in the grid and stores them in a List of points
				Point point1 = Point.fromLngLat(longitudeNW + (lng*j), latitudeNW - (lat*i));
				Point point2 = Point.fromLngLat(longitudeNW + (lng*(j + 1)), latitudeNW - (lat*i));
				Point point3 = Point.fromLngLat(longitudeNW + (lng*(j + 1)), latitudeNW - (lat*(i + 1)));
				Point point4 = Point.fromLngLat(longitudeNW + (lng*j), latitudeNW - (lat*(i + 1)));
				
				ArrayList<Point> points = new ArrayList<Point>();
				// This list will be used for building the grid
				java.util.List<java.util.List<Point>> polygonPoints = new ArrayList<java.util.List<Point>>();
				
				points.add(point1);
				points.add(point2);
				points.add(point3);
				points.add(point4);
				points.add(point1);
				polygonPoints.add(points);
				
				Geometry polygon = (Geometry) Polygon.fromLngLats(polygonPoints);
				
				Feature polygonFeature = Feature.fromGeometry(polygon);
				polygonFeature.addNumberProperty("fill-opacity", 0.75);
				
				map.add(polygonFeature);
			}
		}
		
		for (int i = 0; i < NumSquaresAlongLongitude; i++) {
			for(int j = 0; j < NumSquaresAlongLatitude; j++) {
				PollutionReading reading = new PollutionReading(predictions[i][j]);
				map.get(i*NumSquaresAlongLongitude + j).addStringProperty("rgb-string", reading.convertToRGB());
				map.get(i*NumSquaresAlongLongitude + j).addStringProperty("fill", reading.convertToRGB());
			}
			
		}
		return map;	
	}
}
