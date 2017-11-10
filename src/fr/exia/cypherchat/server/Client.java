package fr.exia.cypherchat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

	private Server parent;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private Thread thread;
	private String nickname = "anonymous";

	public Client(Server parent, Socket socket) throws IOException {
		// Je mémorise le socket et le seveur parent
		this.parent = parent;
		this.socket = socket;
		// J'initialise le flux de sortie (write)
		this.out =
		        new PrintWriter(socket.getOutputStream(), true);
		// J'initialise le flux d'entrée (read)
		this.in =
		        new BufferedReader(
		            new InputStreamReader(socket.getInputStream()));
	}
	
	public Socket getSocket() {
		return this.socket;
	}

	public Server getParentServer() {
		return this.parent;
	}
	
	public void startPollingThread() {
		this.thread = new Thread(this);
		this.thread.start();
	}

	@Override
	public void run() {
		String message;
		// Tant que l'application tourne
		while (true) {
			// Lire this.in pour avoir la prochaine ligne
			try {
				// Ecoute active sur le flux d'entrée
				message = this.in.readLine();
				// Le client vient de se déconnecter !
				if (message == null) {
					// On prévient la classe Server
					parent.onClientDisconnected(this);
					// Fermer le socket et le thread de polling
					close();
					// Et on arrête le thread
					return;
				}
				
				// On prévient la classe Server
				parent.onClientRawDataReceived(this, message);
			}
			catch (IOException e) {
				System.err.println("[Server][" + socket.getInetAddress()
					+ "] Error while receiving message");
			}
		}
	}
	
	public boolean write(String data) {
		try {
			this.out.println(data);
			return true;
		}
		catch (Exception ex) {
			return false;
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
			this.socket.close();
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

	public String getNickname() {
		return this.nickname ;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

}
