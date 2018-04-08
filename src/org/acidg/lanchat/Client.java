package org.acidg.lanchat;

import java.net.InetAddress;

public class Client {
	public final InetAddress address;
	public int messagingPort;
	public final String username;
	public EReachabilityState state;
	
	public Client(InetAddress address, String username, int messagingPort) {
		this.address = address;
		this.messagingPort = messagingPort;
		this.username = username;
		state = EReachabilityState.PENDING;
	}
	
	/** Returns a key, which identifies this client */
	public String getKey() {
		return username + "(" + address.getHostAddress() + ")";
	}

	@Override
	public int hashCode() {
		return username.hashCode() + address.getHostAddress().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj == this) {
			return true;
		}
		
		if (!(obj instanceof Client)) {
			return false;
		}
		
		Client other = (Client) obj;
		return other.address.equals(address) && other.username.equals(username);
	}

	@Override
	public String toString() {
		return String.format("%s(%s:%d)", username, address.getHostAddress(), messagingPort);
	}
}
