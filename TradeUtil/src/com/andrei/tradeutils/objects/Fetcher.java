package com.andrei.tradeutils.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
/**
 * 
 * @author Andrei Ciubotariu
 *
 */
public class Fetcher {

	public static <T extends Object> T  fetch(String url, Class<T> clazz){
		T obj = null;
		InputStream is = null;
		try{

			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			try {
				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {	    	
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				Gson gson = new Gson();
				String data = sb.toString();
				obj = gson.fromJson(data, clazz);
				//System.out.println (p.response.trade_offers_received.get(0).tradeofferid);
			} catch(Exception e) {
				System.out.println ("Error caught " + e.getMessage());
				//e.printStackTrace();
			}
			return obj;
		}
		finally {
			if (is != null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
