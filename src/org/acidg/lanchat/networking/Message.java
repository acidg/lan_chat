package org.acidg.lanchat.networking;

import java.util.Date;

/** Wrapper object representing a message of a client. */
public class Message {
	public final String message;
	public final Date timestamp;
	public transient String fromClientId;
	
	public Message(String message, Date timestamp) {
		this.message = message;
		this.timestamp = timestamp;
	}
	
	public Message(String message, Date timestamp, String clientId) {
		this.message = message;
		this.timestamp = timestamp;
		fromClientId = clientId;
	}
}
