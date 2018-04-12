package org.acidg.lanchat.networking;

import java.util.Collection;

public interface IClientUpdateListener {
	public void clientsUpdated(Collection<Client> clients);
}
