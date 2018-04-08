package org.acidg.lanchat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.logging.Logger;
import java.util.HashMap;

public class ConversationManager {
	private static final Logger LOGGER = Logger.getLogger(ConversationManager.class.getName());
	private static final int CONNECTION_TIMEOUT = 5;
	private static final int WAIT_TIMEOUT = 30;

	private Map<Client, Socket> connections;
	private ClientList clientList;

	private ConversationManager(ClientList clientList) {
		this.clientList = clientList;
		connections = new HashMap<Client, Socket>();
	}

	/**
	 * Establishes a connection to a client which sent back the port and 
	 * username after receiving our DISCOVER package.
	 */
	public void establishMessagingConnection(Client client) {
		try {
			Socket socket = new Socket(client.address, client.messagingPort);
			connections.put(client, socket);
			client.state = EReachabilityState.REACHABLE;
			clientList.updateClient(client);
		} catch (IOException e) {
			LOGGER.warning("Could not establish a connection with " + client);
			client.state = EReachabilityState.OFFLINE;
			clientList.updateClient(client);
		}
	}

	public void waitForConnection(Client client, int port) {
		try {
			ServerSocket socket = new ServerSocket(port);
			socket.setSoTimeout(WAIT_TIMEOUT);
			Socket clientSocket = socket.accept();
			socket.close();
			client.messagingPort = clientSocket.getPort();
			client.state = EReachabilityState.REACHABLE;
			clientSocket.setKeepAlive(true);
			connections.put(client, clientSocket);
			clientList.updateClient(client);
		} catch (IOException e) {
			LOGGER.warning("Error during wainting for connection: " + e.getMessage());
		}
	}

	private void heartBeatLoop() {
		while(!Thread.interrupted() ) {
			for (Client client : connections.keySet()) {
				Socket socket = connections.get(client);
				try {
					socket.getOutputStream().write("Hello?".getBytes());
				} catch (IOException e) {
					LOGGER.warning("Connection to " + client + " seems down: " + e.getMessage());
					client.state = EReachabilityState.OFFLINE;
					clientList.updateClient(client);
				}
			}
		}
	}
}
