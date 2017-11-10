package fr.exia.cypherchat.server;

import java.net.Socket;

public class Client {

	private Socket socket;

	public Client(Socket socket) {
		this.socket = socket;
	}
	
	public Socket getSocket() {
		return this.socket;
	}

}
