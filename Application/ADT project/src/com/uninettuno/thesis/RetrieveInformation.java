package com.uninettuno.thesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

class RetrieveInformation extends AsyncTask<String, Void, String> {
	
	private Context context;
	private MainActivity mainActivity;
	private HostFragment hostFragment;
	private ProgressDialog progDailog;
	
	public RetrieveInformation (Context context, MainActivity mainActivity) {
		this.context = context;
		this.mainActivity = mainActivity;
	}
	
	public RetrieveInformation (Context context, MainActivity mainActivity, HostFragment hostFragment) {
		this.context = context;
		this.mainActivity = mainActivity;
		this.hostFragment = hostFragment;
	}
	
	public void setDateAll() {
		//setto l'ora dell'aggionramento
		TextView oraAggiornamentoAll = (TextView)mainActivity.findViewById(R.id.oraAggiornamento);
		Date dataAll = new Date(new SharedPreferencesModel(context).readDateRefreshAll()*1000);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dataAll);
		String datePrint = "Aggiornato alle:"+ cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+" del "+cal.get(Calendar.DAY_OF_MONTH)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.YEAR);
    	oraAggiornamentoAll.setText(datePrint, TextView.BufferType.EDITABLE);
	}
	
	public void showAlertDialog() {
		//Show an Alert Dialog
		new AlertDialog.Builder(mainActivity)
	    .setTitle("Richiesta fallita")
	    .setMessage("E' stato riscontrato un problema durante la richiesta!")
	    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue
	        }
	     }) 
	    .setIcon(R.drawable.ic_action_warning)
	     .show();
	}
	
	public ArrayList<NavDrawerItem> updateDrawerList() {
		ArrayList<String> hostIps = new SharedPreferencesModel(context).readListHosts();
		mainActivity.navDrawerItems = new ArrayList<NavDrawerItem>();
		for(int i=0;i<hostIps.size();i++){
			mainActivity.navDrawerItems.add(new NavDrawerItem(hostIps.get(i)));
		}
        mainActivity.adapter.setItems(mainActivity.navDrawerItems);
        return mainActivity.navDrawerItems;
	}
	
	public void updateExpandList(JSONObject host){
		Log.w("updateExpandList","Update Expande LIST");
		hostFragment.listAdapter.setContent(host);
	}

	//Questa funzione setta ad adesso l'attributo dateRefresh di tutti gli Host ricevuti.
	public JSONArray setDateRefresh(JSONArray hosts) {
		//get current time in second
		long date = new Date().getTime()/1000;
		JSONArray hostsUpdated = new JSONArray();
		try {
			for (int i=0; i<hosts.length();i++){
				JSONObject hostSingle = new JSONObject(hosts.get(i).toString());
				hostSingle.put("dateRefresh", date);
				hostsUpdated.put(hostSingle);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hostsUpdated;
	}
	
	//Show Loading
	protected void onPreExecute() {
		super.onPreExecute();
        progDailog = new ProgressDialog(mainActivity);
        progDailog.setMessage("Loading...");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(true);
        progDailog.show();
	}
	
    protected String doInBackground(String... urls) {
    	// TODO Auto-generated method stub    	
    	OkHttp example = new OkHttp();
    	Response result = null;
		
			SharedPreferencesModel controllerSP = new SharedPreferencesModel(context);
			//CACHE di 1 minuto
			if ( (new Date().getTime()/1000 - controllerSP.readDateRefresh(urls[1]) > 60) ) {
				try {		
					String json = "{ \"operation\":\"getHost\", \"ip\":\""+urls[1]+"\" }";
					//urls[0]=url, urls[1]=ip
					result = example.post(urls[0],json);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//probabile timeout richiesta
					return "Error on response";
				}
				if (result.isSuccessful()) {
					try {
						return result.body().string();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		} else {
			try {
		        Thread.sleep(1500);         
		    } catch (InterruptedException e) {
		       e.printStackTrace();
		    }
			return "cached";
		}
			
			
		return "Error on response";
       
    }

    protected void onPostExecute(String result) {
    	//Stop Loading
    	progDailog.dismiss();
    	boolean flag = true;
    	if (result.equalsIgnoreCase("Error on response") ){
    		showAlertDialog();
			flag = false;
		}else if (result.equalsIgnoreCase("cached") ){
			Log.w("RetrieveInformation","CACHE LIKE A BOSS!");
			flag = false;
		} else {
    		//Log.w("RetrieveInformation",result);
    	}
    	if(flag){
	    	JSONObject resultJson = null;
	    	SharedPreferencesModel controllerSP = new SharedPreferencesModel(context);
	    	//Log.w("RetrieveInformationFIRST","gett all:  "+controllerSP.getAll());
	    	JSONArray hosts = new JSONArray();
	    	String typeResponse = "";
	    	try {
	    		resultJson = new JSONObject(result);
	    		typeResponse = resultJson.getString("typeHost");
	    		if(resultJson.get("status").toString().equalsIgnoreCase("200")){
	    			hosts = resultJson.getJSONArray("host");
					//setto la data agli host appena ricevuti
	    			hosts = this.setDateRefresh(hosts);
					//Salvo l'host nello shared preferences
					
					//Log.w("RetrieveInformation","dopo creazione controllerSP");
					controllerSP.write(typeResponse, hosts);
					
					if (resultJson.getString("typeHost").equals("all"))
						controllerSP.writeDataRefreshAll(new Date().getTime()/1000);
					
					if (typeResponse.equals("all")) {
		        		this.updateDrawerList();
		        		this.setDateAll();
					}else
		    			this.updateExpandList(hosts.getJSONObject(0));
	    		}else {
	    			showAlertDialog();
	    		}
	    		
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	//Log.w("RetrieveInformationAFTER","gett all:  "+controllerSP.getAll());
    	}
    	
    	
    }
    
    
    public class OkHttp {
    	public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    	OkHttpClient client = new OkHttpClient();

    	Response post(String url, String json) throws IOException {
    		client.setConnectTimeout(15, TimeUnit.SECONDS); // connect timeout
    		client.setReadTimeout(15, TimeUnit.SECONDS);    // socket timeout		
	  	    RequestBody body = RequestBody.create(JSON, json);
	  	    Request request = new Request.Builder()
	  	        .url(url)
	  	        .post(body)
	  	        .build();
	  	    Response response = client.newCall(request).execute();
	  	    return response;
  	  }

    }
}