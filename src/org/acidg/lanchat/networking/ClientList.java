package org.acidg.lanchat.networking;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;

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

	/**
	 * Adds a client to the list. Returns true, if the client was added
	 * successfully, false, if a client with the same id is already in the list.
	 */
	public boolean addClient(Client client) {
		synchronized (clients) {
			if (clients.containsKey(client.id)) {
				return false;
			}

			clients.put(client.id, client);
			return true;
		}
	}

	public Client getClient(String clientId) {
		synchronized (clients) {
			return clients.get(clientId);
		}
	}

	public void removeClient(String clientId) {
		synchronized (clients) {
			clients.remove(clientId);
		}
	}

	public void updateClientState(String clientId, EReachabilityState state) {
		synchronized (clients) {
			Client client = clients.get(clientId);
			if (client == null) {
				// Client does not exist
				Logger.getLogger(this.getClass().getName()).warning("Client with id " + clientId + " does not exist!");
				return;
			}

			if (client.state.equals(state)) {
				return;
			}

			client.state = state;

			for (IClientUpdateListener listener : listeners) {
				listener.clientsUpdated(clients.values());
			}
		}
	}

	public void updateClientUsername(String clientId, String username) {
		synchronized (clients) {
			Client client = clients.get(clientId);
			if (client == null) {
				// Client does not exist
				Logger.getLogger(this.getClass().getName()).warning("Client with id " + clientId + " does not exist!");
				return;
			}

			if (client.username.equals(username)) {
				return;
			}

			client.username = username;

			for (IClientUpdateListener listener : listeners) {
				listener.clientsUpdated(clients.values());
			}
		}
	}

	public boolean hasActiveConnection(String clientId) {
		return clients.containsKey(clientId);
	}
}
