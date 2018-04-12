package org.acidg.lanchat.networking;

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

import org.acidg.lanchat.Settings;

public class BroadcastHandler {
	private static final Logger LOGGER = Logger.getLogger(BroadcastHandler.class.getName());

	private static final int MAX_PACKAGE_LENGTH = 1024;

	private DatagramSocket broadcastSocket;

	private Thread broadcastListenThread;

	private ConversationManager conversationManager;

	private int broadcastPort;

	public BroadcastHandler(ConversationManager conversationManager, int broadcastPort) {
		this.conversationManager = conversationManager;
		this.broadcastPort = broadcastPort;
		try {
			broadcastSocket = new DatagramSocket(broadcastPort);
		} catch (SocketException e) {
			LOGGER.severe("Could not open socket!");
			throw new RuntimeException(e);
		}

		broadcastListenThread = new Thread(() -> handleMessages());
		broadcastListenThread.start();

		new Thread(() -> sendDiscoverBroadcast()).start();
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
	 * Handles DISCOVER and OFFER messages from clients.
	 */
	private void handleMessages() {
		while (!Thread.interrupted()) {
			byte[] buffer = new byte[MAX_PACKAGE_LENGTH];
			DatagramPacket packet = new DatagramPacket(buffer, MAX_PACKAGE_LENGTH);

			try {
				broadcastSocket.receive(packet);
				String[] parts = new String(packet.getData(), 0, packet.getLength()).split(":");
				
				if (parts.length < 1) {
					continue;
				}
				
				switch (EMessageType.valueOf(parts[0])) {
				case DISCOVER:
					if (parts.length == 3) {
						if (KeyManager.INSTANCE.id.equals(parts[2])) {
							// Do not reply to ourself
							break;
						}
						LOGGER.info("Got DISCOVER: " + String.join(":", parts));
						replyOfferMessage(packet.getAddress(), parts[1], parts[2]);
						break;
					}
				case OFFER:
					if (parts.length == 4) {
						LOGGER.info("Got OFFER: " + String.join(":", parts));
						conversationManager.acceptMessagingConnection(packet.getAddress(), parts[1], parts[2], Integer.parseInt(parts[3]));
						break;
					}
				default:
					LOGGER.info("Got weird message from address " + packet.getAddress().getHostAddress() + ", message: "
							+ String.join(":", parts));
				}
			} catch (IOException e) {
				LOGGER.warning("Error listening for broadcast packets");
			}
		}
	}

	/** Sends a reply to a received DISCOVER message */
	private void replyOfferMessage(InetAddress address, String username, String clientId) {
		try {
			int offerPort = conversationManager.offerMessagingConnection(address, username, clientId);
			if (offerPort == -1) {
				return;
			}
			broadcastSocket.send(PacketFactory.createOfferPacket(address, broadcastPort,
					Settings.INSTANCE.getUsername(), KeyManager.INSTANCE.id, offerPort));
		} catch (IOException e) {
			LOGGER.warning("Error sending response to DISCOVER message: " + e.getMessage());
		}
	}

	/**
	 * Broadcasts a DISCOVER message to all networks we are connected to.
	 */
	public void sendDiscoverBroadcast() {
		String username = Settings.INSTANCE.getUsername();
		for (InetAddress broadcastAddress : getNetworkBroadcastAddresses()) {
			try {
				broadcastSocket.send(PacketFactory.createDiscoverPacket(broadcastAddress, broadcastPort, username, KeyManager.INSTANCE.id));
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
