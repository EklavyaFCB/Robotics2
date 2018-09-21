/*
 *  COMP329 Assignment 2
 *  PilotRobot class, containing the move pilot
 */
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.SampleProvider;
//import lejos.hardware.sensor.HiTechnicCompass;
import lejos.robotics.navigation.Pose;
import lejos.robotics.localization.OdometryPoseProvider;
import java.util.Arrays;

public class PilotRobot {

	// Variables
	private Brick myEV3;
	private MovePilot pilot;
	private SampleProvider leftBumperSampleProvider, rightBumperSampleProvider, ultrasonicSensorSampleProvider, colourSensorSampleProvider;
	private float[] leftBumperSample, rightBumperSample, ultrasonicSensorSample, colourSensorSample;
	private EV3UltrasonicSensor ultrasonicSensor;
	private EV3TouchSensor leftBumper, rightBumper;
	private EV3ColorSensor colourSensor;
	private EV3MediumRegulatedMotor ultrasonicSensorMotor;
	private OdometryPoseProvider opp;
	private double offsetVal;
	private double wheelSize;
	private double linSpeed;
	private double rotSpeed;

	// Grid constants
	//final public double WALL_LENGTH = 1.93;					// Entire wall length
	final public double WALL_LENGTH = 1.53;						// Entire wall length in 6x6 square
	final public double WALL_WIDTH = 1.53;						// Entire wall width
	final public int GRID_LNUMBER = 6;							// Grid length number
	final public int GRID_WNUMBER = 6;							// Grid width number
	final public double GRID_LENGTH = WALL_LENGTH/GRID_LNUMBER;	// Individual grid length
	final public double GRID_WIDTH = WALL_WIDTH/GRID_WNUMBER;	// Individual grid width
	final public double SENSOR_WIDTH =  0.04;
	final public double SENSOR_LENGTH = 0.08;
	private float correctAngle_distance = 10;					// Corrected left angle distance
	final public double CORRECT_DISTANCE_ANGLE = 38;

