package com.andrei.tradeutils.objects.steam;
/**
 * 
 * @author Andrei Ciubotariu
 *
 */
public class TradeOffer {
	public static class State{
		public final static int INVALID = 1;
		public final static int ACTIVE = 2;
		public final static int ACCEPTED = 3;
		public final static int COUNTERED = 4;
		public final static int EXPIRED = 5;
		public final static int CANCELED = 6;
		public final static int DECLINED = 7;
		public final static int INVALID_ITEMS = 8;
	}
	
	public String tradeofferid;
	public String accountid_other;
	public String message;
	public String expiration_time;
	public int trade_offer_state;
	public boolean is_our_offer;
	public String time_created;
	public String time_updated;
}
