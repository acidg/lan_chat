package org.acidg.lanchat.networking;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class PacketFactory {
	/** Creates a DISCOVER packet. */
	public static DatagramPacket createDiscoverPacket(InetAddress address, int port, String name, String id) {
		byte[] data = String.format("%s:%s:%s", EMessageType.DISCOVER.toString(), name, id).getBytes();
		return new DatagramPacket(data, data.length, address, port);
	}

	/** Creates a OFFER packet. */
	public static DatagramPacket createOfferPacket(InetAddress address, int broadcastPort, String name, String id,
			int offerPort) {
		byte[] data = String.format("%s:%s:%s:%d", EMessageType.OFFER.toString(), name, id, offerPort).getBytes();
		return new DatagramPacket(data, data.length, address, broadcastPort);
	}
	
	/** Creates a MESSAGE packet. */
	public static byte[] createMessagePacket(String content) {
		return String.format("%s:%s\n", EMessageType.MESSAGE.toString(), content).getBytes();
	}

	/** Creates a HEARTBEAT packet. */
	public static byte[] createHeatbeatPacket(String username) {
		return String.format("%s:%s\n", EMessageType.HEATBEAT.toString(), username).getBytes();
	}
}
