package org.acidg.lanchat.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.text.DateFormat;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;

import org.acidg.lanchat.networking.KeyManager;
import org.acidg.lanchat.networking.Message;

public class ChatCellRenderer implements ListCellRenderer<Message> {
	private static final Color MINE_COLOR = Color.GREEN;
	private static final Color OTHER_COLOR = Color.GRAY;

	@Override
	public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		JPanel chatMessage = new JPanel();
		JTextPane contentPane = new JTextPane();
		contentPane.setText(value.message);
		JLabel timestampLabel = new JLabel(DateFormat.getDateTimeInstance().format(value.timestamp));
		
		if (value.fromClientId.equals(KeyManager.INSTANCE.id)) {
			chatMessage.setLayout(new FlowLayout(FlowLayout.RIGHT));
			chatMessage.setBackground(MINE_COLOR);
			chatMessage.add(contentPane);
			chatMessage.add(timestampLabel);
		} else {
			chatMessage.setLayout(new FlowLayout(FlowLayout.LEFT));
			chatMessage.setBackground(OTHER_COLOR);
			chatMessage.add(timestampLabel);
			chatMessage.add(contentPane);
		}

		timestampLabel.setForeground(Color.LIGHT_GRAY);
		
		
		return chatMessage;
	}
}
