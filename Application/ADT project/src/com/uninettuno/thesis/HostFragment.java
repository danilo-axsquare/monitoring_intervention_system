package com.uninettuno.thesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
 
@SuppressLint("ValidFragment")
public class HostFragment extends Fragment {
	
	//attributi per expandable listview
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    MainActivity mainActivity;
    String ipTitle;
     
    public HostFragment(){}
 
    public HostFragment(MainActivity mainActivity, String ipTitle){
    	this.mainActivity = mainActivity;
    	this.ipTitle = ipTitle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
  
        View rootView = inflater.inflate(R.layout.fragment_host, container, false);
        // get the listview
        expListView = (ExpandableListView) rootView.findViewById(R.id.lvExp);
        
        //Add Header for refresh Hour
        View view =   LayoutInflater.from(mainActivity).inflate(R.layout.header_expandablelistview, expListView, false);
        expListView.addHeaderView(view);
        
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        
        final HostFragment hostFragment = this;
        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild,rootView,mainActivity);
        assignListData();
        
        // setting list adapter
        expListView.setAdapter(listAdapter);
        
     // Group "Refresh" Listener onClick
        expListView.setOnGroupClickListener(new android.widget.ExpandableListView.OnGroupClickListener() {
        	@Override
        	public boolean onGroupClick (ExpandableListView parent, View v,
                    int groupPosition, long id) {
        		if(groupPosition == 0) {
        			String url = generateURL();
    				if(!url.equals("error"))
    					new RetrieveInformation(getActivity(),mainActivity,hostFragment).execute(url,ipTitle);
    				else
    					showMessage("Attenzione", "Setta l'ip e porta da cui reperire le informazioni dalle impostazioni dell'applicazione.");
        		}
                return false;
        	}
        });  
        
        // Child "Azioni" on click Listener
        expListView.setOnChildClickListener(new android.widget.ExpandableListView.OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if (groupPosition == 3) {
					if (childPosition == 0){
						//Show an Alert Dialog
						new AlertDialog.Builder(getActivity())
					    .setTitle("Spegni macchina")
					    .setMessage("Sei sicuro di voler spegnere la macchina con IP "+ipTitle+" ?")
					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					            // continue with shutdown
					        	String url = generateURL();
					        	if(!url.equals("error"))
					        		new DoAction(getActivity(),mainActivity,hostFragment,ipTitle).execute(url,"off");  
			    				else
			    					showMessage("Attenzione", "Setta l'ip e porta da cui reperire le informazioni dalle impostazioni dell'applicazione.");
					        }
					     })
					    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					            // do nothing
					        }
					     })
					    .setIcon(android.R.drawable.ic_dialog_alert)
					     .show();
					}else if(childPosition == 1) {
						//Show an Alert Dialog
						new AlertDialog.Builder(getActivity())
					    .setTitle("Kill sessioni utente")
					    .setMessage("Sei sicuro di voler stoppare tutte le sessioni utente della macchina con IP "+ipTitle+" ?")
					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					            // continue with Kill
					        	String url = generateURL();
					        	if(!url.equals("error"))
					        		new DoAction(getActivity(),mainActivity,hostFragment,ipTitle).execute(url,"kill_sessions");   
			    				else
			    					showMessage("Attenzione", "Setta l'ip e porta da cui reperire le informazioni dalle impostazioni dell'applicazione.");
					        }
					     })
					    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					            // do nothing
					        }
					     })
					    .setIcon(android.R.drawable.ic_dialog_alert)
					    .show();
					}	
				}
				return false;
			}
		});
        return rootView;
    }
    
    public void showMessage(String title, String message){
		 new AlertDialog.Builder(mainActivity)
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
   
   public String generateURL() {
   	SharedPreferencesModel controllerSP = new SharedPreferencesModel(mainActivity);
   	String ipServer = controllerSP.readIpServer();
   	if(!ipServer.equals("0.0.0.0"))
   		return "http://"+ipServer+":"+controllerSP.readPort()+"/external.php";
   	else
   		return "error";
   }
  
    private void assignListData() {
    	SharedPreferencesModel controllerSP = new SharedPreferencesModel(getActivity());
    	listAdapter.setContent(controllerSP.readHost(ipTitle));
        listDataHeader.add("Refresh");     
        List<String> refresh = new ArrayList<String>();
        listDataChild.put(listDataHeader.get(0), refresh);
    }
}
