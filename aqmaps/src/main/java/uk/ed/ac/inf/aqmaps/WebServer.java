package uk.ed.ac.inf.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class WebServer {
	static HttpClient client;
	static int port;
	
	public WebServer(int port) {		
		this.client = HttpClient.newHttpClient();
		this.port = port;
	}
	
	public  String http(String urlString) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
		var response = client.send(request, BodyHandlers.ofString());	
		var body = response.body();	
		return body;
	}
	public ArrayList<Polygon> getNoFlyZones() throws IOException, InterruptedException{
		String buildingInfoJson = http("http://localhost:" + this.port + "/buildings/no-fly-zones.geojson");
		var polygons = new ArrayList<Polygon>();
		var featuresList= FeatureCollection.fromJson(buildingInfoJson).features();
		for(var feature:featuresList) {
			var geometry = feature.geometry();
			if(geometry instanceof Polygon) {
				polygons.add((Polygon) geometry);
			}
		}
		return polygons;
	}
	public ArrayList<Sensor> getSensor(String day, String month, String year) throws IOException, InterruptedException {
		Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
		var body = http(String.format("http://localhost:" + this.port + "/maps/%s/%s/%s/air-quality-data.json",year,month,day));
		ArrayList<Sensor> sensors = new Gson().fromJson(body,listType);
		return sensors;
	}
	
	public ArrayList<Words> getWords(ArrayList<Sensor> sensors) throws IOException, InterruptedException{
		var w3w = new ArrayList<Words>();
		String[][] locationWords = new String[sensors.size()][3];
		String[] parsingWords = new String[locationWords.length];
		for(var sensor:sensors) {
			locationWords[sensors.indexOf(sensor)] = sensor.location.split("\\.");
		}
		for(int i = 0; i < parsingWords.length; i++) {
			parsingWords[i] = http(String.format("http://localhost:" + this.port +"/words/%s/%s/%s/details.json",locationWords[i][0],locationWords[i][1],locationWords[i][2]));
		}
		for(int i = 0; i < parsingWords.length; i++) {
			w3w.add(new Gson().fromJson(parsingWords[i], Words.class));
		}
		return w3w;
	}
	public ArrayList<Sensor> updateSensors(ArrayList<Sensor> sensors, ArrayList<Words> w3w){
		var updatedSensors = new ArrayList<Sensor>();
		for(Sensor sensor : sensors) {
			for(Words word:w3w) {
				if(sensor.location.equals(word.words)) {
				var coordinates = sensor.returnCoordinates(word);
				Point updated = Point.fromLngLat(coordinates[0], coordinates[1]);
				updatedSensors.add(new Sensor(sensor.location,sensor.battery,sensor.reading,updated));
				}
			}
		}
		return updatedSensors;
	}
}
