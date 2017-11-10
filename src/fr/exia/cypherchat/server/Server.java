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
		// On v�rifie que le port est valide selon la l�gislation
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Invalid port");
		}
		this.port = port;
		this.connectedClients = new ArrayList<>();
	}

	public void start() throws IOException {
		// On ouvre le socket sur le port donn�e
		this.socket = new ServerSocket(this.port);

		// On fabrique un thread qui va boucler en permanence et accepter les
		// nouvelles connexions
		this.acceptThread = new Thread(this);
		this.acceptThread.start();

		// Log
		System.out.println("[Server] Listening at port " + this.port);
	}
	
	
	public void onClientMessage(Client client, String message) {
		// Log
		System.out.println("[Server][" + client.getSocket().getInetAddress() + "] Received message: " + message);	
		
		// Propager le message � tous les clients
		broadcastMessage(client, message);
	}
	
	
	public void broadcastMessage(Client client, String message) {
		// On cr�� la trame
		String data = "MSG;";
		data += client.getNickname();
		data += ";";
		data += (long)(System.currentTimeMillis() / 1000);
		data += ";";
		data += client.getSocket().getInetAddress();
		data += ";";
		data += message;
		
		//On l'envoie � tout le monde
		broadcast(data);
	}

	public void broadcast(String message){
		ArrayList<Client> copyConnectedClients;
		synchronized (this.connectedClients) {
			copyConnectedClients = new ArrayList<>(this.connectedClients);
		}
		
		// On envoie le message � l'ensemble des clients
		for(Client client : copyConnectedClients){
			client.write(message);
		}
	}

	public void onClientDisconnected(Client client) {
		// Log
		System.out.println("[Server][" + client.getSocket().getInetAddress() + "] Client was disconnected");
		
		// Retirer le client de la liste
		synchronized (this.connectedClients) {
			this.connectedClients.remove(client);
		}
	}
	

	@Override
	public void run() {
		// On boucle ind�finiement (tant que l'application tourne)
		while (true) {
			try {
				// Cette m�thode sert � attendre la connexion d'un nouveau
				// client.
				// (Elle bloquera jusqu'� l'arriv�e d'une connexion. Quand un
				// client se connectera, la m�thode renverra le socket de
				// connexion au client.)
				Socket socketClient = socket.accept();

				// Arriv� ici, cela signifie qu'une connexion a �t� re�ue sur le
				// port du serveur
				System.out.println("[Server] Connection received from " + socketClient.getInetAddress());

				// Cr�er un objet pour repr�senter le client
				Client client = new Client(this, socketClient);

				// On lance le thread qui va lire les donn�es arrivant sur le
				// socket du client
				client.startPollingThread();
				
				// On sauvegarde le client
				synchronized (this.connectedClients) {
					this.connectedClients.add(client);
				}

				
			} catch (IOException e) {
				System.err.println("[Server] Client initialization error");
				e.printStackTrace();
			}
		}
	}

}
