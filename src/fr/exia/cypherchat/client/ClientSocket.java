package fr.exia.cypherchat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket implements Runnable {

	private Socket socketClient;
	private PrintWriter out;
	private BufferedReader in;
	
	private Thread thread;
	private Model model;
	
	
	public ClientSocket(Model model) {
		// Ouverture de la connexion au serveur
		try {
			socketClient = new Socket("localhost", 500);
			out = new PrintWriter(socketClient.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
		} catch (IOException e) {
			System.err.println("[Client][" + socketClient.getInetAddress() + "] Error while connecting to the server");
			e.printStackTrace();
		}
		
		this.startPollingThread();
		this.model = model;
	}
	
	
	
	
	// METHODES GETTERS ET SETTERS
	
	public Socket getSocket() {
		return this.socketClient;
	}

	
	
	
	// METHODES SOCKET
	
	public boolean write(String data) {
		try {
			this.out.println(data);
			this.out.flush();
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
	
	
	
	
	// METHODES DE THREADING
		
	@Override
	public void run() {
		String data = null;
		// Tant que l'application tourne
		while (true) {
			// Lire this.in pour avoir la prochaine ligne
			try {
				// Ecoute active sur le flux d'entrée
				data = this.in.readLine();
				
				// On notifie le modele
				model.onDataReceived(data);
			}
			catch (IOException e) {
				System.err.println("[Client][" + socketClient.getInetAddress() + "] Error while receiving data");
			}
				
		}
	}

	public boolean close() {
		try {
			// Arrêter le thread
			this.thread.interrupt();
				
			// Fermer les flux
			this.in.close();
			this.out.close();
				
			// Fermer le socket
			this.socketClient.close();
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
	
	public void startPollingThread() {
		this.thread = new Thread(this);
		this.thread.start();
	}

}
