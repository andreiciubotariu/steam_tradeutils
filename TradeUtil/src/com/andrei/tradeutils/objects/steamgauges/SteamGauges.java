package com.andrei.tradeutils.objects.steamgauges;

import java.awt.Color;

import com.andrei.tradeutils.DataController;
/**
 * 
 * @author Andrei Ciubotariu
 *
 */
public class SteamGauges {
	public int ISteamClient;
	public int ISteamFriends;
	public int ISteamUser;
	public int IEconItems_440;
	public int IEconItems_730;
	public int ISteamGameCoorindator_440;
	public int ISteamGameCoorindator_570;

	public static class Status{
		public final int UNKNOWN = -1;
		public final int ONLINE = 0;
		public final int DOWN = 1;
		public final int ERR_500 = 2;
		public final int EMPTY_RESPONSE = 3;
		public final int ERR_404 = 4;
		public final int TIMEOUT = 5;
		public final int ERR_OTHER = 6;

		public int status;
		public Color color;
		public String message;

		public Status(int status){
			this.status = status;
			switch (status){
			case UNKNOWN:
				color = Color.BLACK;
				message = "Unknown";
				break;
			case ONLINE:
				color = DataController.DARK_GREEN;
				message = "Online";
				break;
			default:
				color = Color.RED;
				break;
			}
			switch (status){
			case DOWN:
				message = "Down";
				break;
			case ERR_500:
				message = "Error 500";
				break;
			case EMPTY_RESPONSE:
				message = "Empty response";
				break;
			case ERR_404:
				message = "404 not found";
				break;
			case TIMEOUT:
				message = "Timeout";
				break;
			case ERR_OTHER:
				message = "Other error";
				break;
			}
		}
	}

}
