package com.andrei.tradeutils;

import java.awt.Color;
import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import com.andrei.tradeutils.objects.Fetcher;
import com.andrei.tradeutils.objects.steam.SteamResult;
import com.andrei.tradeutils.objects.steam.TradeOffer;
import com.andrei.tradeutils.objects.steamgauges.SteamGauges;
import com.andrei.tradeutils.objects.steamgauges.SteamGauges.Status;
/**
 * 
 * @author Andrei Ciubotariu
 *
 */
public class DataController {
	private static final String URL_STEAM_API_1 = "https://api.steampowered.com/IEconService/GetTradeOffers/v1/?key=";
	private static final String URL_STEAM_API_2	= "&get_received_offers=1&get_sent_offers=1&active_only=1&format=json";
	private static final String URL_STEAM_GAUGES = "http://steamgaug.es/api/";
	public static final Color DARK_GREEN =  Color.GREEN.darker().darker();
	private JLabel recvd;
	private JLabel sent;
	private JLabel updatedTime;
	private JLabel steamFriendsStatus;
	private JLabel tf2ItemStatus;

	private SteamGauges gauges;
	private SteamResult result;

	private int numRecvdOffers;
	private int numSentOffers;

	private long updatedTimeMillis;

	private int refreshRate = 1;

	final static int MIN = 1000 * 60;

	private DataFetcher fetcher;

	private Timer timer;

	private Date date = new Date(updatedTimeMillis);
	private DateFormat timeFormat = new SimpleDateFormat ("HH:mm:ss");

	private TrayIcon trayIcon; //TrayIcon class
	private Image trayImage; //Image

	private static abstract class Listener {
		public abstract void run (int count);
	}
	private Listener notificationRunnable = new Listener (){
		public void run(int count){
			if (trayIcon != null){
				trayIcon.displayMessage("New trade offers", "There are " + count + " new offer(s)", TrayIcon.MessageType.INFO);
			}
		}
	};

	private Listener offerTrayIconChanger = new Listener (){
		public void run(int count){
			if (trayImage != null){
				TradeUtil.changeImage(count,trayIcon.getImage());
			}
			if (trayIcon != null && trayImage != null){
				trayIcon.setImage (trayImage);
			}
		}
	};

	public DataController(){
		timer = new Timer(refreshRate*MIN, new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshDataIfPossible();
			}
		});
	}

	public void setLabels(JLabel rec, JLabel s, JLabel time, JLabel friends, JLabel items, TrayIcon icon){
		recvd = rec;
		sent = s;
		updatedTime = time;
		steamFriendsStatus = friends;
		tf2ItemStatus = items;
		trayIcon = icon;
	}

	public void updateRefreshRate (int mins){
		refreshRate = mins;
		if (refreshRate > 0){
			timer.setDelay(refreshRate*MIN);
			timer.setInitialDelay(refreshRate*MIN);
			refresh();
			timer.restart();
		}
		else {
			refreshRate = -1;
			timer.stop();
		}
	}

	public void refresh(){
		refreshDataIfPossible();
		timer.restart();
	}

	private void refreshDataIfPossible(){
		System.out.println ("Refreshing");
		if (fetcher == null || fetcher.isDone() || fetcher.isCancelled()){
			fetcher = new DataFetcher();
			fetcher.execute();
		}
	}

	public void start(){
		refreshDataIfPossible();
		timer.start();
	}

	public void stop(){
		timer.stop();
		refreshRate = -1;
	}

	public int getRefreshRate(){
		return refreshRate;
	}

	private class DataFetcher extends SwingWorker<Void, Void> {
		public DataFetcher() {

		}

		@Override
		public Void doInBackground() {
			updatedTimeMillis = System.currentTimeMillis();
			try{
				result = Fetcher.fetch(URL_STEAM_API_1+TradeUtil.key+URL_STEAM_API_2, SteamResult.class);
			}
			catch (Exception e){
				result = null;
			}
			try{
				gauges = Fetcher.fetch(URL_STEAM_GAUGES,SteamGauges.class);
			}
			catch (Exception e){
				gauges = null;
			}
			return null;
		}

		@Override
		public void done() {
			trayImage = trayIcon != null ? trayIcon.getImage() : null;
			if (gauges != null){
				Status friends = new Status (gauges.ISteamFriends);
				steamFriendsStatus.setText(friends.message);
				steamFriendsStatus.setForeground(friends.color);

				Status items = new Status (gauges.IEconItems_440);
				tf2ItemStatus.setText(items.message);
				tf2ItemStatus.setForeground(items.color);

				if (trayImage != null){
					TradeUtil.changeImage(items.color,trayIcon.getImage());
				}
			}
			else {
				steamFriendsStatus.setForeground(Color.BLACK);
				steamFriendsStatus.setText("-");
				tf2ItemStatus.setForeground(Color.BLACK);
				tf2ItemStatus.setText("-");
				if (trayImage != null){
					TradeUtil.changeImage(Color.GRAY,trayIcon.getImage());
				}
			}
			if (result != null){
				numRecvdOffers = checkOffers(result.response.trade_offers_received, numRecvdOffers, recvd, notificationRunnable,offerTrayIconChanger);
				numSentOffers = checkOffers(result.response.trade_offers_sent, numSentOffers, sent, null,null);
			}
			else {
				recvd.setForeground(Color.BLACK);
				recvd.setText("-");
				sent.setForeground(Color.BLACK);
				sent.setText("-");
				if (trayIcon != null && trayImage != null){
					trayIcon.setImage(trayImage);
				}
			}

			if (gauges != null || result != null){
				date.setTime(updatedTimeMillis);
				String text = "Last refresh: " + timeFormat.format(date);
				if (result == null && gauges != null){
					text += " (gauges only)";
				}
				else if (gauges == null && result != null){
					text += " (offers only)";
				}
				updatedTime.setText(text);
			}

		}

	}

	public static int checkOffers (List <TradeOffer> offers, int currentNum, JLabel output, Listener greaterThanlistener,
			Listener trayListener){
		if (offers == null){
			currentNum = 0;
		}
		else {
			int fullyActiveOffers = 0;
			for (TradeOffer t:offers){
				if (t.trade_offer_state == TradeOffer.State.ACTIVE || t.trade_offer_state == TradeOffer.State.COUNTERED){
					fullyActiveOffers++;
				}
			}
			if (fullyActiveOffers > currentNum && greaterThanlistener != null){
				int difference = fullyActiveOffers - currentNum;
				greaterThanlistener.run(difference);
			}
			currentNum = fullyActiveOffers;
		}
		if (trayListener != null){
			trayListener.run(currentNum);
		}
		output.setText(String.valueOf(currentNum));
		output.setForeground (currentNum == 0 ? Color.BLACK : DARK_GREEN);
		return currentNum;
	}
}
