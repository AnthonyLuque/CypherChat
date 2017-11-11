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
	private ClientSocket clientSocket;
	

	public Controller(Model model, ClientWindow view) {
		this.model = model;
		this.view = view;
		
		model.addListener(this);
		view.addListener(this);
		
		clientSocket = new ClientSocket(model);
	}

	
	
	
	// METHODES DE L'OBSERVEUR DE LA VUE
	
	
	@Override
	public void onNicknameChanged(String newNickname) {
		// On change le pseudo dans le modèle
		model.setNickname(newNickname);
		
		// On envoie au server notre nouveau pseudo
		this.clientSocket.write("NCK;" + newNickname);
	}

	@Override
	public void onMessageSent(String message) {
		// On envoie au server notre message
		this.clientSocket.write("MSG;" + message);

/*
		// TODO Code de test, à  supprimer !
//		try {
//			 String rcvd = in.readLine();
//			 System.out.println("[Client] Message received: " + rcvd);
			// Fermeture
			Thread.sleep(3000);
			out.close();
			socketClient.close();
			System.out.println("[Client] Fermeture");
		}
		catch (Exception e) {
			System.err.println("[Client] Impossible de se connecter");
		}
*/
	}
	
	@Override
	public void onCypherMethodChanged(String cypherMethod) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	// METHODES DE L'OBSERVEUR DU MODELE

	
	@Override
	public void onServerConnectionChanged(boolean status) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUserConnected(String nickname, String ip/*, boolean newConnection*/) {
		this.view.addConnectedUser(nickname);
		
		/*
		if(newConnection){
			
		}
		*/
	}

	@Override
	public void onUserDisconnected(String nickname, String ip) {
		this.view.removeConnectedUser(nickname);
	}

	@Override
	public void onMessageReceived(String nickname, String ip, String message) {
		view.setMessageArea(nickname, message);
	}
	
}