package com.uninettuno.thesis;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPreferencesModel {
	public Context context;
	public static final String  KEYSP = "com.uninettuno.thesis";
	SharedPreferences sharPref ;
	
	public SharedPreferencesModel(Context context){
		this.context = context;
		sharPref = context.getSharedPreferences(KEYSP, Context.MODE_PRIVATE);
	}
	
	public void writeDataRefreshAll(long dateRefreshAll){
		//recupero il contenuto delle shared preferences
		String sharPrefStr = sharPref.getString(KEYSP, "vuota");
		JSONObject sharPrefObj = new JSONObject();
		try {
			if (!sharPrefStr.equals("vuota")) {
				
					sharPrefObj = new JSONObject(sharPrefStr);
				
			} else {
				sharPrefObj = new JSONObject();
			}
			sharPrefObj.put("dateRefreshAll", dateRefreshAll);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Log.w("SharedPreferencesController","dopo daterefresh :"+sharPrefObj.toString());
		//ricopio il contenuto nelle sharedpreference
		sharPref.edit().putString(KEYSP, sharPrefObj.toString()).apply();
	}
	
	//scrivo l'ip del server a cui puntare nelle shared preferences
	public void write(String ipServer){
		//recupero il contenuto delle shared preferences
		String sharPrefStr = sharPref.getString(KEYSP, "vuota");
		JSONObject sharPrefObj = new JSONObject();
		try {
			if (!sharPrefStr.equals("vuota")) {
				
					sharPrefObj = new JSONObject(sharPrefStr);	
			} else {
				sharPrefObj = new JSONObject();
			}
			sharPrefObj.put("server", ipServer);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//ricopio il contenuto nelle sharedpreference
		sharPref.edit().putString(KEYSP, sharPrefObj.toString()).apply();
	}
	
	public void write(int porta){
		sharPref.edit().putInt(KEYSP+"Int", porta).apply();
	}
	
	@SuppressLint("NewApi")
	public void  write(String typeResponse , JSONArray hosts){
		String sharPrefStr = sharPref.getString(KEYSP, "vuota");
		JSONObject sharPrefObj = new JSONObject();
		boolean flag = false;
		try {
			if (!sharPrefStr.equals("vuota")) {
					sharPrefObj = new JSONObject(sharPrefStr);
			} else {
				sharPrefObj = new JSONObject();
				flag = true;
			}
			if (typeResponse.equals("single") && flag==false) {
				JSONArray hostSP = sharPrefObj.getJSONArray("host");
				String ipToAdd = hosts.getJSONObject(0).getString("ip");
				for(int i=0;i<hostSP.length();i++){
					//Log.i("WRITE","get(0): "+hostSP.getJSONObject(i).getString("ip"));
					if(ipToAdd.equalsIgnoreCase(hostSP.getJSONObject(i).getString("ip"))) {
						hostSP.put(i,(JSONObject)hosts.getJSONObject(0));
					}
				}
				sharPrefObj.put("host", hostSP);
			} else
				sharPrefObj.put("host", hosts);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//ricopio il contenuto nelle sharedpreference
		sharPref.edit().putString(KEYSP, sharPrefObj.toString()).apply();
	}
	
	//Setta a 0 lo status di una macchina(dopo azione off)
	public void updateStatus(String ipHost){
		JSONObject host = readHost(ipHost);
		try {
			host.put("status", 0);
			write("single", new JSONArray().put(host));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//Setta a 0 il numero di utenti connessi(dopo azione kill_sessions)
	public void updateUser(String ipHost){
		JSONObject host = readHost(ipHost);
		try {
			JSONObject healthHost = host.getJSONObject("health");
			healthHost.put("users", "0");
			host.put("health", healthHost);
			write("single", new JSONArray().put(host));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//legge il valore del daterefresh, tona 0 e lo SP è vuoto
	public long readDateRefresh(String ipHost){
		String sharPrefStr = sharPref.getString(KEYSP, "vuota");
		if(!sharPrefStr.equals("vuota")){
			JSONObject sharPrefObj = new JSONObject();
			try {
				sharPrefObj = new JSONObject(sharPrefStr);
				if (ipHost.equalsIgnoreCase("all!")){
					if(sharPrefObj.has("dateRefreshAll")) {
						return sharPrefObj.getLong("dateRefreshAll");
					}
				}else {
					JSONObject host = readHost(ipHost);
					if(host.has("dateRefresh")) {
						return host.getLong("dateRefresh");
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
		
	}
	
	//leggo il valore dell'ip del server dalle shared preferences
	public String readIpServer() {
		//recupero il contenuto delle shared preferences
		String sharPrefStr = sharPref.getString(KEYSP, "vuota");
		String result = "errore";
		JSONObject sharPrefObj = new JSONObject();
		try {
			if (!sharPrefStr.equals("vuota")) {
					sharPrefObj = new JSONObject(sharPrefStr);	
			} else {
				return "0.0.0.0";
			}
			result = sharPrefObj.getString("server");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//ricopio il contenuto nelle sharedpreference
		return result;
	}
	
	public int readPort() {
		return sharPref.getInt(KEYSP+"Int",80);
	}
	
	//ritorna la lista degli ip monitorati, null se la lista è vuota
	public ArrayList<String> readListHosts() {
		String sharPrefStr = sharPref.getString(KEYSP, "vuota");
		JSONArray sharPrefObj;
		ArrayList<String> listHosts = new ArrayList<String>();
		try {
			if (!sharPrefStr.equals("vuota")) {
				sharPrefObj = new JSONArray(new JSONObject(sharPrefStr).get("host").toString());
				for (int i=0; i<sharPrefObj.length();i++) {
					JSONObject hostSingle = new JSONObject(sharPrefObj.get(i).toString());
					listHosts.add(hostSingle.getString("ip"));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listHosts;	
	}
	
	//ritorna l'oggetto host per l'ip passat, null se non viene trovato
	public JSONObject readHost(String ip) {
		String sharPrefStr = sharPref.getString(KEYSP, "vuota");
		JSONArray sharPrefObj;
		JSONObject host = new JSONObject();
		try {
			if (!sharPrefStr.equals("vuota")) {
				sharPrefObj = new JSONArray(new JSONObject(sharPrefStr).get("host").toString());
				for (int i=0; i<sharPrefObj.length();i++) {
					JSONObject hostSingle = new JSONObject(sharPrefObj.get(i).toString());
					if (ip.equalsIgnoreCase(hostSingle.getString("ip"))) {
						host = hostSingle;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return host;
	}
	
	//Questo metodo recupera l'ora dell'ultimo aggiornamento "all", 0 se non trova nulla
	public long readDateRefreshAll() {
		String sharPrefStr = sharPref.getString(KEYSP, "vuota");
		JSONObject sharPrefObj = new JSONObject();
		long result = 0;
		try {
			if (!sharPrefStr.equals("vuota")) {
				sharPrefObj = new JSONObject(sharPrefStr);
				result = Long.parseLong(sharPrefObj.get("dateRefreshAll").toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	} 
	
	public String getAll(){
		//recupero il contenuto delle shared preferences
		return sharPref.getString(KEYSP, "vuota") + sharPref.getInt(KEYSP+"Int", 0);
	}
}
