import java.awt.List;
import java.util.ArrayList;


import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Keys;
import lejos.hardware.lcd.GraphicsLCD;


public class PositionDetector {
	private int [][]map;
	private PilotRobot pilotRobot;
	private static int finalX;
	private static int finalY;
	private static int finalFacing;
	private int plusX;
	private int minusX;
	private int plusY;
	private int minusY;
	private static float tilesize;
	private static Brick myEV3;
	private ArrayList<Integer> possiblePositionsX;
	private ArrayList<Integer> possiblePositionsY;
	private ArrayList<Integer> facing;
	private String step;

	public PositionDetector(int [][]inputMap) {
		map = inputMap;

		possiblePositionsX = new ArrayList<Integer>();
		possiblePositionsY = new ArrayList<Integer>();
		facing = new ArrayList<Integer>();

		myEV3 = BrickFinder.getDefault();

		pilotRobot = new PilotRobot(myEV3);
		tilesize = (float)0.25;
		pilotRobot.getPilot().setAngularSpeed(20);
		pilotRobot.getPilot().setLinearSpeed(10);

		getTiles();
	}

	public PilotRobot getPilot() {
		return this.pilotRobot;
	}

	//------------------------------------------------------------
	//METHOD THAT RETURNS THE NUMBER OF TILES IN EACH DIRECTION
	//------------------------------------------------------------
	private void getTiles() {
		float tmp = 0;

		tmp = distanceSensor(0);
		plusY = (int)(tmp/tilesize);

		tmp = distanceSensor(-90);
		plusX = (int)(tmp/tilesize);

		tmp = distanceSensor(90);
		minusX = (int)(tmp/tilesize);

		pilotRobot.getPilot().rotate(180);

		tmp = distanceSensor(0);
		minusY = (int)(tmp/tilesize);

		pilotRobot.getUltrasonicMotor().rotateTo(0);
		pilotRobot.getPilot().rotate(180);
	}

	//------------------------------------------------------------
	//METHOD THAT MEASURES AN ACCURATE DISTANCE BASED ON SEVERAL
	//READINGS AND AVERAGE
	//------------------------------------------------------------
	public float distanceSensor(int rotate) {
		float []sensorReading = new float[3];
		float finalReading = 0;

		pilotRobot.getUltrasonicMotor().rotateTo(rotate);

		sensorReading[0]=pilotRobot.getUltrasonicSensor1();
		pilotRobot.getUltrasonicMotor().rotate(-10);
		sensorReading[1]=pilotRobot.getUltrasonicSensor1();
		pilotRobot.getUltrasonicMotor().rotate(20);
		sensorReading[2]=pilotRobot.getUltrasonicSensor1();

		pilotRobot.getUltrasonicMotor().rotateTo(rotate);

		finalReading=(sensorReading[0] + sensorReading[0] + sensorReading[0])/3;

		sensorReading[0]=pilotRobot.getUltrasonicSensor1();
		pilotRobot.getUltrasonicMotor().rotate(-10);
		sensorReading[1]=pilotRobot.getUltrasonicSensor1();
		pilotRobot.getUltrasonicMotor().rotate(20);
		sensorReading[2]=pilotRobot.getUltrasonicSensor1();
		pilotRobot.getUltrasonicMotor().rotateTo(rotate);

		finalReading=(sensorReading[0] + sensorReading[0] + sensorReading[0] + finalReading)/4;
		 //To account for infinity values
		if (finalReading > 5) {
			finalReading = 0;
		}
		return finalReading;
	}

