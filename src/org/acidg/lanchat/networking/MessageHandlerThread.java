package org.acidg.lanchat.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.logging.Logger;

import org.acidg.lanchat.Settings;

import com.google.gson.Gson;

public class MessageHandlerThread implements Runnable {
	/** Maximum time interval between two heart beats */
	private static final int MAX_HEATBEAT_INTERVAL = 30000;
	private static final Gson GSON = new Gson();
	private Socket socket;
	private String clientId;
	private Logger logger;
	private Set<IMessageListener> messageListeners;
	private ClientList clientList;

	public MessageHandlerThread(Socket socket, String clientId, Set<IMessageListener> messageListeners,
			ClientList clientList) {
		this.socket = socket;
		this.clientId = clientId;
		this.messageListeners = messageListeners;
		this.clientList = clientList;
		this.logger = Logger.getLogger(MessageHandlerThread.class.getName() + " for client " + clientId);
	}

	@Override
	public void run() {
		Thread.currentThread().setName("MessageHandler " + socket.getInetAddress().getHostAddress());
		while (!Thread.interrupted() && !socket.isClosed()) {
			try {
				socket.setSoTimeout(MAX_HEATBEAT_INTERVAL);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

				String content = input.readLine();
				if (content == null) {
					finalizeConnection();
					return;
				}
				String[] parts = content.split(":");
				if (parts.length < 2) {
					logger.info("Got weird message from client with id " + clientId + ", message: "
							+ String.join(":", parts));
					return;
				}
				
				switch (EMessageType.valueOf(parts[0])) {
				case MESSAGE:
					logger.info("Got message from " + socket.getInetAddress().getHostAddress() + ": " + String.join(":", parts));
					notifyMessageListeners(GSON.fromJson(content.substring(content.indexOf(":") + 1), Message.class));
					break;
				case HEATBEAT:
					logger.info("Got Heartbeat from " + socket.getInetAddress().getHostAddress());
					// We reply implicitly via TCP acknowledgement
					clientList.updateClientUsername(clientId, parts[1]);
					break;
				default:
					logger.info("Got weird message from client with id " + clientId + ", message: "
							+ String.join(":", parts));
				}
			} catch (SocketTimeoutException e) {
				logger.info("Sending Heartbeat to " + clientList.getClient(clientId));
				sendHeartbeat();
			} catch (IOException e) {
				logger.info("Input stream closed: " + e.getMessage());
				finalizeConnection();
				return;
			}
		}
		
		finalizeConnection();
	}

	private void finalizeConnection() {
		// User wants to exit application, or client closed connection
		logger.info("Closing connection to " + clientList.getClient(clientId));
		clientList.removeClient(clientId);
		try {
			socket.close();
		} catch (IOException e) {
			// ignore, probably already closed
		}
	}

	/** Notifies all listeners about a client state or username change. */
	private void notifyMessageListeners(Message message) {
		message.fromClientId = clientId;
		for (IMessageListener listener : messageListeners) {
			listener.handleMessage(message);
		}
	}

	/**
	 * Sends a HeartBeat message to the client to check the state.
	 * 
	 * TODO: Less spaming: Reset Timeout when packet received.
	 */
	private void sendHeartbeat() {
		// Get username fresh, since it could have changed.
		String username = Settings.INSTANCE.getUsername();
		try {
			socket.getOutputStream().write(PacketFactory.createHeatbeatPacket(username));
			clientList.updateClientState(clientId, EReachabilityState.REACHABLE);
		} catch (IOException e) {
			logger.warning("Connection to " + clientList.getClient(clientId) + " seems down: " + e.getMessage());
			finalizeConnection();
		}
	}
}
