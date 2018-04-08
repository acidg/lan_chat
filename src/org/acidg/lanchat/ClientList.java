package org.acidg.lanchat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class ClientList {
	/** List of clients, used a monitor object to prevent race conditions */
	private Map<String, Client> clients;
	
	private Set<IClientUpdateListener> listeners;
	
	public ClientList() {
		listeners = Collections.newSetFromMap(new WeakHashMap<IClientUpdateListener, Boolean>());
		clients = new HashMap<String, Client>();
	}

	public void addClientUpdateListener(IClientUpdateListener listener) {
		synchronized (clients) {
			listeners.add(listener);
			listener.clientsUpdated(clients.values());
		}
	}

	public void removeClientUpdateListener(IClientUpdateListener listener) {
		synchronized (clients) {
			listeners.remove(listener);
		}
	}

	
	public void updateClient(Client client) {
		synchronized (clients) {
			clients.put(client.getKey(), client);
			for (IClientUpdateListener listener : listeners) {
				listener.clientsUpdated(clients.values());
			}
		}
	}
}
