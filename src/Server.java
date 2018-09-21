
import java.io.*;
import java.net.*;
import java.util.HashSet;

//
//Server class
//
public class Server{

	private static final int PORT=1331;                                        //The port server is binded to
	public static HashSet<PrintWriter> writers=new HashSet<PrintWriter>();    //Stores each client's outgoing stream
	public static ServerSocket sSocket;                                      //The server socket

	//Method to shut down the server
	public static void shutDown() {
		try {
			 //Close the server socket
			sSocket.close();
		} catch (Exception e) {
			System.err.println("Problem shutting down:");
			System.err.println(e.getMessage());
			System.exit(1);
			}
	}


	//Contains all the logic for running the server.
	//Instantiates a new server socket.
	//Accepts connections from clients.
	public static void main(String[] args){
		//Create new server socket
		try{
			sSocket=new ServerSocket(PORT);
		}catch(IOException eee){
			System.err.println(eee.getMessage());
			}
		System.out.println("The server is running.");
		try{
			while(true){
				//Accept connection from the client
				new Handler(sSocket.accept()).start();
				System.out.println("Connection established");
			}
		} catch (SocketException se) {
			System.err.println(se.getMessage());
		    } catch (IOException ioe){
				System.err.println(ioe.getMessage());
				System.exit(1);
				} finally{
					//Close the server socket
					try{
						sSocket.close();
						}catch(IOException ee){
							System.err.println(ee.getMessage());
							}
				}
	}
}

//
//Session-handler class to handle one remote client in a separate thread.
//Contains methods to handle message broadcasting.
//Contains methods to respond to client's commands.
//
class Handler extends Thread{

	private String username;        //Client's user name
	private Socket socket;         //Socket for client's connection
	private BufferedReader in;    //Input stream
	private PrintWriter out;     //Output stream
	boolean done=false;         //Starts if the connection has ended


	//Method to create new handler instance
	public Handler(Socket socket){
		this.socket=socket;
	}

	//Handles broadcasting messages
	public void run(){
		try{
			//Set up I/O
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out=new PrintWriter(socket.getOutputStream(),true);
			Server.writers.add(out);
		    //Perform broadcasting of the messages
			while(done==false){
				//reading client's input
				String input=in.readLine();
				if(input==null){
					return;
				}
				//Broadcast the message
				else {
					for(PrintWriter writer:Server.writers){
						if(writer != out){
							writer.println(input);
						}
					}
				}
			}
		}catch(IOException e){
			    //When the client leaves the server abruptly
				for(PrintWriter writer:Server.writers){
					writer.println(username+" disconnected.");
				}
			} finally{
				if(username !=null){
					Server.writers.remove(out);
				}
				try{
					//Closing I/O streams and the socket
					out.close();
					in.close();
					socket.close();
				} catch (IOException ee){
					System.err.println(ee.getMessage());
					System.exit(1);
				}
			}
	}
}
