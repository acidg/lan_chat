package org.acidg.lanchat;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.acidg.lanchat.networking.BroadcastHandler;
import org.acidg.lanchat.networking.ClientList;
import org.acidg.lanchat.networking.ConversationManager;
import org.acidg.lanchat.view.MainPanel;

public class Starter {
	private ClientList clientList;
	private ConversationManager conversationManager;
	BroadcastHandler broadcastHandler;
	public static void main(String[] args) {
		new Starter().start();
	}
	
	public Starter() {
		clientList = new ClientList();
		conversationManager = new ConversationManager(clientList);
		broadcastHandler = new BroadcastHandler(conversationManager,
				Settings.INSTANCE.getBroadcastingPort());
	}
	
	public void start() {
		JFrame frame = new JFrame("Lan Chat");
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
				// Ignore
			}

			@Override
			public void windowClosing(WindowEvent e) {
				broadcastHandler = null;
				conversationManager = null;
				clientList = null;
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// Ignore
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// Ignore
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// Ignore
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// Ignore
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// Ignore
			}
		});
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		SwingUtilities.invokeLater(() -> {
			frame.setSize(800, 600);
			frame.add(new MainPanel(clientList, broadcastHandler, conversationManager).getComponent());
			frame.setVisible(true);
		});
	}
}
