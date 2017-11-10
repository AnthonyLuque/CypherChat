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
		// On ouvre le socket sur le port donnée
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
		// On boucle indéfiniement
		while (true) {
			try {
				// Cette méthode sert à attendre la connexion d'un
				// nouveau client. Elle bloquera jusqu'à l'arrivée
				// d'une connexion. Quand un client se connectera,
				// la méthode renverra le socket de connexion au
				// client.
				Socket s = socket.accept();
				// Arrivé ici, cela signifie qu'une connexion a été
				// reçue sur le port du serveur.
				System.out.println("[Server] Connection received from "
						+ s.getInetAddress());
				// Créer un objet pour réprésenter le client
				Client c = new Client(this, s);
				// On lance le thread qui se charge de lire les
				// données qui arrivent sur le socket.
				c.startPollingThread();
				// Je sauvegarde mon client maintenant qu'il est
				// bien initialisé.
				synchronized (this.connectedClients) {
					this.connectedClients.add(c);
				}
			}
			catch (IOException e) {
				System.err.println("[Server] Client initialization error");
				e.printStackTrace();
			}
		}
	}

	public void onClientDisconnected(Client client) {
		// Log
		System.out.println("[Server][" + client.getSocket().getInetAddress()
			+ "] Client has just been disconnected");
		// Retirer le client de la liste des clients connectés
		synchronized (this.connectedClients) {
			this.connectedClients.remove(client);
		}
	}

	public void onClientRawDataReceived(Client client, String message) {
		// Log
		System.out.println("[Server][" + client.getSocket().getInetAddress()
			+ "] Received data: " + message);
		
		if (message.length() < 3) {
			System.err.println("[Server] Invalid RAW data");
			return;
		}
		
		String opcode = message.substring(0, 4);
		
		switch (opcode) {
		case "MSG;" :
			// Propager le message à tous les clients
			broadcastMessage(client, message.substring(4));
			break;
		case "NCK;" :
			// Changer le nickname du client
			client.setNickname(message.substring(4));
			// TODO A supprimer
			System.out.println("Nickname changed: " + client.getNickname());
			break;
		default :
			System.err.println("[Server] Invalid OPCODE : " + opcode);
			return;
		}
		
	}

	public void broadcastMessage(Client client, String message) {
		// Protocole
		String data = "MSG;";
		data += client.getNickname();
		data += ";";
		data += (long)(System.currentTimeMillis() / 1000);
		data += ";";
		data += client.getSocket().getInetAddress();
		data += ";";
		data += message;
		// Broadcast
		broadcast(data);
	}

	public void broadcast(String message) {
		
		// On effectue une copie de la liste
		ArrayList<Client> copy;
		synchronized (this.connectedClients) {
			 copy = new ArrayList<>(this.connectedClients);
		}
		
		// On parcours l'ensemble des clients
		for (Client client : copy) {
			// Et on leur envoie le message
			client.write(message);
		}
	}
	
}
