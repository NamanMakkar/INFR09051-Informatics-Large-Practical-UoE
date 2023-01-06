package uk.ac.ed.inf.heatmap;

public class PollutionReading {
	public static int pollutionReading;
	
	public PollutionReading(int reading) {
		this.pollutionReading = reading;
	}
	
	public String convertToRGB() {
		int pollution = this.pollutionReading;
		if (0 > pollution || pollution > 256) {
			return "#aaaaaa"; // Return grey if not visited
			}
		else if(pollution < 32) {
			return "#00ff00";
		}
		else if(pollution < 64) {
			return "#40ff00";
		}
		else if(pollution < 96) {
			return "#80ff00 ";
		}
		else if(pollution < 128) {
			return "#c0ff00";
		}
		else if(pollution < 160) {
			return "#ffc000";
		}
		else if(pollution < 192) {
			return "#ff8000";
		}
		else if(pollution < 224) {
			return "#ff4000";		
		}
		else if(pollution < 256) {
			return "#ff0000";
		}
		return "#000000"; // Return Black if battery is low. Ambiguous
	}

}
