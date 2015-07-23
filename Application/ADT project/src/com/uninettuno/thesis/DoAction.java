package com.uninettuno.thesis;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

class DoAction extends AsyncTask<String, Void, String> {
		
		private Context context;
		private MainActivity mainActivity;
		private HostFragment hostFragment;
		private ProgressDialog progDailog;
		private String ipHost;
		private String typeAction;
		
		public DoAction (Context context, MainActivity mainActivity, HostFragment hostFragment,String ipHost) {
			this.context = context;
			this.mainActivity = mainActivity;
			this.hostFragment = hostFragment;
			this.ipHost = ipHost;
		}
		
		public void updateExpandList(JSONObject host){
			Log.w("updateExpandList","Update Expande LIST");
			hostFragment.listAdapter.setContent(host);
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
		
	    protected String doInBackground(String... urls) {
	    	// TODO Auto-generated method stub    	
	    	OkHttp example = new OkHttp();
	    	Response result = null;
			try {
				//urls[0]=url, urls[1]=typeAction
				this.typeAction = urls[1];
				String json;
				if(this.typeAction.equalsIgnoreCase("off") || this.typeAction.equalsIgnoreCase("kill_sessions"))
					json = "{ \"operation\":\"action\", \"ip\":\""+ipHost+"\",\"command\":\""+typeAction+"\" }";
				else 
					return "Error on response";
				result = example.post(urls[0],json);
				Log.w("RetrieveInformationFIRST","gett all:  "+result.toString());
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
			return "Error on response";
	       
	    }

	    protected void onPostExecute(String result) {
	    	//Stop Loading
	    	progDailog.dismiss();
	    	boolean flag = true;
	    	if (result.equalsIgnoreCase("Error on response") ){
	    		showAlertDialog();
				flag = false;
			}else {
	    		Log.w("RetrieveInformation",result);
	    	}
	    	if(flag){
		    	JSONObject resultJson = new JSONObject();
		    	SharedPreferencesModel controllerSP = new SharedPreferencesModel(context);
		    	Log.w("RetrieveInformationFIRST","gett all:  "+controllerSP.getAll());
		    	try {
		    		resultJson = new JSONObject(result);
		    		Log.i("resultJSON","ResultJSON:  "+resultJson.toString());
		    		if(resultJson.get("status").toString().equalsIgnoreCase("200")){	
		    			if(typeAction.equalsIgnoreCase("off"))
		    				controllerSP.updateStatus(ipHost);	
		    			else if (typeAction.equalsIgnoreCase("kill_sessions")) {
		    				controllerSP.updateUser(ipHost);
		    			} else {
		    				Log.w("DoAction","typeAction Value"+typeAction);
		    			}
		    			this.updateExpandList(controllerSP.readHost(ipHost));
		    		}else {
		    			showAlertDialog();
		    		}
		    		
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	Log.w("RetrieveInformationAFTER","gett all:  "+controllerSP.getAll());
	    	}
	    }
	        
	    public class OkHttp {
	    	public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	    	OkHttpClient client = new OkHttpClient();

	    	Response post(String url, String json) throws IOException {
	    		client.setConnectTimeout(20, TimeUnit.SECONDS); // connect timeout
	    		client.setReadTimeout(20, TimeUnit.SECONDS);    // socket timeout		
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
