package org.acidg.lanchat;

public enum EReachabilityState {
	REACHABLE("User is online"),
	PENDING("Waiting for answer"),
	OFFLINE("User is offline");
	
	public final String description;
	
	EReachabilityState(String description) {
		this.description = description;
	}
}
