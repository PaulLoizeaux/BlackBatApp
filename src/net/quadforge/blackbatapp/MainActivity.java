/* 
 * BlackBat Application - Version 0.1.0
 * ============================
 * last updated: 4/26/2013
 * 
 */

package net.quadforge.blackbatapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView debug, debug1, debug2, debug3, debug4, debug5, wifiScanList;
	private WifiManager wifiManager;
	private List<ScanResult> scanList;
	private List<WifiLog> loggedNetworks;
	private List<NetworkConfiguration> networkConfigurations;
	private NetworkConfiguration targetNetwork;
	private ConnectivityManager connectManager;
	private NetworkInfo networkInfo;
	private Timer scanTimer;
	private boolean connecting;
	private boolean connectOnce;
	private DownloadManager downloadManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Initialize text views.
		debug        = (TextView)findViewById(R.id.debug);
		debug1       = (TextView)findViewById(R.id.debug1);
		debug2       = (TextView)findViewById(R.id.debug2);
		debug3       = (TextView)findViewById(R.id.debug3);
		debug4       = (TextView)findViewById(R.id.debug4);
		wifiScanList = (TextView)findViewById(R.id.wifi_scan_results_list);
		
		// Initialize WifiManager, ConnectivityManager, NetworkInfo, and DownloadManager
		wifiManager     = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		connectManager  = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		networkInfo     = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		
		// Initialize lists
		scanList              = new ArrayList<ScanResult>();
		loggedNetworks        = new ArrayList<WifiLog>();
		networkConfigurations = new ArrayList<NetworkConfiguration>();
		
		// Initialize globals
		targetNetwork = new NetworkConfiguration();
		connecting = false;
		connectOnce = false;
		
		// Debugging
		
		// Start thread. Needs to be here because onResume() may run more than once and you can only start a thread once
		connectToNetwork.start();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		loadNetworkConfigurations();
		
		// Check to see if wifi is enabled.
		if (!wifiManager.isWifiEnabled()) {
			
			// Turn wifi on
			wifiManager.setWifiEnabled(true);
			
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		scanTimer = new Timer();
		scanTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				
				// Request wifi scan
				wifiManager.startScan();
				
			}
			
		}, MissionParameters.WIFI_SCAN_INITIAL_DELAY, MissionParameters.WIFI_SCAN_INTERVAL);
		
		
		new Handler().post(scanReceiver);
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		finish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private Runnable scanReceiver = new Runnable() {

		@Override
		public void run() {
			Handler again = new Handler();
			again.postDelayed(this, 1000);
			
			// Display current network
			networkInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			
			if (networkInfo.isConnected()) {
				debug1.setText("current network: " + wifiManager.getConnectionInfo().getSSID());
			} else {
				debug1.setText("current network: not connected");
			}
			
			StringBuilder scanDisplay = new StringBuilder();
			
			scanList = wifiManager.getScanResults();
			
			for (ScanResult r : scanList) {
				
				scanDisplay.append(r.SSID + " " + r.level + "\n");
				checkForKnownNetwork(r);
				
			}
			
			wifiScanList.setText(scanDisplay.toString());
			
			// Debug
			debug4.setText("connecting?: " + connecting);
			debug.setText("now connecting/connected: " + targetNetwork.SSID);

		}
		
	};
	
	private void loadNetworkConfigurations() {
		
		// Need to write code here that will dynamically add configured networks
		NetworkConfiguration net = new NetworkConfiguration("http://192.168.1.1/");
    	net.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    	net.SSID = "\"OpenWrt\"";
    	net.BSSID = "a0:f3:c1:c9:39:03";
    	net.networkId = wifiManager.addNetwork(net);
    	networkConfigurations.add(net);
    	
    	NetworkConfiguration net2 = new NetworkConfiguration("http://192.168.1.2/");
    	net2.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    	net2.SSID = "\"AbrahamLinksys\"";
    	net2.BSSID = "a0:f3:c1:a6:6c:58";
    	net2.networkId = wifiManager.addNetwork(net2);
    	networkConfigurations.add(net2);
    	
	}

	private boolean checkForKnownNetwork(ScanResult r) {
		
		Iterator<NetworkConfiguration> i = networkConfigurations.iterator();
		
		while (i.hasNext()) {
			
			NetworkConfiguration network = i.next();
			
			if (r.BSSID.equals(network.BSSID) && !r.BSSID.equals(wifiManager.getConnectionInfo().getBSSID()) && !connecting) {
				
				// Connect to network
				
				connecting = true;
				targetNetwork = network;
				
				i.remove();
				
				debug2.setText("remaining configured networks: " + networkConfigurations.size());
				
				return true;
			}
			
		}
		
		return false;	
	}
	
	Thread connectToNetwork = new Thread() {
		
	    @Override
	    public void run() {
	    	
	    	while (true) {
	    		
	    		if (connecting && !connectOnce) {
	    			wifiManager.disconnect();
	    			wifiManager.enableNetwork(targetNetwork.networkId, true);
	    			wifiManager.reconnect();
	    			connectOnce = true;
	    			
	    			Timer newConnection = new Timer();
		    		newConnection.schedule(new TimerTask() {
		    			
		    			@Override
		    			public void run() {
		    				
		    				// queue download
		    				ArrayList<Uri> dlUris = targetNetwork.getDownloadUris();
		    				
		    				for (Uri u : dlUris) {
		    					downloadData(u);
		    				}
		    				
		    				Timer download = new Timer();
		    				download.schedule(new TimerTask() {
		    					
		    					@Override
		    					public void run() {
		    						connecting = false;
		    						connectOnce = false;
		    						wifiManager.disconnect();
		    						targetNetwork.equals(null);
		    					}
		    					
		    				}, 20000); // <-- time to download
		    				
		    			}
		    			
		    		}, 20000); // <--- time to wait before download (after connecting)
	    		}
	    		
	    	}
	    		
	    }
	};
	
	private void downloadData(Uri uri) {
		
		DownloadManager.Request downloadRequest = new DownloadManager.Request(uri);
		
		downloadManager.enqueue(downloadRequest);
	}
}