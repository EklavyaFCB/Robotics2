// Environment code for project RescueMission.mas2j

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.Collections;
import java.util.logging.Logger;
import java.io.*;
import java.net.*;

public class Map extends Environment {

	public static final int GRID = 6;                //Grid size
	public static final int OBSTACLE  = 16;         //Garbage code in grid model
	public static final int PATIENT  = 32;         //Patient position code in grid model
	public static final int RED = 64;             //Red patient code in the grid model
	public static final int BLUE = 128;          //Blue patient code in the grid model
	public static final int GREEN = 256;        //Green patient code in the grid model
	public static int numOfMoves = 0;
	
	//Scouts temporal starting position
	public static int scoutStartX = 0;
	public static int scoutStartY = 0;

	//Array for storing locations of obstacles
	public static int[] obstacles = new int[12];
	public int nextObstacle = 0;

	//Array for storing locations of patients
	public static int[] patients = new int[10];
	public int nextPatient = 0;

	//Array for storing traversal path
	String[] traversalPath;
	public static int nextCell = 0;
	
	public static boolean redFound = false;
	public static boolean blueFound = false;
	public static boolean greenFound = false;

	private Logger logger = Logger.getLogger("RescueMission.mas2j."+Map.class.getName());
	private MapModel model;
	private MapView  view;
	private Env environment;

  @Override
  public void init(String[] args) {
	  
	   //Establish connection
	  	environment = new Env();
		//Connection
		String host = "localhost";         //Host name
		int port = 1331;                  //Port number
		
		//Creating socket, input and output streams
			try{
				Socket socket = new Socket(host,port);
				environment.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				environment.out = new PrintWriter(socket.getOutputStream(),true);
				environment.userIn = new  BufferedReader(new InputStreamReader(System.in));				
			} catch(UnknownHostException uhe){
				System.err.println("Unhnown host: ");
				System.err.println(uhe.getMessage());
				System.exit(1);
				}catch(IOException ioe){
					System.err.println(ioe.getMessage());
				}
  }

  //Wait for a message from the robot and return this message when you receive it
  public String waitForMessage() {
	  String serverMes = "";
	  try{
		while((serverMes=environment.in.readLine())==null) {
		//Waiting for a message
		logger.info(serverMes);
		}				
	} catch(Exception e) {}
	return serverMes;
  }
  
  public void waitForConfirmation() {
	  String serverMes = "";
	  try{
		while(!(serverMes=environment.in.readLine()).equals("Confirmation")) {
		//Waiting for a message
		}				
	} catch(Exception e) {}
  }
  
  @Override
  public boolean executeAction (String agName, Structure action) {
		String act = action.toString();
		String message = "";
		try {
			//Scout's localization
			if (act.contains("localize")) {
				
				environment.out.println("Perform localization");
				message = waitForMessage();
				
				//Getting position from the robot
				String[] positionParts = message.split(",");
				int x = Integer.parseInt(positionParts[0]);
				int y = Integer.parseInt(positionParts[1]);
				int heading = Integer.parseInt(positionParts[2]);
				
				//Creating a percept for the scout
				Literal startPos = Literal.parseLiteral("position(scout,"+x+","+y+")");
				addPercept("scout",startPos);
			}
			//Saving the coords provided by the scout
			else if (act.contains("communicatePosition")) {
				act = act.replace("communicatePosition(","");
				act = act.replace(")","");
				String[] parts = act.split(",");
				scoutStartX = Integer.parseInt(parts[0]);
				scoutStartY = Integer.parseInt(parts[1]);
			}
			//Saving objects' coords provided by the doctor and adding them to the display
			else if (act.contains("communicateObject")) {
				act = act.replace("communicateObject(","");
				act = act.replace(")","");
				String[] parts = act.split(",");
				int x = Integer.parseInt(parts[0]);
				int y = Integer.parseInt(parts[1]);
				obstacles[nextObstacle] = x;
				nextObstacle = nextObstacle + 1;
				obstacles[nextObstacle] = y;
				nextObstacle = nextObstacle + 1;
				model.add(OBSTACLE, x, y);
			}
			//Saving patient's coords provided by the doctor and adding them to the display
			else if (act.contains("communicatePatient")) {
				act = act.replace("communicatePatient(","");
				act = act.replace(")","");
				String[] parts = act.split(",");
				int x = Integer.parseInt(parts[0]);
				int y = Integer.parseInt(parts[1]);
				patients[nextPatient] = x;
				nextPatient = nextPatient + 1;
				patients[nextPatient] = y;
				nextPatient = nextPatient + 1;
				model.add(PATIENT, x, y);
			}
			//Setting up the model
			else if (act.contains("setUpEnvironment")) {
				model = new MapModel();
				view  = new MapView(model);
				model.setView(view);
				model.setAgPos(1, scoutStartX, scoutStartY);
			}
			//Seraching for traverdal path
			else if (act.contains("findRoute")) {
				Path path = new Path (GRID, GRID, obstacles, 4, patients, scoutStartX, scoutStartY);
				path.setUpMatrix();
				path.floydWarshall();
				path.shortestPath();
				traversalPath = path.traversalPath;
			}
			//Get next move from the path
			else if (act.contains("findMove")) {
				String cell = traversalPath[nextCell];
				nextCell = nextCell + 1;
				Literal nextMove = Literal.parseLiteral("next("+cell+")");
				addPercept("doctor",nextMove);
			}
			//Moving the scout
			else if (act.contains("makeMove")) {
				act = act.replace("makeMove(","");
				act = act.replace(")","");
				
				//Changing the scout's position on the model
				if(act.equals("down")){
					//Send a message to the robot and wait for a reply
					environment.out.println("Move down");
					waitForConfirmation();
					scoutStartY = scoutStartY + 1;
					view.repaint();
				}
				else if(act.equals("up")){
					//Send a message to the robot and wait for a reply
					environment.out.println("Move up");
					waitForConfirmation();
					scoutStartY = scoutStartY - 1;
					view.repaint();
				}
				else if(act.equals("right")){
					//Send a message to the robot and wait for a reply
					environment.out.println("Move right");
					waitForConfirmation();
					scoutStartX = scoutStartX + 1;
					view.repaint();
				}
				else if(act.equals("left")){
					//Send a message to the robot and wait for a reply
					environment.out.println("Move left");
					waitForConfirmation();
					scoutStartX = scoutStartX - 1;
					view.repaint();
				}
				model.setAgPos(1,scoutStartX, scoutStartY);
				numOfMoves = numOfMoves + 1;
				logger.info("Number of moves made: "+numOfMoves);
				message = waitForMessage();

				//Checking for patients
				//If the red patient is found
				if(message.equals("Moved, found: red")){
					//Adding a percept
					Literal red = Literal.parseLiteral("red("+scoutStartX+","+scoutStartY+")");
					addPercept("scout",red);
					//Updating the model
					redFound = true;
					model.remove(PATIENT, scoutStartX, scoutStartY);
					model.add(RED, scoutStartX, scoutStartY);
					view.allFound();
				}
				//If the blue patient is found
				else if(message.equals("Moved, found: blue")){
					//Adding a percept
					Literal blue = Literal.parseLiteral("blue("+scoutStartX+","+scoutStartY+")");
					addPercept("scout",blue);
					//Updating the model
					blueFound = true;
					model.remove(PATIENT, scoutStartX, scoutStartY);
					model.add(BLUE, scoutStartX, scoutStartY);
					view.allFound();
				}
				//If the green patient is found
				else if(message.equals("Moved, found: green")){
					//Adding the percept
					Literal green = Literal.parseLiteral("green("+scoutStartX+","+scoutStartY+")");
					addPercept("scout",green);
					//Updating the model
					greenFound = true;
					model.remove(PATIENT, scoutStartX, scoutStartY);
					model.add(GREEN, scoutStartX, scoutStartY);
					view.allFound();
				}else {
					//Updating a model if a patient has not been found
					if (model.hasObject(PATIENT, scoutStartX, scoutStartY)) {
						model.remove(PATIENT, scoutStartX, scoutStartY);
					}
				}

				view.repaint();
				Literal nextMove = Literal.parseLiteral("next("+act+")");
				removePercept("doctor",nextMove);
			}
			else {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(200);
   		} catch (Exception e) {}

		informAgsEnvironmentChanged();
		return true;
  }

  @Override
  public void stop() {
      super.stop();
  }
}

class MapModel extends GridWorldModel {

