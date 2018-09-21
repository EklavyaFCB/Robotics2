import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.GraphicsLCD;


public class MapDrawer  {
	private static Brick myEV3 = BrickFinder.getDefault();
	public static GraphicsLCD lcd = myEV3.getGraphicsLCD();
	int r;
	int t;
	int cellSize = 15;

	int [][]map;

	public MapDrawer(int [][]m) {
		map = m;
	}
	public void initMap() {
		r = 0;
		t = 0;

		for(int i = 0; i < (map[0].length); i++) {
			r = 0;

			for(int j = 0; j < (map.length); j++) {
					if(map[i][j] == 1) {
						lcd.fillRect(j + r, i + t, cellSize, cellSize);
					} else {
						lcd.drawRect(j + r, i + t, cellSize, cellSize);

				}

				r = r + cellSize;	//Set space between the rows
			}
			t = t + cellSize; //Set space between the columns
		}

	}

	public void updateMap(int x, int y) {
		r = 0;
		t = 0;

		lcd.clear();

		for(int i = 0; i < (map[0].length); i++) {
			r = 0;

			for(int j = 0; j < (map.length); j++) {
					if((map[i][j] == 1)) {
						lcd.fillRect(j + r, i + t, cellSize, cellSize);
					}
					else if ((i == (x+1) && j == (y+1) )) {
						lcd.drawChar('R', j + r, i + t, 0);
					} else {
						lcd.drawRect(j + r, i + t, cellSize, cellSize);
				}

				r = r + cellSize;	//Set space between the rows
			}
			t = t + cellSize; //Set space between the columns
		}
	}
}
