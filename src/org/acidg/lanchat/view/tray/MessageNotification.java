package org.acidg.lanchat.view.tray;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class MessageNotification extends JFrame {
	private static final long serialVersionUID = 5759617854638289707L;

	public MessageNotification(String title, String content, Runnable callback) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setUndecorated(true);
		setAlwaysOnTop(true);

		JPanel mainPanel = new JPanel();
		JPanel topPanel = new JPanel();
		JLabel lblContent = new JLabel(content);
		JLabel lblTitle = new JLabel(title);
		JLabel lblIcon = new JLabel(new ImageIcon(ClassLoader.getSystemResource("tray_icon_16.png")));
		JButton btnClose = new JButton("x");

		btnClose.setSize(5, 5);
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});

		mainPanel.setLayout(new BorderLayout(5, 5));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		topPanel.add(lblIcon);
		topPanel.add(lblTitle);
		topPanel.add(btnClose);
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(lblContent, BorderLayout.CENTER);
		getContentPane().add(mainPanel);

		pack();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
		Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
		int x = (int) rect.getMaxX() - getWidth();
		setLocation(x, 0);

		this.rootPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				SwingUtilities.invokeLater(callback);
				close();
			}
		});
	}

	private void close() {
		dispose();
	}
}
