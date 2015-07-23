package com.uninettuno.thesis;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;
    protected MainActivity mainActivity;
    protected View rootView;
 
    public ExpandableListAdapter(Context context, List<String> listDataHeader,
            HashMap<String, List<String>> listChildData,View rootView,MainActivity mainActivity) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.mainActivity = mainActivity;
        this.rootView = rootView;
    }
 
    public void setDataHost(String ipHost) {
    	TextView oraAggiornamentoHost = (TextView)rootView.findViewById(R.id.oraAggiornamentoHost);
    	Date dataHost = new Date(new SharedPreferencesModel(mainActivity).readDateRefresh(ipHost)*1000);
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(dataHost);
		String datePrint = "Aggiornato alle:"+ cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+" del "+cal.get(Calendar.DAY_OF_MONTH)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.YEAR);
    	oraAggiornamentoHost.setText(datePrint, TextView.BufferType.EDITABLE);
    }
    
    //1=verde , 0=rosso
    public void setColorHeader(int status){
    	TextView oraAggiornamentoHost = (TextView)rootView.findViewById(R.id.oraAggiornamentoHost);
    	if (status == 1) 
        	oraAggiornamentoHost.setBackgroundColor(0xff00ef78);
    	else 
    		oraAggiornamentoHost.setBackgroundColor(0xfff00000);
    }
    
    public void setContent(JSONObject host){
    	Log.w("SETCONTENT","HOST: "+host.toString());
    	String ipHost = "";
    	int status = 1;
    	List<String> newListDataHeader = new ArrayList<String>();
    	HashMap<String, List<String>> newListDataChild = new HashMap<String, List<String>>();
    	
    	newListDataHeader.add("Refresh");
    	ArrayList<String> refresh = new ArrayList<String>();
    	newListDataChild.put(newListDataHeader.get(0), refresh);
    	if(!host.toString().equals("{}")) {
    		
			newListDataHeader.add("Informazioni");
    		ArrayList<String> listInformation = new ArrayList<String>();
    		
    		try {
    			ipHost = host.getString("ip");
    			//Setto le INFORMAZIONI
		    	JSONObject hostInformation = host.getJSONObject("information");
		    	listInformation.add("Hostname : "+hostInformation.getString("hostname"));
		    	listInformation.add("Tipo macchina : "+hostInformation.getString("type"));
		    	listInformation.add("Sistema operativo : "+hostInformation.getString("OS").replace('_', ' '));
		    	listInformation.add("CPU : "+hostInformation.getString("CPU").replace('_', ' '));
		    	listInformation.add("Memoria : "+hostInformation.getString("memory"));
		    	listInformation.add("Dischi : "+hostInformation.getString("disk").replace('_', ' '));
		    	newListDataChild.put(newListDataHeader.get(1), listInformation);
		    	
		    	status = (Integer)host.get("status");
		    	//Mostro "Stato Salute" e "Azioni" solo per gli host accesi
		    	if (status == 1) {
		    		newListDataHeader.add("Stato Salute");
					newListDataHeader.add("Azioni");
					ArrayList<String> listHealth = new ArrayList<String>();
					ArrayList<String> listActions = new ArrayList<String>();
					
					//Setto lo STATO SALUTE
			    	JSONObject hostHealth = host.getJSONObject("health");
			    	String partitions = hostHealth.getString("partitions").replace('_', ' ');
			    	listHealth.add("Partizioni : "+partitions.substring(0, partitions.length()-1));
			    	listHealth.add("Memoria : "+hostHealth.getString("memory"));
			    	listHealth.add("CPU : "+hostHealth.getString("CPU"));
			    	listHealth.add("Utenti connessi : "+hostHealth.getString("users"));
			    	newListDataChild.put(newListDataHeader.get(2), listHealth);
			    	
			    	//setto le AZIONI
			    	listActions.add("Spegni macchina");
			    	listActions.add("Killa sessioni utente"); 
			    	newListDataChild.put(newListDataHeader.get(3), listActions);
		    	}
    		} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    		
			this._listDataHeader = newListDataHeader;
			this._listDataChild = newListDataChild;
			//Log.w("ExpadableviewAdap","ListDataHeader: "+newListDataHeader.toString()+"ListDataChild: "+newListDataChild.toString()+" ERRORE:"+this._listDataChild.get(this._listDataHeader.get(0)).size());
			notifyDataSetChanged(); 
			setDataHost(ipHost);
			setColorHeader(status);
    	}
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
        final String childText = (String) getChild(groupPosition, childPosition);
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
 
        txtListChild.setText(childText);
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
 
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
 
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
    	if (groupPosition == 3)
    		return true;
    	else
    		return false;  
    }
}