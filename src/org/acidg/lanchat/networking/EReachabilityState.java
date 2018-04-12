package org.acidg.lanchat.networking;

public enum EReachabilityState {
	REACHABLE("User is online"),
	PENDING("Waiting for answer");
	
	public final String description;
	
	EReachabilityState(String description) {
		this.description = description;
	}
}
