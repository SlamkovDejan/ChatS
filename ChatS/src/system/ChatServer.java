package system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

class ChatServer {
	
	private ServerSocket server;
	private static ConcurrentHashMap<String, String> allUsers;
	private static ConcurrentHashMap<String, Socket> onlineUsers;
	
	@SuppressWarnings("unchecked")
	public ChatServer(int port) {
		
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Problem starting the server!");
			e.printStackTrace();
		}
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("baza.ser"));
			allUsers = (ConcurrentHashMap<String, String>) ois.readObject();
			ois.close();
		}catch(FileNotFoundException fnf) {
			System.err.println("New map was made!");
			allUsers = new ConcurrentHashMap<String, String>();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		onlineUsers = new ConcurrentHashMap<String, Socket>();
		
		LogInThread.initializeMaps(allUsers, onlineUsers);
		CommunicationThread.initializeMap(onlineUsers);
	}
	
	public void startChatting() {
		
		try {
			
			while(true) {
				
				Socket connection = server.accept();
				
				LogInThread logIn = new LogInThread(connection);
				logIn.start();
				
				Thread communication = new Thread(new CommunicationThread(logIn, connection));
				communication.start();
				
			}
			
		}catch(Exception ex) {
			if(server != null && !server.isClosed())
				close();
			System.err.println("Closed the server");
		}
	}
	
	public void close() {
		
		try {
			File baza = new File("baza.ser");
			baza.delete();
			baza.createNewFile();
			
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(baza));
			oos.writeObject(allUsers);
			oos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			this.server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static InetAddress addressOfServer;
	public static int port = 9999;
	
	public static void main(String[] args) {
		
		try {
			addressOfServer = InetAddress.getByName("192.168.0.115");
		} catch (UnknownHostException e) {
			System.err.println("Can't find address!");
			e.printStackTrace();
		}
		
		ChatServer cs = new ChatServer(port);
		
		cs.startChatting();
		
	}
	
}
