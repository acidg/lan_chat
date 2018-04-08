package org.acidg.lanchat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class BroadcastHandler {
	private static final Logger LOGGER = Logger.getLogger(BroadcastHandler.class.getName());

	private static final int MAX_PACKAGE_LENGTH = 1024;

	private DatagramSocket broadcastSocket;
	private Set<InetAddress> networkBroadcastAddresses;

	private Thread broadcastListenThread;

	private ClientList clientList;
	private ConversationManager conversationManager;

	private int broadcastPort;

	public BroadcastHandler(ClientList clientList, ConversationManager conversationManager, int broadcastPort) {
		this.clientList = clientList;
		this.conversationManager = conversationManager;
		this.broadcastPort = broadcastPort;
		networkBroadcastAddresses = getNetworkBroadcastAddresses();
		try {
			broadcastSocket = new DatagramSocket(broadcastPort);
		} catch (SocketException e) {
			LOGGER.severe("Could not open socket!");
			throw new RuntimeException(e);
		}

		broadcastListenThread = new Thread(() -> handleBroadcasts());
		broadcastListenThread.run();

		new Thread(() -> sendDiscoverBroadcast()).run();
	}

	private Set<InetAddress> getNetworkBroadcastAddresses() {
		HashSet<InetAddress> listOfBroadcasts = new HashSet<InetAddress>();
		try {
			Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();

			while (list.hasMoreElements()) {
				NetworkInterface iface = (NetworkInterface) list.nextElement();

				if (iface == null) {
					continue;
				}

				if (!iface.isLoopback() && iface.isUp()) {
					Iterator<InterfaceAddress> it = iface.getInterfaceAddresses().iterator();
					while (it.hasNext()) {
						InterfaceAddress address = (InterfaceAddress) it.next();
						if (address == null) {
							continue;
						}
						InetAddress broadcast = address.getBroadcast();
						if (broadcast != null) {
							LOGGER.info("Found broadcast: " + broadcast);
							listOfBroadcasts.add(broadcast);
						}
					}
				}
			}
		} catch (SocketException e) {
			LOGGER.warning("Error determining broadcast addresses");
		}

		return listOfBroadcasts;
	}

	/**
	 * Handles DISCOVER messages and responses from clients.
	 */
	private void handleBroadcasts() {
		while(!Thread.interrupted()) {
			byte[] buffer = new byte[MAX_PACKAGE_LENGTH];
			DatagramPacket packet = new DatagramPacket(buffer, MAX_PACKAGE_LENGTH);
			
			try {
				broadcastSocket.receive(packet);
				String[] parts = new String(packet.getData(), 0, packet.getLength()).split(":");
				Client client = new Client(packet.getAddress(), parts[0], Integer.parseInt(parts[1]));
				clientList.updateClient(client);
				new Thread(() -> conversationManager.establishMessagingConnection(client)).run();
			} catch (IOException e) {
				LOGGER.warning("Error listening for broadcast packets");
			}
		}
	}

	/**
	 * Broadcasts a DISCOVER message to all networks we are connected to.
	 */
	private void sendDiscoverBroadcast() {
		String username = Settings.INSTANCE.getUsername();
		String message = String.format("DISCOVER:%s", username);
		byte[] buffer = message.getBytes();
		for (InetAddress broadcastAddress : networkBroadcastAddresses) {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, this.broadcastPort);
			try {
				broadcastSocket.send(packet);
			} catch (IOException e) {
				LOGGER.warning("Error sending broadcast message on " + broadcastAddress.getHostAddress() + ": "
						+ e.getMessage());
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		broadcastListenThread.interrupt();
		broadcastSocket.close();

	}

}
