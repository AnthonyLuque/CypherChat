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
		// On ouvre le socket sur le port donnÃ©e
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
				// Cette méthode sert à attendre la connexion d'un nouveau client.
				// (Elle bloquera jusqu'à  l'arrivée d'une connexion. Quand un client se connectera, la méthode renverra le socket de connexion au client.)
				Socket s = socket.accept();
				// Arrivé ici, cela signifie qu'une connexion a été reçue sur le port du serveur.
				System.out.println("[Server] Connection received from " + s.getInetAddress());
				// Créer un objet pour représenter le client
				Client c = new Client(this, s);
				// On lance le thread qui se charge de lire les données qui arrivent sur le socket.
				c.startPollingThread();
				// Je sauvegarde mon client maintenant qu'il est bien initialisé
				synchronized (this.connectedClients) {
					this.connectedClients.add(c);
				}
				// On envoie la liste des clients connectés à ce nouveau client
				this.sendConnectedClients(c);
				// Log
				System.out.println("[Server][" + c.getSocket().getInetAddress() + "] Client connected");
			}
			catch (IOException e) {
				System.err.println("[Server] Client initialization error");
				e.printStackTrace();
			}
		}
	}

	public void onClientDisconnected(Client client) {
		// Log
		System.out.println("[Server][" + client.getSocket().getInetAddress() + "] Client has just been disconnected");
		
		// Retirer le client de la liste des clients connectés
		synchronized (this.connectedClients) {
			this.connectedClients.remove(client);
		}
		
		// On prévient tous les clients connectés
		broadcastClientDisconnected(client);
	}

	private void broadcastClientDisconnected(Client client) {
		// Protocole : DNT;Nickname;IP
		String data = "MSG;";
		data += client.getNickname();
		data += ";";
		data += client.getSocket().getInetAddress();
		// Broadcast
		broadcast(data);
	}

	public void onClientRawDataReceived(Client client, String message) {
		// Log
		System.out.println("[Server][" + client.getSocket().getInetAddress() + "] Received data: " + message);
		
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
		// Protocole : MSG;Nickname;Time;IP;Message
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

	public void broadcast(String data) {
		
		// On effectue une copie de la liste
		ArrayList<Client> copy;
		synchronized (this.connectedClients) {
			 copy = new ArrayList<>(this.connectedClients);
		}
		
		// On parcours l'ensemble des clients
		for (Client client : copy) {
			// Et on leur envoie le message
			client.write(data);
		}
	}
	
	public void sendConnectedClients(Client c){
		// On effectue une copie de la liste
		ArrayList<Client> copy;
		synchronized (this.connectedClients) {
			 copy = new ArrayList<>(this.connectedClients);
		}
		
		// On parcours l'ensemble des clients
		for (Client client : copy) {
			// Protocole : CNT;Nickname;IP;NewConnection
			String data = "CNT;";
			data += client.getNickname();
			data += ";";
			data += client.getSocket().getInetAddress();
			data += ";";
			data += "false"; /* Client déjà connecté */
			// On envoie le client connecté
			c.write(data);
		}
	}
	
/*	
	public String connectedClientsListToString() {
		
		// On effectue une copie de la liste
		ArrayList<Client> copy;
		synchronized (this.connectedClients) {
			 copy = new ArrayList<>(this.connectedClients);
		}
		
		// On créer la trame
		String data = "CNT";
		for (Client client : copy) {
			data += ";";
			data += client.getNickname();
		}
		
		return data;
	}
*/
	
}
