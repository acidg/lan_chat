package org.acidg.lanchat.networking;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class PacketFactory {
	private static final Logger LOGGER = Logger.getLogger(PacketFactory.class.getName());

	/** Creates a DISCOVER packet. */
	public static DatagramPacket createDiscoverPacket(InetAddress address, int port, String name, String id) {
		byte[] data;
		try {
			data = String.format("%s:%s:%s", EMessageType.DISCOVER.toString(), name, id).getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warning("Can not encode string: " + e.getMessage());
			data = String.format("%s:%s:%s", EMessageType.DISCOVER.toString(), name, id).getBytes();
		}
		return new DatagramPacket(data, data.length, address, port);
	}

	/** Creates a OFFER packet. */
	public static DatagramPacket createOfferPacket(InetAddress address, int broadcastPort, String name, String id,
			int offerPort) {
		byte[] data;
		try {
			data = String.format("%s:%s:%s:%d", EMessageType.OFFER.toString(), name, id, offerPort).getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warning("Can not encode string: " + e.getMessage());
			data = String.format("%s:%s:%s:%d", EMessageType.OFFER.toString(), name, id, offerPort).getBytes();
		}
		return new DatagramPacket(data, data.length, address, broadcastPort);
	}

	/** Creates a MESSAGE packet. */
	public static byte[] createMessagePacket(String content) {
		try {
			return String.format("%s:%s\n", EMessageType.MESSAGE.toString(), content).getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warning("Can not encode string: " + e.getMessage());
			return String.format("%s:%s\n", EMessageType.MESSAGE.toString(), content).getBytes();
		}
	}

	/** Creates a HEARTBEAT packet. */
	public static byte[] createHeatbeatPacket(String username) {
		try {
			return String.format("%s:%s\n", EMessageType.HEATBEAT.toString(), username).getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warning("Can not encode string: " + e.getMessage());
			return String.format("%s:%s\n", EMessageType.HEATBEAT.toString(), username).getBytes();
		}
	}
}
