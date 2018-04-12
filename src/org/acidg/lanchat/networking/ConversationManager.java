package org.acidg.lanchat.networking;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class ConversationManager {
	private static final Logger LOGGER = Logger.getLogger(ConversationManager.class.getName());
	/** Timeout for waiting for a client to connect to our offered TCP port */
	private static final int WAIT_FOR_CONNECTION_TIMEOUT = 10000;
	private static final Gson GSON = new Gson();

	private Map<String, Socket> connections;
	private Set<IMessageListener> messageListeners;

	/** List of threads handling connections to clients. Used to take them down. */
	private List<Thread> connectionThreads;
	private ClientList clientList;

	public ConversationManager(ClientList clientList) {
		this.clientList = clientList;
		connections = new HashMap<String, Socket>();
		connectionThreads = new LinkedList<Thread>();
		messageListeners = Collections.newSetFromMap(new WeakHashMap<IMessageListener, Boolean>());
	}

	public void addMessageListener(IMessageListener listener) {
		messageListeners.add(listener);
	}
	
	public void removeMessageListener(IMessageListener listener) {
		messageListeners.add(listener);
	}
	
	/**
	 * Accepts a connection to a client which sent back the port in an OFFER message
	 * after receiving our DISCOVER message.
	 */
	public void acceptMessagingConnection(InetAddress address, String username, String clientId, int messagingPort) {
		if (clientList.hasActiveConnection(clientId)) {
			LOGGER.info("Connection to " + username + "(" + address.getHostAddress() + ") already active");
			return;
		}
		try {
			Socket clientSocket = new Socket(address, messagingPort);
			LOGGER.info("New Messaging Connection with " + username + "(" + address.getHostAddress() + ")");
			clientSocket.setKeepAlive(true);
			startMessagingLoop(clientSocket, address, username, clientId);
		} catch (IOException e) {
			LOGGER.warning("ACCEPT: Could not establish a connection with " + address.getHostAddress() + ": "
					+ e.getMessage());
		}
	}

	/**
	 * Offers a TCP port for a new connection after a DISCOVER message has been
	 * received from a given client. If there already is a connection to the client,
	 * nothing is done.
	 * 
	 * @throws IOException
	 *             If there was an error opening the port.
	 */
	public int offerMessagingConnection(InetAddress address, String username, String clientId) throws IOException {
		if (clientList.hasActiveConnection(clientId)) {
			LOGGER.info("Connection to " + username + "(" + address.getHostAddress() + ") already active");
			return -1;
		}
		final ServerSocket socket = new ServerSocket(0);
		int port = socket.getLocalPort();
		LOGGER.info("Offering connection to " + username + "(" + address.getHostAddress() + ") on port " + port);

		socket.setSoTimeout(WAIT_FOR_CONNECTION_TIMEOUT);

		new Thread(() -> {
			try {
				Socket clientSocket = socket.accept();
				LOGGER.info("New Messaging Connection with " + username + "(" + address.getHostAddress() + ") on port " + port);

				startMessagingLoop(clientSocket, address, username, clientId);
			} catch (IOException e) {
				LOGGER.warning("OFFER: Could not establish a connection with " + address.getHostAddress() + ": "
						+ e.getMessage());
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					// ignore, socket should be closed
				}
			}
		}).start();

		return port;
	}

	public void sendMessage(Message message, String clientId) {
		new Thread(() -> {
			Socket clientSocket = connections.get(clientId);
			if (clientSocket == null) {
				LOGGER.warning("No connection to client " + clientId);
				return;
			}
			
			try {
				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				output.write(EMessageType.MESSAGE.toString());
				output.write(":");
				output.write(GSON.toJson(message));
				output.newLine();
				output.flush();
			} catch (IOException e) {
				LOGGER.warning("Error opening output for client with id " + message.fromClientId + ": " + e.getMessage());
			}
		}).start();
	}

	/**
	 * Starts the messaging loop for the given connected socket. The socket has to
	 * be already connected. Adds the client to the client list.
	 */
	private void startMessagingLoop(Socket clientSocket, InetAddress address, String username, String clientId) {
		LOGGER.info("Starting messaging loop for " + address.getHostAddress());
		Client client = new Client(address, username, clientId);

		try {
			if (!clientList.addClient(client)) {
				clientSocket.getOutputStream().write("DUPLICATE CONNECTION!".getBytes());
				clientSocket.close();
				return;
			}

			clientSocket.setKeepAlive(true);
		} catch (IOException e) {
			LOGGER.warning("Could not start messaging loop: " + e.getMessage());
			clientList.removeClient(clientId);
			return;
		}

		connections.put(clientId, clientSocket);
		clientList.updateClientState(clientId, EReachabilityState.REACHABLE);
		Thread connectionThread = new Thread(
				new MessageHandlerThread(clientSocket, clientId, messageListeners, clientList));
		connectionThread.start();
		connectionThreads.add(connectionThread);

	}

	@Override
	protected void finalize() throws Throwable {
		for (Socket socket : connections.values()) {
			try {
				socket.close();
			} catch (IOException e) {
				// We don't care here, since we just want to quit.
			}
		}
	}

}
