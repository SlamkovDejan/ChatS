package system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

class LogInThread extends Thread{
	
	private BufferedReader clientInputStream;
	private PrintWriter clientOutputStream;
	private Socket clientSocket;
	private String username, password;
	private static ConcurrentHashMap<String, String> allUsers;
	private static ConcurrentHashMap<String, Socket> onlineUsers;
	
	public LogInThread(Socket socketOfClient) {
		
		try {
			this.clientInputStream = new BufferedReader(new InputStreamReader(socketOfClient.getInputStream()));
			this.clientOutputStream = new PrintWriter(socketOfClient.getOutputStream());
		} catch (IOException e) {
			System.err.println("Problem with getting the streams of the client!");
			e.printStackTrace();
		}
		
		this.clientSocket = socketOfClient;
	}
	
	public static void initializeMaps(ConcurrentHashMap<String, String> users, ConcurrentHashMap<String, Socket> online) {
		allUsers = users;
		onlineUsers = online;
	}
	
	public String getUsername() {
		return username;
	}
	
	@Override
	public void run() {
		
		while(true) {
			clientOutputStream.println("1. Log in 2. Sign up 3. Close");
			flush();
			int choice = 0;
			
			try {
				choice = Integer.parseInt(clientInputStream.readLine());
			} catch (NumberFormatException | IOException e) {
				System.err.println("Can't read from client!");
				e.printStackTrace();
			}
			
			if(choice == 1) {
				if(!logInUser()) {
					clientOutputStream.println("Again!");
					flush();
					continue;
				}
				clientOutputStream.println("Success!");
				flush();
				onlineUsers.put(username, clientSocket);
				break;
			}
			else if(choice == 2) {
				if(!signUpUser()) {
					clientOutputStream.println("Again!");
					flush();
					continue;
				}
				clientOutputStream.println("Success!");
				flush();
				onlineUsers.put(username, clientSocket);
				break;
			}
			else if(choice == 3) {
				try {
					clientOutputStream.println("Done!");
					flush();
					this.clientSocket.close(); // check in thread caller
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	private void flush() {
		clientOutputStream.flush();
	}
	
	private void getData() {
		clientOutputStream.println("username: ");
		flush();
		
		try {
			username = clientInputStream.readLine();
			clientOutputStream.println("password: ");
			flush();
			
			password = clientInputStream.readLine();
		} catch (IOException e) {
			System.err.println("Can't read from client!");
			e.printStackTrace();
		}
	}
	
	private boolean signUpUser() {
		getData();
		
		if(allUsers.containsKey(username))
			return false;
		
		allUsers.put(username, password);
		return true;
	}

	private boolean logInUser() {
		getData();
		
		if(!allUsers.containsKey(username))
			return false;
		String passwordInSystem = allUsers.get(username);
		
		if(!passwordInSystem.equals(password))
			return false;
		
		return true;
	}
}
