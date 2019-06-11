package system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

class SendMessageThread implements Runnable{
	
	private PrintWriter pw;
	private BufferedReader bf;

	public SendMessageThread(OutputStream connection, InputStream connection1) {
		pw = new PrintWriter(connection);
		bf = new BufferedReader(new InputStreamReader(connection1));
	}

	@Override
	public void run() {
		
		while(true) {
			
			try {
				String message = bf.readLine();
				pw.println(message);
				pw.flush();
				if(message.equals("END"))
					break;
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Problem sending the message");
			}
			
		}
		
	}
}