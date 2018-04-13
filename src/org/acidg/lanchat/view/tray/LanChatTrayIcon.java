package org.acidg.lanchat.view.tray;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.acidg.lanchat.Settings;
import org.acidg.lanchat.networking.Client;
import org.acidg.lanchat.networking.ClientList;
import org.acidg.lanchat.networking.ConversationManager;
import org.acidg.lanchat.networking.IMessageListener;
import org.acidg.lanchat.networking.Message;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.Notifications;
import com.github.plushaze.traynotification.notification.TrayNotification;

public class LanChatTrayIcon implements IMessageListener {
	private static final String POPUP_MENU_TITLE = "LAN Chat";
	private static final Logger LOGGER = Logger.getLogger(LanChatTrayIcon.class.getName());
	private static final String NORMAL_TRAY_IMAGE_PATH = "tray_icon.png";
	private static final String NOTIFY_TRAY_IMAGE_PATH = "tray_icon_notify.png";
	private static final String LAN_CHAT_TOOLTIP = "LAN Chat";
	private static final String CUSTOM_ACTION_TITLE = "CUSTOM_ACTION";
	private static final String EXIT_ACTION_TITLE = "Exit";
	private static final String NEW_MESSAGE_TITLE = "New Message from ";
	private TrayIcon trayIcon;

	private SystemTray sysTray;
	private JPopupMenu popupMenu;
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

		trayIcon = new TrayIcon(normalTrayIcon, LAN_CHAT_TOOLTIP);
		trayIcon.addMouseListener(new MouseAdapter() {
			// From https://stackoverflow.com/a/14681503
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					openChatWindow();
				}

				if (e.getButton() == MouseEvent.BUTTON2 && e.getClickCount() == 1) {
					Rectangle bounds = getSafeScreenBounds(e.getPoint());
					Point point = e.getPoint();
					int x = point.x;
					int y = point.y;
					if (y < bounds.y) {
						y = bounds.y;
					} else if (y > bounds.y + bounds.height) {
						y = bounds.y + bounds.height;
					}
					if (x < bounds.x) {
						x = bounds.x;
					} else if (x > bounds.x + bounds.width) {
						x = bounds.x + bounds.width;
					}
					if (x + popupMenu.getPreferredSize().width > bounds.x + bounds.width) {
						x = (bounds.x + bounds.width) - popupMenu.getPreferredSize().width;
					}
					if (y + popupMenu.getPreferredSize().height > bounds.y + bounds.height) {
						y = (bounds.y + bounds.height) - popupMenu.getPreferredSize().height;
					}
					popupMenu.setLocation(x, y);
					popupMenu.setVisible(true);
				}
			}
		});

		try {
			sysTray.add(trayIcon);
		} catch (AWTException e) {
			LOGGER.warning("Could not add tray icon! " + e.getMessage());
		}
	}

	public void handleMessage(Message message) {
		showNotification(message);
		trayIcon.setImage(notifyTrayIcon);
	}

	private void showNotification(Message message) {
		SwingUtilities.invokeLater(() -> {
			TrayNotification notification = new TrayNotification();
			Client client = clientList.getClient(message.fromClientId);
			notification.setTitle(NEW_MESSAGE_TITLE + client.username);
			notification.setMessage(message.message);
			notification.setAnimation(Animations.POPUP);
			notification.setNotification(Notifications.SUCCESS);
			notification.showAndWait();
			notification.setOnShown(e -> openChatWindow());
			notification.setOnDismiss(e -> trayIcon.setImage(normalTrayIcon));
		});
	}

	private void openChatWindow() {
		trayIcon.setImage(normalTrayIcon);
		chatWindow.setVisible(true);
	}

	private JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu(POPUP_MENU_TITLE);

		JMenuItem openItem = new JMenuItem(LAN_CHAT_TOOLTIP);
		openItem.addActionListener(e -> openChatWindow());
		popupMenu.add(openItem);

		popupMenu.addSeparator();

		JMenuItem customActionItem = new JMenuItem(CUSTOM_ACTION_TITLE);
		customActionItem.addActionListener(e -> {
			for (Client client : clientList.getAllClients()) {
				conversationManager.sendMessage(new Message(Settings.INSTANCE.getCustomMessage(), new Date()),
						client.id);
			}
		});
		popupMenu.add(customActionItem);

		popupMenu.addSeparator();

		JMenuItem exitItem = new JMenuItem(EXIT_ACTION_TITLE);
		openItem.addActionListener(e -> {
			conversationManager.closeAllConnections();
			System.exit(0);
		});
		popupMenu.add(exitItem);

		return popupMenu;
	}

	private static Rectangle getSafeScreenBounds(Point pos) {
		Rectangle bounds = getScreenBoundsAt(pos);
		Insets insets = getScreenInsetsAt(pos);

		bounds.x += insets.left;
		bounds.y += insets.top;
		bounds.width -= (insets.left + insets.right);
		bounds.height -= (insets.top + insets.bottom);

		return bounds;
	}

	private static Insets getScreenInsetsAt(Point pos) {
		GraphicsDevice gd = getGraphicsDeviceAt(pos);
		Insets insets = null;
		if (gd != null) {
			insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
		}
		return insets;
	}

	private static Rectangle getScreenBoundsAt(Point pos) {
		GraphicsDevice gd = getGraphicsDeviceAt(pos);
		Rectangle bounds = null;
		if (gd != null) {
			bounds = gd.getDefaultConfiguration().getBounds();
		}
		return bounds;
	}

	private static GraphicsDevice getGraphicsDeviceAt(Point pos) {
		GraphicsDevice device = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice lstGDs[] = ge.getScreenDevices();

		ArrayList<GraphicsDevice> lstDevices = new ArrayList<GraphicsDevice>(lstGDs.length);

		for (GraphicsDevice gd : lstGDs) {
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			Rectangle screenBounds = gc.getBounds();

			if (screenBounds.contains(pos)) {
				lstDevices.add(gd);
			}
		}

		if (lstDevices.size() > 0) {
			device = lstDevices.get(0);
		} else {
			device = ge.getDefaultScreenDevice();
		}

		return device;
	}
}