	// Constructor
	public PilotRobot(Brick robot) {
		// Initiliase
		this.myEV3 = robot;

		// Declarations
		offsetVal = 5.5;
		wheelSize = 4.32;
		linSpeed = 7.5;
		rotSpeed = 15.0;

		// Get sensors
		this.leftBumper = new EV3TouchSensor(myEV3.getPort("S1"));
		this.rightBumper = new EV3TouchSensor(myEV3.getPort("S4"));
		this.ultrasonicSensor = new EV3UltrasonicSensor(myEV3.getPort("S3"));
		this.colourSensor = new EV3ColorSensor(myEV3.getPort("S2"));


		// Initialise sensor sample providers
		leftBumperSampleProvider = leftBumper.getTouchMode();
		rightBumperSampleProvider = rightBumper.getTouchMode();
		colourSensorSampleProvider = colourSensor.getRGBMode();
		ultrasonicSensorSampleProvider = ultrasonicSensor.getDistanceMode();

		// Initialise sensor samples
		leftBumperSample = new float[leftBumperSampleProvider.sampleSize()];
		rightBumperSample = new float[rightBumperSampleProvider.sampleSize()];
		colourSensorSample = new float[colourSensorSampleProvider.sampleSize()];
		ultrasonicSensorSample = new float[ultrasonicSensorSampleProvider.sampleSize()];

		// Initialise wheels and create chassis
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(myEV3.getPort("B"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(myEV3.getPort("D"));
		ultrasonicSensorMotor = new EV3MediumRegulatedMotor(myEV3.getPort("C"));

		Wheel leftWheel = WheeledChassis.modelWheel(leftMotor, wheelSize).offset(-offsetVal);
		Wheel rightWheel = WheeledChassis.modelWheel(rightMotor, wheelSize).offset(offsetVal);

		Chassis chassis = new WheeledChassis( new Wheel[]{leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);

		// Initialise move pilot with the chassis
		this.pilot = new MovePilot(chassis);

		// Set reasonable speeds to avoid unintended diagonal mouvement
		pilot.setAngularSpeed(rotSpeed);
		pilot.setLinearSpeed(linSpeed);

		// Initiliase odometry with the pilot
		opp = new OdometryPoseProvider(pilot);
		opp.setPose(new Pose((float)GRID_LENGTH/2,(float)GRID_WIDTH/2,0)); 	// Centered
	}

	// Is the left bumper pressed?
	public boolean isLeftBumperPressed() {
    	leftBumperSampleProvider.fetchSample(leftBumperSample, 0);
    	return (leftBumperSample[0] == 1.0);
	}

	// Is the right bumper pressed?
	public boolean isRightBumperPressed() {
    	rightBumperSampleProvider.fetchSample(rightBumperSample, 0);
    	return (rightBumperSample[0] == 1.0);
	}

	// Get the colour from the colour sensor
	public float[] getColourSensor() {
    	colourSensorSampleProvider.fetchSample(colourSensorSample, 0);
    	return colourSensorSample;
	}

	// Get the move pilot
	public MovePilot getPilot() {
		return pilot;
	}

	// Set the robot pose
	public void setPose(Pose p) {
		opp.setPose(p);
	}

	// Get the robot Pose
	public Pose getPose() {
		return opp.getPose();
	}

	// Get the ultrasonic sensor motor
	public EV3MediumRegulatedMotor getUltrasonicMotor() {
		return ultrasonicSensorMotor;
	}

	// Get ultrasonic reading
	public float getUltrasonicSensor1() {
		ultrasonicSensorSampleProvider.fetchSample(ultrasonicSensorSample, 0);
		return ultrasonicSensorSample[0];
	}

	// Get ultrasonic reading to the right
	public float getUltrasonicSensorRight1() {
		// Rotate sensor by 90
		ultrasonicSensorMotor.rotate(-90);

		ultrasonicSensorSampleProvider.fetchSample(ultrasonicSensorSample, 0);

		float reading = ultrasonicSensorSample[0];
		// Rotate back to 0
		ultrasonicSensorMotor.rotate(90);

		return reading;
	}

	// Get ultrasonic reading to the left
	public float getUltrasonicSensorLeft1() {
		// Rotate sensor by -90
		ultrasonicSensorMotor.rotate(90);

		ultrasonicSensorSampleProvider.fetchSample(ultrasonicSensorSample, 0);

		float reading = ultrasonicSensorSample[0];
		// Rotate back to 0
		ultrasonicSensorMotor.rotate(-90);

		return reading;
	}

	// Rotate ultrasonic sensor
	public void rotateUltrasonicSensor1(int rotate) {
		ultrasonicSensorMotor.rotate(rotate);

		while(ultrasonicSensorMotor.isMoving()) {
			// Wait for sensor to finish moving
		}
	}

	// Rotate ultrasonic sensor
	public void rotateUltrasonicSensor(int rotate) {
		ultrasonicSensorMotor.rotate(rotate);

		while(ultrasonicSensorMotor.isMoving()) {
			// Wait for sensor to finish moving
		}
	}

	// Get ultrasound reading to the right - this type gets 5 values and stores them in an array
	public float getUltrasonicSensorRight() {
		// Rotate sensor by 90
 		rotateUltrasonicSensor(-90);

		int noOfSamples = 5;	// We use 5 samples
 		float[] samplesArr = new float[noOfSamples]; // Initialise

 		// Put sample in array everytime
 		for (int i=0; i<noOfSamples; i++) {
 			// Sleep to get better sample accuracy
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}

 			ultrasonicSensorSampleProvider.fetchSample(ultrasonicSensorSample, 0);
 			samplesArr[i] = ultrasonicSensorSample[0];

 		}

		// Rotate sensor back to 0
 		rotateUltrasonicSensor(90);

		// Return mean value
 		return getMean(samplesArr);
	}

	// Get ultrasound reading to the left - this type gets 5 values and stores them in an array
	public float getUltrasonicSensorLeft() {

		// Rotate sensor by 90
 		rotateUltrasonicSensor(90);

		int noOfSamples = 5;	// We use 5 samples
 		float[] samplesArr = new float[noOfSamples]; // Initialise

 		// Put sample in array everytime
 		for (int i=0; i<noOfSamples; i++) {
 			// Sleep to get better sample accuracy
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}

 			ultrasonicSensorSampleProvider.fetchSample(ultrasonicSensorSample, 0);
 			samplesArr[i] = ultrasonicSensorSample[0];

 		}

		// Rotate sensor back to 0
 		rotateUltrasonicSensor(-90);

		// Return mean value
 		return getMean(samplesArr);
	}

	// Get samples mean
	public float getMean(float[] arr) {

		// Sort the array
 		Arrays.sort(arr);

		float avg;
		float tot = 0;

		// We start at 1 to not get the highest value
		// We end at arr.length-1 to not get the lowest value
		for (int i=1; i<arr.length-1;i++) {
			tot = tot + arr[i];
		}

		// We average by dividing by total number of values in the array minus 2
		avg = tot/(arr.length-2);
		return avg;
	}

	// Revamped method to go to adjacent grid
	public void goTo(int direction) {

		// Initiliase
		float aDist = 0;
		float bDist = 0;

		double tDist = (GRID_LENGTH)*100;

		switch(direction) {
			// FORWARD
			case 1: aDist = getUltrasonicSensorLeft();
					this.pilot.travel(tDist/3);
					bDist = getUltrasonicSensorLeft();
					straighten(aDist, bDist, tDist/3);
					break;
			// RIGHT
			case 2: this.pilot.rotate(90);
					aDist = getUltrasonicSensorLeft();
					this.pilot.travel(tDist/3);
					bDist = getUltrasonicSensorLeft();
					straighten(aDist, bDist, tDist/3);
					break;
			// BACK
			case 3: this.pilot.rotate(180);
					aDist = getUltrasonicSensorLeft();
					this.pilot.travel(tDist/3);
					bDist = getUltrasonicSensorLeft();
					straighten(aDist, bDist, tDist/3);
					break;
			// LEFT
			case 4: this.pilot.rotate(-90);
					aDist = getUltrasonicSensorLeft();
					this.pilot.travel(tDist/3);
					bDist = getUltrasonicSensorLeft();
					straighten(aDist, bDist, tDist/3);
					break;
		}

	}

	// Correct angle in case of deviation from a straight path
	public void straighten(float aDist, float bDist, double dist) {
		// Important to remember here the var dist is tDist/2, not tDist

		// Change to centimeters
		aDist = aDist*100;
		bDist = bDist*100;

		// Use trignometry
		double param = (aDist-bDist)/(dist);
		double angleRad = Math.asin(param);
		double angleDeg = Math.toDegrees(angleRad);

		// To not get NaN error, only correct when within ]1;-1[, as sin^-1(x) returns -PI/2 to PI/2, when x = ]1;-1[
		if ( (param<1) || (param>-1) ) {

			// If robot next to wall or obstacle (+margin of half a grid)
			if ((aDist < 1.5*GRID_LENGTH*100) && (bDist < 1.5*GRID_LENGTH*100)) {

				// If difference between first and second dist is not bigger than a grid length
				// and the correction angle is not null
				if (!(Math.abs(aDist-bDist) > (GRID_LENGTH*100)) && (angleDeg!=0.0)) {
					// Do correction

					// Go back
					this.pilot.travel(-dist);

					// Correct deviateded angle by rotating to a straight path
					this.pilot.rotate(angleDeg);

					// Go the same distance forward again
					this.pilot.travel(3*dist);
				} else {
					this.pilot.travel(2*dist);
				}
			} else {
				this.pilot.travel(2*dist);
			}
		} else {
			this.pilot.travel(2*dist);
		}

	}

	// Get colour sensor reading
	public int getColour() {
		int colour = colourSensor.getColorID();
    	return colour;
	}

	// End the sensors
	public void endSensors() {
		leftBumper.close();
		rightBumper.close();
		colourSensor.close();
	}

}
