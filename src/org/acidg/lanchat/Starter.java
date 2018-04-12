package org.acidg.lanchat;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.acidg.lanchat.networking.BroadcastHandler;
import org.acidg.lanchat.networking.ClientList;
import org.acidg.lanchat.networking.ConversationManager;
import org.acidg.lanchat.view.MainPanel;

public class Starter {
	public static void main(String[] args) {
		ClientList clientList = new ClientList();
		ConversationManager conversationManager = new ConversationManager(clientList);
		BroadcastHandler broadcastHandler = new BroadcastHandler(conversationManager,
				Settings.INSTANCE.getBroadcastingPort());

		JFrame frame = new JFrame("Lan Chat");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		SwingUtilities.invokeLater(() -> {
			frame.setSize(800, 600);
			frame.add(new MainPanel(clientList, broadcastHandler, conversationManager).getComponent());
			frame.setVisible(true);
		});
	}
}
