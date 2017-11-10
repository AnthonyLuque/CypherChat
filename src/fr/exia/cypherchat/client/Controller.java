package fr.exia.cypherchat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Controller implements ModelListener, ViewListener {

	private Model model;
	private ClientWindow view;

	public Controller(Model model, ClientWindow view) {
		this.model = model;
		this.view = view;
		
		model.addListener(this);
		view.addListener(this);
	}

	@Override
	public void onNicknameChanged(String newNickname) {
		// TODO Auto-generated method stub
		System.out.println("Nickname changed: " + newNickname);
	}

	@Override
	public void onMessageSent(String message) {
		// TODO Auto-generated method stub
		System.out.println("On envoie le message " + message);
		
		// TODO Code de test, à  supprimer !
		try {
			// Ouverture
			Socket sock = new Socket("localhost", 500);
		    PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
		    
		    // Envoie du message
		    out.println(message);
		    
		    // Réceptionner le prochain message
		    BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		    
		    String received = in.readLine();
		    System.out.println("[Client] Message received: " + received);
		    
		    // Fermeture
		    System.out.println("[Client] Fermeture");
		    out.close();
		    sock.close();
		}
		catch (Exception e) {
			System.err.println("[Client] Impossible de se connecter");
		}
	}

	@Override
	public void onCypherMethodChanged(String cypherMethod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServerConnectionChanged(boolean status) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUserConnected(String nickname, String ip, boolean newConnection) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUserDisconnected(String nickname, String ip) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMessageReceived(String nickname, String ip, String message) {
		// TODO Auto-generated method stub
		view.messageField.setText(view.messageField.getText() + "\n" + message);
	}

}
