package system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class ChatClient {
	
	private Socket connection;
	private BufferedReader inputStreamFromServer;
	private BufferedReader stdIn;
	private PrintWriter outputStream;

	public ChatClient(InetAddress addressOfServer, int portOfServer) {
		
		try {
			this.connection = new Socket(addressOfServer, portOfServer);
			this.inputStreamFromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			this.outputStream = new PrintWriter(connection.getOutputStream());
			this.stdIn = new BufferedReader(new InputStreamReader(System.in));
		} catch (IOException e) {
			System.err.println("Problem with the connection to the server");
			e.printStackTrace();
		}
		
	}
	
	public void logIn() {
		
		try {
			
			while(true) {
				System.out.println(inputStreamFromServer.readLine());
				
				outputStream.println(stdIn.readLine()); 
				outputStream.flush();
				
				String messageForUsername = inputStreamFromServer.readLine();
				if(messageForUsername.equals("Done!"))
					break;
				
				System.out.println(messageForUsername);
				outputStream.println(stdIn.readLine()); 
				outputStream.flush();
				
				messageForUsername = inputStreamFromServer.readLine();
				System.out.println(messageForUsername);
				outputStream.println(stdIn.readLine()); 
				outputStream.flush();
				
				messageForUsername = inputStreamFromServer.readLine();
				if(messageForUsername.equals("Success!"))
					break;
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(connection.isClosed())
			return;
		
		communicate();
		
	}
	
	private void communicate() {
		
		try {
			while(true) {
				System.out.println(inputStreamFromServer.readLine());
				String user = null;
				while(!(user = inputStreamFromServer.readLine()).equals("Log Off")) {
					System.out.println(user);
				}
				System.out.println(user);
				
				outputStream.println(stdIn.readLine());
				outputStream.flush();
				
				String message = inputStreamFromServer.readLine();
				if(message.equals("Wait!"))
					continue;
				else if(message.equals("END!")) {
					this.connection.close();
					break;
				}
				else
					break;
				
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		
		try {
			ChatClient begin = new ChatClient(InetAddress.getByName("192.168.0.115"), 9999);
			begin.logIn();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}
	
}
