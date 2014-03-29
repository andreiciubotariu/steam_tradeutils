package com.andrei.tradeutils;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jasypt.util.text.BasicTextEncryptor;

/**
 * 
 * @author Andrei Ciubotariu
 *
 */
public class TradeUtil {
	protected static String key;
	protected static boolean format_24 = true;
	private static Image [] images = new Image [7];
	
	public static void main(String[] args) { 
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		try {
			BufferedReader in = new BufferedReader (new FileReader("config.cfg"));
			String pass = in.readLine().trim();
			textEncryptor.setPassword(pass);
			String eKey = in.readLine().trim();
			key = textEncryptor.decrypt(eKey);
			in.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (key == null){
			key = JOptionPane.showInputDialog (null,"Enter your key","API key required",JOptionPane.PLAIN_MESSAGE);
			if (key != null && key.length() > 0){
				int save = JOptionPane.showConfirmDialog(null, "Save key to file?", "Key saving", JOptionPane.YES_NO_OPTION);
				if (save == JOptionPane.YES_OPTION){
					try {
						PrintWriter out = new PrintWriter (new FileWriter ("config.cfg"));
						String pass = String.valueOf (System.currentTimeMillis());
						textEncryptor.setPassword(pass);
						String eKey = textEncryptor.encrypt(key);
						out.println (pass);
						out.println(eKey);
						out.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}

		//
		JFrame window = new JFrame("Trade Utils");
		window.setPreferredSize (new Dimension(500,600));
		try {
			int[] sizes = new int [] {16,32,48,64,128,256};
			List <Image> images = new ArrayList <Image>();
			for (int i:sizes){
				images.add (ImageIO.read(TradeUtil.class.getResource("images/icons/icon-" + i+ ".png")));
			}
			window.setIconImages(images);
		} catch (IOException e1) {
			//e1.printStackTrace();
		}

		final DataController controller = new DataController();
		//
		JPanel container = new JPanel();
		container.setLayout (new BorderLayout());
		container.setPreferredSize (window.getPreferredSize());
		container.setBackground (Color.LIGHT_GRAY);

		//
		JPanel topControls = new JPanel();
		topControls.setOpaque(false);
		topControls.add(new JLabel ("Refresh rate (mins): "));
		JComboBox<String> refreshRate = new JComboBox<> (new String [] {"1","2","3","4","5","6","7","8","9","10","never"});
		refreshRate.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox<String>)e.getSource();
				String refreshRate = (String)cb.getSelectedItem();
				if ("never".equals(refreshRate)){
					controller.stop();
				}
				else {
					int newRefreshRate = controller.getRefreshRate();;
					try{
						newRefreshRate = Integer.parseInt(refreshRate);
					}
					catch (NumberFormatException ne){
						ne.printStackTrace();
					}
					if (newRefreshRate != controller.getRefreshRate()){
						controller.updateRefreshRate(newRefreshRate);
					}
				}
			}
		});
		topControls.add (refreshRate);
		JButton refreshButton = new JButton ("Refresh");
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.refresh();

			}
		});
		topControls.add (refreshButton);
		container.add(topControls, BorderLayout.PAGE_START);

		//
		JPanel tradeOfferContainer = new JPanel();
		tradeOfferContainer.setOpaque (false);
		tradeOfferContainer.setLayout (new GridLayout (1,2));

		//
		JPanel recvdTradeOfferContainer = new JPanel (new BorderLayout());
		recvdTradeOfferContainer.setOpaque (false);
		JLabel recvdText = genSizedLabel("offers received", 20);
		recvdTradeOfferContainer.add(recvdText, BorderLayout.PAGE_END);
		JLabel recvdTradeOfferNum = genSizedLabel("-", 200);;
		recvdTradeOfferContainer.add(recvdTradeOfferNum);
		recvdTradeOfferContainer.setBorder(BorderFactory.createMatteBorder(
				1, 0, 1, 1, Color.BLACK));
		tradeOfferContainer.add(recvdTradeOfferContainer,BorderLayout.CENTER);

		//
		JPanel sentTradeOfferContainer = new JPanel(new BorderLayout());
		sentTradeOfferContainer.setOpaque (false);
		JLabel sentTxt = genSizedLabel("offers sent", 20);
		sentTradeOfferContainer.add(sentTxt, BorderLayout.PAGE_END);

		JLabel sentTradeOfferNum = genSizedLabel("-", 200);
		sentTradeOfferNum.setOpaque(false);
		sentTradeOfferContainer.add(sentTradeOfferNum);
		sentTradeOfferContainer.setBorder(BorderFactory.createMatteBorder(
				1, 1, 1, 0, Color.BLACK));
		tradeOfferContainer.add(sentTradeOfferContainer,BorderLayout.CENTER);

		//
		container.add (tradeOfferContainer,BorderLayout.CENTER);

		//
		JPanel gauges = new JPanel (new GridLayout(0,2));
		gauges.setOpaque (false);
		gauges.add(genCenteredText("Steam Friends"));
		JLabel friendsStatus = genSizedLabel ("-", 15);
		gauges.add(friendsStatus);
		gauges.add(genCenteredText("TF2 Items API"));
		JLabel itemStatus = genSizedLabel ("-", 15);
		gauges.add(itemStatus);
		JPanel placeHolder = new JPanel();
		placeHolder.setBorder(BorderFactory.createMatteBorder(
				1, 0, 0, 0, Color.BLACK));
		placeHolder.setOpaque(false);
		gauges.add(placeHolder);
		JLabel updatedTime = new JLabel ("Attempted refresh at --:--",JLabel.RIGHT);
		updatedTime.setBorder(BorderFactory.createMatteBorder(
				1, 0, 0, 0, Color.BLACK));
		gauges.add(updatedTime);
		container.add(gauges,BorderLayout.PAGE_END);

		window.add(container);

		window.pack();
		controller.setLabels(recvdTradeOfferNum, 
				sentTradeOfferNum, 
				updatedTime, 
				friendsStatus, 
				itemStatus,createTrayIcon(window));
		controller.start();

		window.setVisible(!(args.length > 0 && args[0].equals("hidden")));
	}

	public static JLabel genSizedLabel (String text, float fontSize){
		JLabel label = new JLabel (text,JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(fontSize));
		return label;
	}
	public static JPanel genCenteredText (String text){
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		JLabel j = genSizedLabel (text, 15);
		panel.add(j);
		j.setHorizontalAlignment(SwingConstants.RIGHT);
		return panel;
	}

	private static TrayIcon createTrayIcon(final JFrame window) {
		final Runnable toFront = new Runnable(){
			@Override
			public void run(){
				window.toFront();
				window.repaint();
			}
		};
		//Check the SystemTray support
		if (!SystemTray.isSupported()) {
			//System.out.println("SystemTray is not supported");
			return null;
		}
		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon =
				new TrayIcon(createOfferImages(Color.DARK_GRAY ,-1));
		trayIcon.setImageAutoSize(true);
		final SystemTray tray = SystemTray.getSystemTray();
		trayIcon.setToolTip("TradeUtils");

		// Create a popup menu components
		MenuItem aboutItem = new MenuItem("About");
		MenuItem openItem = new MenuItem("Open");
		MenuItem exitItem = new MenuItem("Exit");

		//Add components to popup menu
		popup.add(aboutItem);
		popup.addSeparator();
		popup.add(openItem);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			//System.out.println("TrayIcon could not be added.");
			return null;
		}

		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.setVisible(true);
				java.awt.EventQueue.invokeLater(toFront);
			}
		});

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"Written by Andrei Ciubotariu.\n" +
								"This program uses the Steam web API and the SteamGauges API.\n" +
								"JSON parsing implemented using GSON. " +
								"Data fetching implemented using Apache HTTPClient.\n" +
								"Also uses the Jasypt library.","About", JOptionPane.PLAIN_MESSAGE);
			}
		});

		openItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				window.setVisible(true);
				java.awt.EventQueue.invokeLater(toFront);
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tray.remove(trayIcon);
				System.exit(0);
			}
		});

		return trayIcon;
	}

	private static Image createOfferImages (Color color, int count){
		for (int x = 0; x < images.length;x++){
			try {
				URL url = TradeUtil.class.getResource("images/trade_offers/" + x + ".png");
				images [x] = ImageIO.read(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		BufferedImage b = new BufferedImage (300,300, BufferedImage.TYPE_INT_ARGB);
		b.createGraphics();
		changeImage(color,b);
		return changeImage (count, b);
	}

	public static Image changeImage (int number, Image image){
		Graphics g = image.getGraphics();
		if (number >= 0){
			Image toDraw = number < images.length-1 ? images [number] : images [images.length-1];
			g.drawImage(toDraw,0,0,null);
		}
		return image;
	}
	public static Image changeImage (Color color, Image image){
		Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, 300, 300);
		return image;
	}
}
