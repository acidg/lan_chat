package org.acidg.lanchat.networking;

import java.util.Random;

import org.apache.commons.codec.binary.Hex;

public class KeyManager {
	public final static KeyManager INSTANCE = new KeyManager();
	
	/** use public key instead? */
	public final String id;
	
	private KeyManager() {
		byte[] randomBytes = new byte[16];
		new Random().nextBytes(randomBytes);
		
		id = Hex.encodeHexString(randomBytes);
		// TODO set up private and public key instead
	}
}
