package system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

class CommunicationThread implements Runnable{
	
	private LogInThread logInThreadToWait;
	private static ConcurrentHashMap<String, Socket> onlineUsers;
	private Socket client;
	private PrintWriter clientOutput;
	private BufferedReader clientInput;
	
	public CommunicationThread(LogInThread logInThreadToWait, Socket client) {
		this.logInThreadToWait = logInThreadToWait;
		this.client = client; // maybe this connection is closed
		// because the connection is MAYBE closed, i don't initialize the streams until i'm sure it's not closed
	}
	
	public static void initializeMap(ConcurrentHashMap<String, Socket> map) {
		onlineUsers = map;
	}
	
	public void initializeStreams() {
		try {
			this.clientOutput = new PrintWriter(client.getOutputStream());
			this.clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		try {
			logInThreadToWait.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(client.isClosed()) // did not log in
			return;
		
		initializeStreams(); // the connection is for sure not closed (at the moment)
		
		connect();
		
	}
	
	private void connect() {
		
		Set<String> usersToChose = onlineUsers.keySet().stream()
				.filter(s -> !s.equals(logInThreadToWait.getUsername()))
				.collect(Collectors.toSet()); // check
		
		String choice = recursiveChoice(usersToChose);
		
		if(choice == null) {
			clientOutput.println("Wait!");
			flush();
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			connect();
		}
		else if(choice.equals("Log Off")) {
			onlineUsers.remove(logInThreadToWait.getUsername());
			clientOutput.println("END!");
			flush();
			try {
				this.client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		Socket chosenOne = onlineUsers.get(choice);
		
		connectTwoUsers(chosenOne);
		
		clientOutput.println("Log off? Y/N");
		flush();
		
		try {
			String answer = clientInput.readLine();
			if(answer.equals("Y")) {
				onlineUsers.remove(logInThreadToWait.getUsername());
				this.client.close();
				return;
			}
			else if(answer.equals("N")) {
				connect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void flush() {
		clientOutput.flush();
	}
	
	private String recursiveChoice(Set<String> usersToChose) {
		clientOutput.println("Chose a user to chat with (type their name), or chose a option");
		for(String user : usersToChose){
			clientOutput.println(user);
		}
		clientOutput.println("Exit");
		clientOutput.println("Log Off");
		flush();
		String rv = null;
		try {
			rv = clientInput.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(usersToChose.contains(rv))
			return rv;
		else if(rv.equals("Exit"))
			return null;
		else if(rv.equals("Log Off"))
			return "Log Off";
		return recursiveChoice(usersToChose);
	}
	
	private void connectTwoUsers(Socket chosenOne) {
		
		Thread send1 = null;
		Thread send2 = null;
		Thread receive1 = null;
		Thread receive2 = null;
		
		try {
			send1 = new Thread(new SendMessageThread(chosenOne.getOutputStream(), this.client.getInputStream()));
			send2 = new Thread(new SendMessageThread(this.client.getOutputStream(), chosenOne.getInputStream()));
			receive1 = new Thread(new ReceiveMessageThread(chosenOne.getInputStream(), this.client.getOutputStream()));
			receive2 = new Thread(new ReceiveMessageThread(this.client.getInputStream(), chosenOne.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		send1.start();
		send2.start();
		receive1.start();
		receive2.start();
		
		try {
			send1.join();
			send2.join();
			receive1.join();
			receive2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