	//------------------------------------------------------------
	//METHOD THAT CALCULATES THE POSITION OF THE ROBOT
	//------------------------------------------------------------
	public void getPosition() {
		int comparabilityCount = 0;
		boolean done = false;
		int positionCount = 0;
		int n = 1;
		int i,j = 0;
		int x1 = 0,x2 = 0,y1 = 0,y2 = 0;
		boolean compare = false;

		//For the 4 directions
		do {
			for (n = 1; n <= 4; n++) {

				for ( i = 1; i < map.length-1;i++) {
					for (j = 1; j < map[0].length-1;j++) {

						//Compare all tiles in the map to see if they match the scanned requirements
						if (map[i][j] != 1)
							comparabilityCount = compareY(i,j) + compareX(i,j);

						//If a tile does match the requirements, add the x,y and facing positions to 3 separate lists
						if(comparabilityCount == 4) {
							possiblePositionsX.add(i);
							possiblePositionsY.add(j);
							facing.add(n);
							positionCount++;
							comparabilityCount = 0;
						}
					}
				}
				//Compare all possible ways the robot could be facing
				rotate90Degrees();
			}


			//If there is only one possible position in the list, take that as the final position
			if (positionCount == 1) {
				finalX = possiblePositionsX.get(0);
				finalY = possiblePositionsY.get(0);;
				finalFacing = facing.get(0);
				done = true;
			}

			//If the robot needs to compare a position after a second movement
			if (compare == true) {
				int plusYCount = 0;
				int plusXCount = 0;
				int minusYCount = 0;
				int minusXCount = 0;
				int tmp = 0;

				//Ckecking wether there is just one possible position it cold be in based on repeated
				//facing values (the robot will always be facing the same direction at the start)
				for (i = 0; i < facing.size(); i++) {
					if(facing.get(i) == 1)
			    		  plusYCount++;
			    	  if(facing.get(i) == 2)
			    		  plusXCount++;
			    	  if(facing.get(i) == 3)
			    		  minusYCount++;
			    	  if(facing.get(i) == 4)
			    		  minusXCount++;
				}
				if (plusYCount == 2)
					tmp++;
				if (plusXCount == 2)
					tmp++;
				if (minusYCount == 2)
					tmp++;
				if (minusXCount == 2)
					tmp++;

				//If there is only one possible position, take that one else, compare the available equal positions
				//based on the movement the robot took
				if (tmp == 1) {
					for (i = 0; i < facing.size()-1; i++)
						   for (int k = i+1; k < facing.size(); k++)
						      if(facing.get(i) == facing.get(k)) {
						         finalFacing = facing.get(k);
						         finalX = possiblePositionsX.get(k);
						         finalY = possiblePositionsY.get(k);
						         done = true;
						      }
				} else {
					do {
					for (i = 0; i < facing.size()-1; i++)
						   for (int k = i+1; k < facing.size(); k++)
						      if(facing.get(i) == facing.get(k)) {
						         x1 = possiblePositionsX.get(i);
						         x2 = possiblePositionsX.get(k);
						         y1 = possiblePositionsY.get(i);
						         y2 = possiblePositionsY.get(k);


						         //Check if the robot were able to move from position 1 to to after
						         //the movement it took
						         if(canGoFrom(x1,x2,y1,y2)) {
						        	 finalFacing = facing.get(k);
							         finalX = possiblePositionsX.get(k);
							         finalY = possiblePositionsY.get(k);
							         done = true;
						         }
						         else {
						        	 //It is not possible for it to be in this position so remmove it and
						        	 //compare again
						        	 facing.remove(k);
						        	 possiblePositionsX.remove(k);
						        	 finalY = possiblePositionsY.remove(k);
						         }
						      }

					} while (done == false);
				}

			}

			//If there is more than one possible position after the first scan, move the robot
			//and scan again
			if (compare == false) {
				if (possiblePositionsX.size() > 1) {


					if (canGoFront()) {
						pilotRobot.getPilot().travel((tilesize*100));
						step = "UP";
					}
					else if (canGoRight()) {
						pilotRobot.getPilot().rotate(90);
						pilotRobot.getPilot().travel((tilesize*100));
						pilotRobot.getPilot().rotate(-90);
						step = "RIGHT";
					} else if (canGoLeft()) {
						pilotRobot.getPilot().rotate(-90);
						pilotRobot.getPilot().travel((tilesize*100));
						pilotRobot.getPilot().rotate(90);
						step = "LEFT";
					} else {
						pilotRobot.getPilot().rotate(180);
						pilotRobot.getPilot().travel((tilesize*100));
						pilotRobot.getPilot().rotate(180);
						step = "DOWN";
					}

					getTiles();
					pilotRobot.getPilot().travel(-(tilesize*100));
					compare = true;
				}
			}

			positionCount = 0;

		} while (done == false);
	}

	//------------------------------------------------------------
	//METHOD THAT CHECKS WETHER THE ROBOT CAN BE IN A CERTAIN
	//POSITION BASED ON THE MOVEMENT IT TOOK
	//------------------------------------------------------------
	private boolean canGoFrom(int x1, int x2, int y1, int y2) {
		if (step == "RIGHT") {
			if((y2 - y1) == 1)
				return true;
		}
		else if (step == "LEFT") {
			if((y1 - y2) == 1)
				return true;
		}
		else if (step == "UP") {
			if((x1 - x2) == 1)
				return true;
		}
		else if (step == "DOWN") {
			if((x2 - x1) == 1)
				return true;
		}

		return false;
	}

