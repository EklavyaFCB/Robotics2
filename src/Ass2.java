import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import java.io.*;
import java.net.*;

public class Ass2 {

	static int [][]inputMap;
	static int length;
	static int width;
	private static Brick myEV3;
	Socket socket;                             //Socket for connection with the server
	BufferedReader in;                        //BufferedReader used for incoming messages
	PrintWriter out;                         //PrintWriter used for sending messages
	BufferedReader userIn;                  //BufferedReader used for client's input
	static boolean redFound = false;
	static boolean greenFound = false;
	static boolean blueFound = false;
	public static int facing  = 0;
	static int x = 0;
	static int y = 0;

	public Ass2() {}

	public static void main(String[] args) {
		Ass2 robot = new Ass2();

		length = 6;
		width = 6;
		inputMap = new int[length+2][width+2];

		//Connection
		String host="172.20.1.133";      //Host name
		int port=1331;                  //Port number

		//Creating socket, input and output streams
			try{
				Socket socket=new Socket(host,port);
				robot.in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				robot.out=new PrintWriter(socket.getOutputStream(),true);
				robot.userIn=new  BufferedReader(new InputStreamReader(System.in));
			} catch(UnknownHostException uhe){
				System.err.println("Unhnown host: ");
				System.err.println(uhe.getMessage());
				System.exit(1);
				}catch(IOException ioe){
					System.err.println(ioe.getMessage());
				}

		//Printing out the map
		generateMap();
		MapDrawer map = new MapDrawer(inputMap);
		map.lcd.clear();
		map.initMap();

		//PilotRobot me = new PilotRobot(myEV3);
		//myEV3 = BrickFinder.getDefault();
		PositionDetector pos = new PositionDetector(inputMap);
		PilotRobot me = pos.getPilot();

		//Through the whole mission, the robot waits for a message, executes a command and sends a message back
		while(true){

			//Wait for message
			String serverMes = "";
			try{
				while((serverMes=robot.in.readLine())==null) {
					//Waiting for a message
				}
			}catch(Exception e){
				//When the server goes down abruptly
				System.out.println("The server went down. The messages can not be broadcasted.");
				System.exit(1);
			}

			//Perform an action based on the message text
			if(serverMes.equals("Perform localization")){
				serverMes = "";
				map.lcd.clear();
				//Get localisation
				pos.getPosition();
				//printMap();
				//System.out.println();
				/*;
				lcd = myEV3.getGraphicsLCD();*/
				map.lcd.clear();
				y = pos.getX();
				x = pos.getY();
				facing = pos.getFacing();

				map.updateMap(y,x);
				//Send a message with position
				robot.out.println(x+","+y+","+facing);
			}
			else if(serverMes.equals("Move up")){

				//Method to move up  should be called here

				//Method to check for patient should be called here
				if(facing == 1) {
				}else if (facing == 2){
					me.getPilot().rotate(-90);
				}else if(facing == 3){
					me.getPilot().rotate(180);
				}else{
					me.getPilot().rotate(90);
				}
				facing = 1;
				me.goTo(1);
				y = y - 1;
				map.updateMap(y,x);
				serverMes = "";
				robot.out.println("Confirmation");
				checkForPatient(me);
				communicatePatient(robot);

			}
			else if(serverMes.equals("Move down")){
				//Method to move  down should be called here

				//Method to check for patient should be called here
				if(facing == 1) {
					me.getPilot().rotate(180);
				}else if (facing == 2){
					me.getPilot().rotate(90);
				}else if(facing == 3){
				}else{
					me.getPilot().rotate(-90);
				}
				facing = 3;
				me.goTo(1);
				y = y + 1;
				map.updateMap(y,x);
				serverMes = "";
				robot.out.println("Confirmation");
				checkForPatient(me);
				communicatePatient(robot);
			}
			else if(serverMes.equals("Move right")){
				//Method to move right should be called here

				//Method to check for patient should be called here
				if(facing == 1) {
					me.getPilot().rotate(90);
				}else if (facing == 2){

				}else if(facing == 3){
					me.getPilot().rotate(-90);
				}else{
					me.getPilot().rotate(180);
				}
				facing = 2;
				me.goTo(1);
				x = x + 1;
				map.updateMap(y,x);
				serverMes = "";
				robot.out.println("Confirmation");
				checkForPatient(me);
				communicatePatient(robot);
			}
			else if(serverMes.equals("Move left")){
				//Method to move left should be called here

				//Method to check for patient should be called here
				if(facing == 1) {
					me.getPilot().rotate(-90);
				}else if (facing == 2){
					me.getPilot().rotate(180);
				}else if(facing == 3){
					me.getPilot().rotate(90);
				}else{
				}
				facing = 4;
				me.goTo(1);
				x = x - 1;
				map.updateMap(y,x);
				serverMes = "";
				robot.out.println("Confirmation");
				checkForPatient(me);
				communicatePatient(robot);
			}

		}

	}

	public static void checkForPatient(PilotRobot me){
		//Get RGB values
		float[] color = me.getColourSensor();
		float first = color[0];
		int f = Math.round((first*10));
		float second = color[1];
		int s = Math.round(second*10);
		float third = color[2];
		int t = Math.round(third*10);

		//Convert the values to colors
		if(f == 0 && s == 2 && t == 2) {
			blueFound = true;
		}
		if(f == 3 && s == 0 && t == 0) {
			redFound = true;
		}
		if(f == 1 && s == 2 && t == 1) {
			greenFound = true;
		}
	}

	//Sending a message indicating that a move has finished and whether or not a patient was found
	public static void communicatePatient(Ass2 robot) {
		boolean found = false;

		if(redFound == true) {
			robot.out.println("Moved, found: red");
			redFound = false;
			found = true;
		}
		else if(blueFound == true) {
			robot.out.println("Moved, found: blue");
			blueFound = false;
			found = true;
		}
		else if(greenFound == true) {
			robot.out.println("Moved, found: green");
			greenFound = false;
			found = true;
		}
		if(found == false){
			robot.out.println("Moved, not found");
		}
	}

	public static void generateMap() {
		for (int i = 0; i < inputMap.length;i++) {
			for (int j = 0; j < inputMap[0].length;j++) {
				if (i == 0 || i == length+1 || j == 0 || j == width+1)
					inputMap[i][j] = 1;
				else inputMap[i][j] = 0;

			}
		}

		inputMap[1][1] = 1;
		inputMap[2][4] = 1;
		inputMap[3][5] = 1;
		inputMap[5][2] = 1;
		inputMap[5][5] = 1;
		inputMap[6][2] = 1;


	}
	public static void printMap() {
		for (int i = 0; i < inputMap.length;i++) {
			for (int j = 0; j < inputMap[0].length;j++) {
				System.out.print(inputMap[i][j]);
			}
			System.out.println(" ");
		}
	}

}
