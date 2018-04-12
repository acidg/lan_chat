package org.acidg.lanchat.networking;

/** Types of packets. */
public enum EMessageType {
	/** Discover other clients via broadcast. */
	DISCOVER,
	/** Offer another client a connection for messaging. */
	OFFER,
	/** Send a text message. */
	MESSAGE,
	/** Heart beat message to check whether the other client is reachable. Can also be used to change the name. */
	HEATBEAT;
}