	//------------------------------------------------------------
	//CHECKS IF THE ROBOT CAN MOVE RIGHT
	//------------------------------------------------------------
	public boolean canGoRight() {
		float sensorReading = pilotRobot.getUltrasonicSensorRight1();

		if(sensorReading < Float.POSITIVE_INFINITY) {
			if(sensorReading <= tilesize)
				return false;
		}

		return true;
	}

	//------------------------------------------------------------
	//CHECKS IF THE ROBOT CAN MOVE LEFT
	//------------------------------------------------------------
	public boolean canGoLeft() {
		float sensorReading = pilotRobot.getUltrasonicSensorLeft1();

		if(sensorReading < Float.POSITIVE_INFINITY) {
			if(sensorReading <= tilesize)
				return false;
		}

		return true;
	}

	//------------------------------------------------------------
	//CHECKS IF THE ROBOT CAN MOVE FORWARD
	//------------------------------------------------------------
	public boolean canGoFront() {
		float sensorReading = pilotRobot.getUltrasonicSensor1();

		if(sensorReading < Float.POSITIVE_INFINITY) {
			if(sensorReading <= tilesize)
				return false;
		}

		return true;
	}


	public int getX() {
		return finalX - 1;
	}

	public int getY() {
		return finalY - 1;
	}

	public int getFacing() {
		return finalFacing;
	}

	//------------------------------------------------------------
	//ROTATES THE COORDINATES 90 DEGREES
	//------------------------------------------------------------
	private void rotate90Degrees(){
		int tmp = 0;
		int tmp2 = 0;
		tmp = plusX;
		plusX = plusY;
		plusY = minusX;
		minusX = minusY;
		minusY = tmp;
	}

	//------------------------------------------------------------
	//COMPARE THE X COORDINATES
	//------------------------------------------------------------
	private int compareX(int i, int j) {
		int XComparabilityCount = 0;
		int count = 0;
		int r = 1;
		int m = j + 1;
		boolean obstacle = false;

		//-------------------------------------------------------
		// Check Right (PlusX)
		//-------------------------------------------------------

		//0 spaces in this direction
		if (plusX == 0) {
			if (map[i][j+r] != 0) {
				XComparabilityCount++;
			}
		}
		else {
			//More than 0 spaces
			while (!obstacle) {

					if (map[i][m] == 0) {
						count++;
					}
					else obstacle = true;
					m++;
			}
			if (count == plusX) {
				XComparabilityCount++;
			}

		}

		//-------------------------------------------------------
		// Check Left (MinusX)
		//-------------------------------------------------------

		m = j - 1;
		obstacle = false;
		count = 0;

		if (minusX == 0) {
			if (map[i][j-r] != 0) {
				XComparabilityCount++;
			}
		}
		else {
			//More than 0 spaces
			while (!obstacle) {

					if (map[i][m] == 0) {
						count++;
					}
					else obstacle = true;
					m--;
			}
			if (count == minusX) {
				XComparabilityCount++;
			}
		}

		return XComparabilityCount;
	}
	//------------------------------------------------------------
	//COMPARE THE X COORDINATES
	//------------------------------------------------------------

	private int compareY(int i, int j) {
		int YComparabilityCount = 0;
		int count = 0;
		int r = 1;
		int m = i - 1;
		boolean obstacle = false;

		//-------------------------------------------------------
		// Check Right (PlusY)
		//-------------------------------------------------------
		//0 spaces in this direction
		if (plusY == 0) {
			if (map[i-r][j] != 0) {
				YComparabilityCount++;
			}
		}
		else {
			//More than 0 spaces
			while (!obstacle) {

					if (map[m][j] == 0) {
						count++;
					}
					else obstacle = true;
					m--;
			}
			if (count == plusY) {
				YComparabilityCount++;
			}

		}

		//-------------------------------------------------------
		// Check Left (MinusY)
		//-------------------------------------------------------
		m = i + 1;
		obstacle = false;
		count = 0;

		if (minusY == 0) {
			if (map[i+r][j] != 0) {
				YComparabilityCount++;
			}
		}
		else {
			//More than 0 spaces
			while (!obstacle) {

					if (map[m][j] == 0) {
						count++;
					}
					else obstacle = true;
					m++;
			}
			if (count == minusY) {
				YComparabilityCount++;
			}
		}

		return YComparabilityCount;
	}
}
