package com.uninettuno.thesis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingActivity extends Activity {
	SharedPreferencesModel controllerSP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		controllerSP = new SharedPreferencesModel(this);
		if(!controllerSP.readIpServer().equals("0.0.0.0")) {
			EditText ipServerEdit = (EditText)findViewById(R.id.ip_server_text);
			ipServerEdit.setText(controllerSP.readIpServer(), TextView.BufferType.EDITABLE);
		}
		
		EditText portEdit = (EditText)findViewById(R.id.port_text);	
		portEdit.setText(controllerSP.readPort()+"",TextView.BufferType.EDITABLE);
	}
	
	 public void savePort(View view) {
		 EditText portEdit = (EditText)findViewById(R.id.port_text);
		 controllerSP = new SharedPreferencesModel(this);
		 String port = portEdit.getText().toString();
		 if(port != null && !port.equals("0") && !port.equals("")) {
			 int portaa = Integer.parseInt(port);	
			 controllerSP.write(portaa);
			 showMessage("OK","Salvataggio avvenuto con successo");
		 }
		 else
			 showMessage("Errore", "Inserire un valore corretto.");
	 }
	 

	public void saveIpServer(View view) {
		 EditText ipServerEdit = (EditText)findViewById(R.id.ip_server_text);
		 SharedPreferencesModel controllerSP = new SharedPreferencesModel(this);
		 String ipServer = ipServerEdit.getText().toString();
		 ipServer = ipServer.trim();
		 if(ipServer != null && !ipServer.equals("") && !ipServer.contains("'") && !ipServer.contains("\"")) {
			 controllerSP.write(ipServer);
			 showMessage("OK","Salvataggio avvenuto con successo");
		 }
		 else
			 showMessage("Errore", "Inserire un valore corretto.");
	 }

	 public void showMessage(String title, String message){
		 new AlertDialog.Builder(this)
		    .setTitle(title)
		    .setMessage(message)
		    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue
		        }
		     }) 
		    .setIcon(R.drawable.ic_action_warning)
		     .show();
	 }
}
