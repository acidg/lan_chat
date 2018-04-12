package org.acidg.lanchat.networking;

import java.net.InetAddress;

public class Client {
	public final String id;
	public final InetAddress address;
	public String username;
	public EReachabilityState state;
	
	public Client(InetAddress address, String username, String id) {
		this.address = address;
		this.id = id;
		this.username = username;
		state = EReachabilityState.PENDING;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
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
		return other.id.equals(id);
	}

	@Override
	public String toString() {
		return String.format("[%s] %s(%s)", state.toString(), username, address.getHostAddress());
	}
}
