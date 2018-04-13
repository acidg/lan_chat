package org.acidg.lanchat;

import java.awt.Event;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.acidg.lanchat.networking.BroadcastHandler;
import org.acidg.lanchat.networking.ClientList;
import org.acidg.lanchat.networking.ConversationManager;
import org.acidg.lanchat.view.MainPanel;
import org.acidg.lanchat.view.tray.LanChatTrayIcon;

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
				frame.setVisible(false);
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// Ignore
			}

			@Override
			public void windowIconified(WindowEvent e) {
				if(e.getNewState()==Event.WINDOW_ICONIFY){
                    frame.setVisible(false);
				}
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
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		SwingUtilities.invokeLater(() -> {
			new LanChatTrayIcon(frame, conversationManager, clientList);
			
			frame.setSize(800, 600);
			frame.add(new MainPanel(clientList, broadcastHandler, conversationManager).getComponent());
			frame.setVisible(Settings.INSTANCE.getShowOnStartup());
		});
	}
}