  public  MapModel() {

    super(Map.GRID, Map.GRID, 2);

    //Initial location of agent
    try {
		setAgPos(1, 0, 0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

class MapView extends GridWorldView {

  public MapView (MapModel model) {
    super(model, "Map", 600);
    defaultFont = new Font("Arial", Font.BOLD, 18);
    setVisible(true);
    repaint();
  }

  //Draw objects
  @Override
  public void draw(Graphics g, int x, int y, int object) {
    switch (object) {
		case Map.OBSTACLE: drawObst (g, x, y);  break;
		case Map.PATIENT: drawPatient (g, x, y); break;
		case Map.RED: drawRed (g,x,y); break;
		case Map.GREEN: drawGreen (g,x,y); break;
		case Map.BLUE: drawBlue (g,x,y); break;
    }
  }

  //Draw agent method
  @Override
  public void drawAgent(Graphics g, int x, int y, Color c, int id) {
    String label = "Scout";
    c = Color.blue;
    super.drawAgent(g, x, y, c, -1);
    g.setColor(Color.white);
    super.drawString(g, x, y, defaultFont, label);
  }

  //Draw obstacle method
  public void drawObst(Graphics g, int x, int y) {
    super.drawObstacle(g, x, y);
    g.setColor(Color.white);
    drawString(g, x, y, defaultFont, "O");
  }

  //Draw patient method
  public void drawPatient(Graphics g, int x, int y) {
    super.drawObstacle(g, x, y);
    g.setColor(Color.white);
    drawString(g, x, y, defaultFont, "P");
  }

  public void allFound() {
	  if (Map.redFound == true && Map.blueFound == true && Map.greenFound == true) {
		  for (int i = 0; i < Map.GRID; i++) {
			  for (int j = 0; j < Map.GRID; j++) {
				  if (model.hasObject(Map.PATIENT, i, j)) {
					  model.remove(Map.PATIENT, i, j);
				  }
			  }
		  }
	  }
  }
  
 //Draw red/blue/green patient methods
 public void drawRed (Graphics g, int x, int y) {
    super.drawObstacle(g, x, y);
    g.setColor(Color.red);
    drawString(g, x, y, defaultFont, "P");
  }
   public void drawGreen (Graphics g, int x, int y) {
    super.drawObstacle(g, x, y);
    g.setColor(Color.green);
    drawString(g, x, y, defaultFont, "P");
  }
   public void drawBlue (Graphics g, int x, int y) {
    super.drawObstacle(g, x, y);
    g.setColor(Color.blue);
    drawString(g, x, y, defaultFont, "P");
  }
}

//Used for connection with the server
class Env{
	Socket socket;                   //Socket for connection with the server
	BufferedReader in;              //BufferedReader used for incoming messages
	PrintWriter out;               //PrintWriter used for sending messages
	BufferedReader userIn;        //BufferedReader used for client's input
	
	public Env() {}
}

