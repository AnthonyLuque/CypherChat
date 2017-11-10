package fr.exia.cypherchat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private Thread thread;
	private Server parentServer;
	private String nickname = "Anonymous";

	public Client(Server parentServer, Socket socket) throws IOException {
		this.socket = socket;
		this.parentServer = parentServer;
		
	    this.out = new PrintWriter(socket.getOutputStream(), true);
	    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public Socket getSocket() {
		return this.socket;
	}

	public void startPollingThread() {
		this.thread = new Thread(this);
		this.thread.start();
	}

	@Override
	public void run() {
		// On boucle indéfiniement (tant que l'application tourne)
		while (true) {
			
			// Lecture des données arrivant sur le socket
			String message;
			try {
				
				message = this.in.readLine(); // (Bloquant jusqu'à la reception de la prochaine ligne)
				
				if(message == null) {
					parentServer.onClientDisconnected(this);
					
					//Fermer le socket et le thread du polling
					close();

					return;
				}
				
				parentServer.onClientMessage(this, message);
				
			} catch (IOException e) {
				System.err.println("[Server][" + socket.getInetAddress() + "] Error while receiving message");
			}
			
		}
	}
	
	public boolean write(String data){
		try {
			this.out.println(data);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean close(){
		try{
			// Arrêter le thread
			thread.interrupt();
			
			// Fermer les flux
			this.in.close();
			this.out.close();
			
			// Fermer la socket
			this.socket.close();
			
			return true;
			
		}catch (Exception e) {
			return false;
		}
	}

	public String getNickname() {
		return this.nickname ;
	}

}
