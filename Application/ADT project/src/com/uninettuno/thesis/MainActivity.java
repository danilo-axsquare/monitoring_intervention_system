package com.uninettuno.thesis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
 
    // nav drawer title
    private CharSequence mDrawerTitle;
 
    // used to store app title
    private CharSequence mTitle;
 
    protected ArrayList<NavDrawerItem> navDrawerItems;
    protected NavDrawerListAdapter adapter;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
 
        mTitle = mDrawerTitle = getTitle();
 
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        
        View view =   LayoutInflater.from(this).inflate(R.layout.header_listview, mDrawerList, false);
        mDrawerList.addHeaderView(view);
        final MainActivity mainActivity = this;
        //Setto il listener alla TextView Aggiorna
         view.findViewById(R.id.textViewAggiorna).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String ip = "all!";
				String url = generateURL();
				if(!url.equals("error"))
					new RetrieveInformation(getApplicationContext(),mainActivity).execute(url,ip);
				else
					showMessage("Attenzione", "Setta l'ip e porta da cui reperire le informazioni dalle impostazioni dell'applicazione.");
			}
		});
        //Inizializza il menù laterale con le informazioni delle SharedPreferences
        navDrawerItems = new ArrayList<NavDrawerItem>();
        ArrayList<String> hostShared = new SharedPreferencesModel(this).readListHosts();
        for(int i=0;i<hostShared.size();i++){
			navDrawerItems.add(new NavDrawerItem(hostShared.get(i)));
		}
        
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
 
        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);
        //se la lista è vuota mostro un alertDialog
        if (navDrawerItems.size() == 0){
        	showMessage("Nessuna informazione","Premi il tasto aggiorna dal menù laterale per caricare le informazioni.");
        }else {//setto l'ora dell'aggionramento
        	TextView oraAggiornamentoAll = (TextView)findViewById(R.id.oraAggiornamento);
        	Date dataAll = new Date(new SharedPreferencesModel(this).readDateRefreshAll()*1000);
        	Calendar cal = Calendar.getInstance();
        	cal.setTime(dataAll);
			String datePrint = "Aggiornato alle:"+ cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+" del "+cal.get(Calendar.DAY_OF_MONTH)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.YEAR);
        	oraAggiornamentoAll.setText(datePrint, TextView.BufferType.EDITABLE);
        }
        
 
        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
 
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }
 
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
 
        if (savedInstanceState == null) {
            // on first time display view for first nav item
        	if( navDrawerItems.size() > 0 )
        		displayView(1);
        }
        

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
    
    public String generateURL() {
    	SharedPreferencesModel controllerSP = new SharedPreferencesModel(this);
    	String ipServer = controllerSP.readIpServer();
    	if(!ipServer.equals("0.0.0.0"))
    		return "http://"+ipServer+":"+controllerSP.readPort()+"/external.php";
    	else
    		return "error";
    }
    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
        case R.id.action_settings:
        	//Apri activity settings
        	startActivity(new Intent(this, SettingActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 
    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
 
    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
    	if(position > 0) {
	    	Fragment fragment = new HostFragment(this,navDrawerItems.get(position-1).getTitle()); 
	        FragmentManager fragmentManager = getFragmentManager();
	        fragmentManager.beginTransaction()
	            .replace(R.id.frame_container, fragment).commit();
	 
	        // update selected item and title, then close the drawer
		    mDrawerList.setItemChecked(position, true);
		    mDrawerList.setSelection(position);
		    setTitle(navDrawerItems.get(position-1).getTitle());
		    mDrawerLayout.closeDrawer(mDrawerList);
    	}
    }
 
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
 
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
 
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
 
}