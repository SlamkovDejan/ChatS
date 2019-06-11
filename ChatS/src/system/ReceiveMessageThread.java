package system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

class ReceiveMessageThread implements Runnable{
	
	private PrintWriter pw;
	private BufferedReader bf;

	public ReceiveMessageThread(InputStream connection, OutputStream connection1) {
		bf = new BufferedReader(new InputStreamReader(connection));
		pw = new PrintWriter(connection1);
	}

	@Override
	public void run() {
		
		while(true) {
			
			try {
				String message = bf.readLine();
				if(message == null)
					break;
				pw.println(message);
				pw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
}