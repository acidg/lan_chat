package org.acidg.lanchat.view;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.acidg.lanchat.networking.BroadcastHandler;
import org.acidg.lanchat.networking.Client;
import org.acidg.lanchat.networking.ClientList;
import org.acidg.lanchat.networking.ClientUpdateEvent;
import org.acidg.lanchat.networking.ConversationManager;
import org.acidg.lanchat.networking.IClientUpdateListener;
import org.acidg.lanchat.networking.IMessageListener;
import org.acidg.lanchat.networking.Message;

public class MainPanel implements IClientUpdateListener, IMessageListener {
	private ClientList clientList;
	private JPanel mainPanel;
	private JList<Client> clientListPanel;
	private DefaultListModel<Client> listModel;
	private BroadcastHandler broadcastHandler;
	private JPanel chatPanelContainer;
	
	private Map<String, ChatPanel> chatPanels;
	private ConversationManager conversationManager;
	
	public MainPanel(ClientList clientList, BroadcastHandler broadcastHandler, ConversationManager conversationManager) {
		this.clientList = clientList;
		this.broadcastHandler = broadcastHandler;
		this.conversationManager = conversationManager;
		chatPanels = new HashMap<String, ChatPanel>();
		
		initComponent();
	}

	@Override
	public void handleMessage(Message message) {
		String clientId = message.fromClientId;
				
		ChatPanel chatPanel = chatPanels.get(clientId);
		if (chatPanel == null) {
			chatPanel = new ChatPanel(clientList.getClient(clientId), conversationManager);
			chatPanels.put(clientId, chatPanel);
		}
		
		chatPanel.addMessage(message);
	}

	@Override
	public void clientUpdated(ClientUpdateEvent e) {
		switch (e.type) {
		case ADDED:
			listModel.addElement(e.client);
			chatPanels.put(e.client.id, new ChatPanel(e.client, conversationManager));
			break;
		case REMOVED:
			listModel.removeElement(e.client);
			chatPanels.remove(e.client.id);
			break;
		case STATE_CHANGED:
			listModel.removeElement(e.client);
			listModel.addElement(e.client);
			break;
		default:
			// ignore
		}
	}
	
	public JPanel getComponent() {
		return mainPanel;
	}
	
	public void removeChatFor(String clientId) {
		throw new RuntimeException("Not implemented yet");
	}

	private void initComponent() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(5, 5));
		
		listModel = new DefaultListModel<Client>();
		clientListPanel = new JList<Client>();
		clientListPanel.setModel(listModel);
		mainPanel.add(new JScrollPane(clientListPanel), BorderLayout.WEST);

		chatPanelContainer = new JPanel(new BorderLayout());
		mainPanel.add(chatPanelContainer, BorderLayout.CENTER);
		
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> new Thread(() -> broadcastHandler.sendDiscoverBroadcast()).start());
		mainPanel.add(refreshButton, BorderLayout.SOUTH);

		addListeners();
	}

	private void addListeners() {
		clientList.addClientUpdateListener(this);
		
		clientListPanel.addListSelectionListener(e -> openChatForSelectedClient());
		conversationManager.addMessageListener(this);
	}

	private void openChatForSelectedClient() {
		final Client selectedClient = clientListPanel.getSelectedValue();
		
		if (selectedClient == null) {
			return;
		}
		
		SwingUtilities.invokeLater(() -> {
			synchronized (chatPanels) {
				ChatPanel chatPanel = chatPanels.get(selectedClient.id);
				
				if (chatPanel == null) {
					chatPanel = new ChatPanel(selectedClient, conversationManager);
					chatPanels.put(selectedClient.id, chatPanel);
				}
				
				chatPanelContainer.removeAll();
				chatPanelContainer.add(chatPanel.getComponent(), BorderLayout.CENTER);
				chatPanelContainer.revalidate();
			}
		});
	}
}
