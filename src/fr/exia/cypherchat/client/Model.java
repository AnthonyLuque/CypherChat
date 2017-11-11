package fr.exia.cypherchat.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Model {

	private String nickname = null;
	
	private String cypherMethod = null;
	
	private String serverAddress = null;
	
	private List<String> connectedUsers;
	
	private List<ModelListener> listeners;
	
	public Model() {
		this.connectedUsers = new ArrayList<>();
		this.listeners = new ArrayList<>();
	}
	
	
	
	
	// METHODES GETTERS ET SETTERS
	
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getCypherMethod() {
		return cypherMethod;
	}

	public void setCypherMethod(String cypherMethod) {
		this.cypherMethod = cypherMethod;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public List<String> getConnectedUsers() {
		return connectedUsers;
	}

	public void addConnectedUser(String username) {
		this.connectedUsers.add(username);
	}
	
	public void removeConnectedUser(String username) {
		this.connectedUsers.remove(username);
	}
	
	
	
	
	// METHODES DE L'OBSERVEUR DE LA SOCKET
	
	public void onDataReceived(String data) {
		String opcode = data.substring(0, 4);
		
		switch (opcode) {
		case "MSG;" :
			this.messageReceived(data);
			break;
			
		case "CNT;" :
			this.connectionReceived(data);
			break;
			
		case "DNT;" :
			this.disconnectionReceived(data);
			break;
			
		default :
			System.err.println("[Server] Invalid OPCODE : " + opcode);
			return;
		}

	}



	public void messageReceived(String data){
		// On découpe la trame reçue
		/* Protocole : MSG;Nickname;Time;IP;Message */
		String[] dataPart = data.split(";");
						
		// On créé le tableau d'arguments
		Object[] args = new String[3];
		args[0] = dataPart[1];
		args[1] = dataPart[3];
		// Si le message envoyé est vide
		if(dataPart.length == 5){
			args[2] = dataPart[4];
		} else {
			args[2] = " ";
		}
		
		//On notifie le controlleur
		this.notifyEvent("onMessageReceived", args);
	}
	
	public void connectionReceived(String data){
		// On découpe la trame reçue
		/* Protocole : CNT;Nickname;IP;NewConnection */
		String[] dataPart = data.split(";");
								
		// On créé le tableau d'arguments
		Object[] args = new String[2];
		// Si le message envoyé est vide
		if(dataPart.length > 1){
			args[0] = dataPart[1];
			args[1] = dataPart[2];
			
			/*
			switch(dataPart[3]){
			case "true" :
				args[2] = true;
				break;
			case "false" :
				args[2] = false;
				break;
			default :
				// Log
				System.err.println("[Client] Error while receiving connected clients");
				return;
			}
			*/
		
			// On met à jour la liste des clients connectés dans le modèle
			this.connectedUsers.add(dataPart[1]);
				
			//On notifie le controlleur
			this.notifyEvent("onUserConnected", args);
		}
		
	}
	
	private void disconnectionReceived(String data) {
		// On découpe la trame reçue
		/* Protocole : DNT;Nickname;IP */
		String[] dataPart = data.split(";");
										
		// On créé le tableau d'arguments
		Object[] args = new String[2];
		args[0] = dataPart[1];
		args[1] = dataPart[2];
		
		// On notifie le controlleur
		this.notifyEvent("onUserDisconnected", args);
	}
	
	
	
	
	// METHODES DE L'OBSERVABLE

	
	public void addListener(ModelListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(ModelListener listener) {
		this.listeners.remove(listener);
	}
	
	public void notifyEvent(String methodName, Object... args) {
		// Les trois petits points sont une élipse
		// Object...   =   Object[n]
		
		// Chercher la bonne méthode dans l'interface
		Method methodCall = null;
		for (Method method : ModelListener.class.getMethods()) {
			if (methodName.equals(method.getName())) {
				methodCall = method;
				break;
			}
		}
		if (methodCall == null) {
			throw new IllegalArgumentException("Event " + methodName + " doesn't exist");
		}
		
		// Parcourir les listeners
		for (ModelListener listener : this.listeners) {
			// Appeler la méthode sur le listener
			try {
				methodCall.invoke(listener, args);
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				System.err.println("Erreur lors du dispatch de l'event " 
						+ methodName + " sur le listener " 
						+ listener.getClass());
				e.printStackTrace();
			}
		}
	}
	
}
