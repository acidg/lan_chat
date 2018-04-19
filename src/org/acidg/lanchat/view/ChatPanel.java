package org.acidg.lanchat.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.acidg.lanchat.networking.Client;
import org.acidg.lanchat.networking.ConversationManager;
import org.acidg.lanchat.networking.KeyManager;
import org.acidg.lanchat.networking.Message;

public class ChatPanel {
	private static final String SEND_BUTTON_TEXT = "Send";

	private final Client selectedClient;

	private JPanel mainPanel;
	private JList<Message> messageList;
	private JTextField messageBox;

	private JButton sendButton;

	private ConversationManager conversationManager;

	private DefaultListModel<Message> listModel;

	public ChatPanel(Client selectedClient, ConversationManager conversationManager) {
		this.selectedClient = selectedClient;
		this.conversationManager = conversationManager;

		initComponent();
	}

	public JPanel getComponent() {
		return mainPanel;
	}

	private void initComponent() {
		mainPanel = new JPanel(new BorderLayout(5, 5));
		mainPanel.setMinimumSize(new Dimension(500, 500));
		listModel = new DefaultListModel<Message>();
		messageList = new JList<Message>(listModel);
		messageList.setCellRenderer(new ChatCellRenderer());

		JPanel messageBoxAndButton = new JPanel(new BorderLayout());
		messageBox = new JTextField();
		sendButton = new JButton(SEND_BUTTON_TEXT);
		messageBoxAndButton.add(messageBox, BorderLayout.CENTER);
		messageBoxAndButton.add(sendButton, BorderLayout.EAST);
		mainPanel.add(new JScrollPane(messageList), BorderLayout.CENTER);
		mainPanel.add(messageBoxAndButton, BorderLayout.SOUTH);

		sendButton.addActionListener(e -> sendMessage());
		messageBox.addActionListener(e -> sendMessage());
	}

	private void sendMessage() {
		String content = messageBox.getText();
		Message message = new Message(content, new Date(), KeyManager.INSTANCE.id);

		conversationManager.sendMessage(message, selectedClient.id, () -> listModel.addElement(message));
		messageBox.setText("");
	}

	public void addMessage(Message message) {
		listModel.addElement(message);
	}
}
