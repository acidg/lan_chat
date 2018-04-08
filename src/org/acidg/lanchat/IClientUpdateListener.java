package org.acidg.lanchat;

import java.util.Collection;

public interface IClientUpdateListener {
	public void clientsUpdated(Collection<Client> clients);
}
