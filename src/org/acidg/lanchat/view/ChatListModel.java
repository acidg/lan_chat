package org.acidg.lanchat.view;

import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import org.acidg.lanchat.networking.Message;

public class ChatListModel implements ListModel {

	List<Message> messages;
	
	@Override
	public int getSize() {
		return messages.size();
	}

	@Override
	public Object getElementAt(int index) {
		return messages.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

}
