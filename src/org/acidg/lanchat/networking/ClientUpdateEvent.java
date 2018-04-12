package org.acidg.lanchat.networking;

import java.util.Collection;

public class ClientUpdateEvent {
	public enum EChangeType {
		ADDED, REMOVED, STATE_CHANGED, NAME_CHANGED;
	}

	public final EChangeType type;
	public final Collection<Client> clients;
	public final Client client;

	protected ClientUpdateEvent(EChangeType type, Client client, Collection<Client> clients) {
		this.type = type;
		this.client = client;
		this.clients = clients;
	}
	
}
