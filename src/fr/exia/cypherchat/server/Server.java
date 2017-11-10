package fr.exia.cypherchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {

	private int port;
	private ServerSocket socket;
	private Thread acceptThread;
	
	private List<Client> connectedClients;

	public Server(int port) {
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Invalid port");
		}
		this.port = port;
		this.connectedClients = new ArrayList<>();
	}
	
	public void start() throws IOException {
		// On ouvre le socket sur le port donn�e
		this.socket = new ServerSocket(this.port);
		// On fabrique un thread qui va boucler en permanence
		// et accepter les nouvelles connexions.
		this.acceptThread = new Thread(this);
		this.acceptThread.start();
		// Log
		System.out.println("[Server] Listening at port " + this.port);
	}

	@Override
	public void run() {
		// On boucle ind�finiement
		while (true) {
			try {
				// Cette m�thode sert à attendre la connexion d'un
				// nouveau client. Elle bloquera jusqu'à l'arrivée
				// d'une connexion. Quand un client se connectera,
				// la m�thode renverra le socket de connexion au
				// client.
				Socket s = socket.accept();
				// Arriv� ici, cela signifie qu'une connexion a été
				// re�ue sur le port du serveur.
				System.out.println("[Server] Connection received from "
						+ s.getInetAddress());
				// Cr�er un objet pour repr�senter le client
				Client c = new Client(s);
				this.connectedClients.add(c);
			}
			catch (IOException e) {
				System.out.println("[Server] Accept error");
				e.printStackTrace();
			}
		}
	}
	
}
