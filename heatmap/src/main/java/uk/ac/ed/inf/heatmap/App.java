package uk.ac.ed.inf.heatmap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

/**
 * Hello world!
 *
 */
public class App 
{
	public static Reader reader = new Reader();
	public static Map map = new Map();
    public static void main( String[] args) throws Exception
    {
    	String path = args[0];
    	File file = reader.createFile(path);
    	int[][] predictions = reader.createPreds(file);
    	ArrayList<Feature> grid = map.buildMap(map.corners, predictions);
    	FeatureCollection collection = FeatureCollection.fromFeatures(grid);
    	String geojson = collection.toJson();
    	
    	File jsonFile = new File("heatmap.geojson");
		System.out.println("New file created");
    	
    	try {
    		FileWriter jsonWriter = new FileWriter("heatmap.geojson");
			jsonWriter.write(geojson);
			jsonWriter.close();
    	}
    	catch(IOException e) {
    		e.getStackTrace();
    		
    	}
    	
    }
}