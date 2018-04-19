package org.acidg.lanchat.view.tray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.acidg.lanchat.Settings;
import org.acidg.lanchat.networking.Client;
import org.acidg.lanchat.networking.ClientList;
import org.acidg.lanchat.networking.ConversationManager;
import org.acidg.lanchat.networking.IMessageListener;
import org.acidg.lanchat.networking.Message;

public class LanChatTrayIcon implements IMessageListener {
	private static final String POPUP_MENU_TITLE = "LAN Chat";
	private static final Logger LOGGER = Logger.getLogger(LanChatTrayIcon.class.getName());
	private static final String NORMAL_TRAY_IMAGE_PATH = "tray_icon_16.png";
	private static final String NOTIFY_TRAY_IMAGE_PATH = "tray_icon_notify_16.png";
	private static final String LAN_CHAT_TOOLTIP = "LAN Chat";
	private static final String CUSTOM_ACTION_TITLE = "CUSTOM_ACTION";
	private static final String EXIT_ACTION_TITLE = "Exit";
	private static final String NEW_MESSAGE_TITLE = "New Message from ";
	private TrayIcon trayIcon;

	private SystemTray sysTray;
	private PopupMenu popupMenu;
	private ConversationManager conversationManager;
	private ClientList clientList;
	private JFrame chatWindow;
	private final Image normalTrayIcon;
	private final Image notifyTrayIcon;

	public LanChatTrayIcon(JFrame chatWindow, ConversationManager conversationManager, ClientList clientList) {
		this.chatWindow = chatWindow;
		this.conversationManager = conversationManager;
		this.clientList = clientList;

		try {
			this.normalTrayIcon = ImageIO.read(ClassLoader.getSystemResource(NORMAL_TRAY_IMAGE_PATH));
			this.notifyTrayIcon = ImageIO.read(ClassLoader.getSystemResource(NOTIFY_TRAY_IMAGE_PATH));
		} catch (IOException e) {
			LOGGER.severe("Could not load image! " + e.getMessage());
			throw new RuntimeException(e);
		}

		if (SystemTray.isSupported()) {
			sysTray = SystemTray.getSystemTray();
		} else {
			LOGGER.warning("Tray icon is not supported!");
			return;
		}

		popupMenu = createPopupMenu();

		trayIcon = new TrayIcon(normalTrayIcon, LAN_CHAT_TOOLTIP, popupMenu);
		trayIcon.setImageAutoSize(true);
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					openChatWindow();
				}
			}
		});

		try {
			sysTray.add(trayIcon);
		} catch (AWTException e) {
			LOGGER.warning("Could not add tray icon! " + e.getMessage());
		}

		conversationManager.addMessageListener(this);
	}

	public void handleMessage(Message message) {
		showNotification(message);
		trayIcon.setImage(notifyTrayIcon);
	}

	private void showNotification(Message message) {
		SwingUtilities.invokeLater(() -> {
			if (chatWindow.isFocused()) {
				return;
			}
			Client client = clientList.getClient(message.fromClientId);
			new MessageNotification(NEW_MESSAGE_TITLE + client.username, message.message, () -> openChatWindow())
					.setVisible(true);
		});
	}

	private void openChatWindow() {
		trayIcon.setImage(normalTrayIcon);
		if (!chatWindow.isVisible()) {
			chatWindow.setVisible(true);
		}
	}

	private PopupMenu createPopupMenu() {
		PopupMenu popupMenu = new PopupMenu(POPUP_MENU_TITLE);

		MenuItem openItem = new MenuItem(LAN_CHAT_TOOLTIP);
		openItem.addActionListener(e -> {
			LOGGER.info("Opening chat window action");
			openChatWindow();
		});
		popupMenu.add(openItem);

		popupMenu.addSeparator();

		MenuItem customActionItem = new MenuItem(CUSTOM_ACTION_TITLE);
		customActionItem.addActionListener(e -> {
			LOGGER.info("Custom action");
			for (Client client : clientList.getAllClients()) {
				conversationManager.sendMessage(new Message(Settings.INSTANCE.getCustomMessage(), new Date()),
						client.id, () -> {}); // TODO: update chat list
			}
		});
		popupMenu.add(customActionItem);

		popupMenu.addSeparator();

		MenuItem exitItem = new MenuItem(EXIT_ACTION_TITLE);
		exitItem.addActionListener(e -> {
			LOGGER.info("exit action");
			conversationManager.closeAllConnections();
			System.exit(0);
		});
		popupMenu.add(exitItem);

		return popupMenu;
	}
}
