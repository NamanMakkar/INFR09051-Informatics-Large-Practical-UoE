package uk.ac.ed.inf.heatmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class Reader {
	
	public File createFile(String filePath) {
		File file = new File(filePath);
		return file;
	}
	
	public int[][] createPreds(File file){
		try {
			int [][] preds = new int[10][10];
			Scanner reader = new Scanner(file);
			int numLine = 0;
			
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] vals = line.split(",");
				for(int idx=0;idx<vals.length;idx++) {
					String n = vals[idx].trim();
					preds[numLine][idx] = Integer.parseInt(n);
				}
				numLine ++;
			}
			reader.close();
			return preds;
		}
		catch(FileNotFoundException e){
			System.out.println("There is no such file in this directory");
		}
		return null ;
		
	}

}
