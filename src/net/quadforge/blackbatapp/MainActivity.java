/* 
 * BlackBat Application - Version 0.1.0
 * ============================
 * last updated: 10/16/2013
 * 
 */

package net.quadforge.blackbatapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import java.util.Date; // Used to output time on wifi connected debug

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment; // Not used
import android.os.Handler;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent; // Used to download data using AndFTP
import android.database.Cursor; // Not used
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	// Allows us to output the date on each screen refresh for wifi scanning
	private Date outputDate;
	
	// Allows us to tag our output to LogCat
	private static final String TAG = MainActivity.class.getName();
	
	// Used when creating an AndFTP intent to tell it what we want done
	private static final int UPLOAD_FILES_REQUEST = 0;
	private static final int DOWNLOAD_FILES_REQUEST = 1;
	private static final int UPLOAD_FOLDER_REQUEST = 2;
	private static final int DOWNLOAD_FOLDER_REQUEST = 3;
	private static final int DOWNLOAD_FILE_ALIAS_REQUEST = 4;
	private static final int BROWSE_REQUEST = 5;
	private static final int SEND_REQUEST = 6;
	
	private TextView debug, debug1, debug2, debug3, debug4, debug5, wifiScanList; // Debug3 and debug5 not used
	private WifiManager wifiManager;
	private List<ScanResult> scanList; // List of all wifi networks that we can see
	private List<WifiLog> loggedNetworks; // Not used
	private List<NetworkConfiguration> networkConfigurations;
	private NetworkConfiguration targetNetwork;
	private ConnectivityManager connectManager;
	private NetworkInfo networkInfo;
	private Timer scanTimer;
	private boolean connecting; // boolean used to note when
	private boolean connectOnce; // boolean used to note when
	private DownloadManager downloadManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Initialize text views.
		debug        = (TextView)findViewById(R.id.debug); // Holds info on 
		debug1       = (TextView)findViewById(R.id.debug1); // Holds info on the status of a wifi connection
		debug2       = (TextView)findViewById(R.id.debug2); // Holds info on
		debug3       = (TextView)findViewById(R.id.debug3); // Holds info on
		debug4       = (TextView)findViewById(R.id.debug4); // Holds info on
		wifiScanList = (TextView)findViewById(R.id.wifi_scan_results_list); // Holds info on the currently connected wifi network
		
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
			
			// If wifi is off, turn it on
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
	
	// Receives new scans, calls other methods to do connecting and downloading. Does do the work of displaying debug info to screen
	private Runnable scanReceiver = new Runnable() {

		@Override
		public void run() {
			// Initialize date
			outputDate = new Date();
			
			Handler again = new Handler();
			// again.postDelayed(this, 1000); // Time of delay between each wifi scan?
			again.postDelayed(this, MissionParameters.WIFI_SCAN_INTERVAL); //TODO Time of delay between each wifi scan. Change to a variable, changable via settings file
			
			// Display current network
			networkInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			
			if (networkInfo.isConnected()) {
				debug1.setText("Current wifi network: " + wifiManager.getConnectionInfo().getSSID() + " " + outputDate.toString());
			} else {
				debug1.setText("Current wifi network: No network connected... " + outputDate.toString());
			}
			
			StringBuilder scanDisplay = new StringBuilder();
			
			// Get the results of the last wifi scan
			scanList = wifiManager.getScanResults();
			
			// A for each loop that goes into each entry in the scanList List
			for (ScanResult r : scanList) {
				
				// Adds the SSID, the received signal level and adds a line break
				scanDisplay.append(r.SSID + " " + r.level + "\n");
				// Passes a single wifi network to the checkForKnownNetwork method
				checkForKnownNetwork(r);
				
			}
			
			// Save the scanDisplay to an ArrayList so we can write to disk at end of program
			
			// Set this textView to the results of the scan that we organized in the above for each loop
			wifiScanList.setText(scanDisplay.toString());
			
			// Debug that displays if we are in the process of connecting or if we are connected
			debug4.setText("connecting?: " + connecting);
			debug.setText("now connecting/connected: " + targetNetwork.SSID);

		}
		
	};
	
	// This most likely won't be needed as we will just connect to any network we find
	private void loadNetworkConfigurations() {
		
		// Need to write code here that will dynamically add configured networks
		// Import from a file saved on the phone is probably the best option
		
		/*
		 * File wifiSettings = new File("wifiSettings.txt");
		 */
		
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

	// Most likely won't be needed as we just connect to any network we find
	private boolean checkForKnownNetwork(ScanResult r) {
		
		Iterator<NetworkConfiguration> i = networkConfigurations.iterator();
		
		while (i.hasNext()) { // While there are still unchecked entries in the list of networks we can see continue the loop? I think?
			
			NetworkConfiguration network = i.next(); // Set network to be the next unchecked network
			
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
	
	// Does the work of connecting, downloading, and then disconnecting from wifi networks
	Thread connectToNetwork = new Thread() {
		
		// This thread does the work of downloading data from the base station once connected
	    
		@Override
	    public void run() {
	    	
	    	while (true) { // Always have this looking to see if we are connected to a new wifi network
	    		
	    		if (connecting && !connectOnce) {
	    			wifiManager.disconnect(); // Make sure we are disconnected from any wifi network
	    			wifiManager.enableNetwork(targetNetwork.networkId, true); // 
	    			wifiManager.reconnect();
	    			connectOnce = true;
	    			
	    			Timer newConnection = new Timer();
		    		newConnection.schedule(new TimerTask() {
		    			
		    			@Override
		    			public void run() {
		    				
		    				// queue download
		    				ArrayList<Uri> dlUris = targetNetwork.getDownloadUris(); // Make an array list of the files we want to download, in URI form
		    				
		    				//for (Uri u : dlUris) {
		    					//downloadData(u); // Send files to download to the downloadData method
		    					downloadData(); // At this time we only need to call this as files to download are hard coded
		    				//}
		    				
		    				Timer download = new Timer();
		    				download.schedule(new TimerTask() {
		    					
		    					@Override
		    					public void run() {
		    						
		    						// Reset the variable
		    						connecting = false;
		    						// Reset the variable
		    						connectOnce = false;
		    						// Disconnect from any connected network
		    						wifiManager.disconnect();
		    						// TODO Figure out what this does
		    						targetNetwork.equals(null); 
		    						// Remove the network we just downloaded data from out of wifi config
		    						wifiManager.removeNetwork(targetNetwork.networkId);
		    					}
		    					
		    				}, 120000); // <-- Time to allow for the file to download, if it doesn't download in this time we interrupt the download
		    				
		    			}
		    			
		    		}, 120000); // <--- Time to wait before download (after connecting) (Orig)
	    		}
	    		
	    	}
	    		
	    }
	};
	
	
		// Uses AndFTP to download data off of weather stations
		private void downloadData()
		//private void downloadData(Uri uri)
	{
		// Taken from http://www.lysesoft.com/products/andftp/intent.html
		// These settings are stored in a key : value format. The first thing is the key, the property name, second is the setting value

		Intent downloadFiles = new Intent();
		
		downloadFiles.setAction(Intent.ACTION_PICK);
		// Server to download from
		Uri ftpUri = Uri.parse("ftp://192.168.1.2");
		
		downloadFiles.setDataAndType(ftpUri, "vnd.android.cursor.dir/lysesoft.andftp.uri");
		// Action for AndFTP to do, in this case download from the server
		downloadFiles.putExtra("command_type", "download");
		// FTP username to use
		downloadFiles.putExtra("ftp_username", "root");
		// FTP password to use
		downloadFiles.putExtra("ftp_password", "root");
		
		//intent.putExtra("ftp_keyfile", "/sdcard/rsakey.txt");
		//intent.putExtra("ftp_keypass", "optionalkeypassword");
		
		// Set optional FTP options
		downloadFiles.putExtra("ftp_pasv", "true");
		
		// First file to download
		downloadFiles.putExtra("remote_file1", "/www/Abraham.txt");
		// Second file to download
		downloadFiles.putExtra("remote_file2", "/www/Linksys.txt");
		// Third file to download
		downloadFiles.putExtra("remote_file3", "/www/test.png");
		// Target local folder where files will be downloaded
		// downloadFiles.putExtra("local_folder", Environment.getExternalStorageDirectory().getPath() + "/stationdata");
		//downloadFiles.putExtra("local_folder", Environment.getExternalStorageDirectory().getAbsolutePath() + "stationdata");
		downloadFiles.putExtra("local_folder", "/sdcard2/stationdata"); // Bad idea to hardcode this for real but doing this for testing
		
		// Closes the AndFTP interface after the download is complete
		downloadFiles.putExtra("close_ui", "true"); 

		// Finally start the Activity to be closed after transfer:
		startActivityForResult(downloadFiles, DOWNLOAD_FILES_REQUEST);
		
		// Transfer status will be returned in onActivityResult method:
		String status = downloadFiles.getStringExtra("TRANSFERSTATUS");
		String files = downloadFiles.getStringExtra("TRANSFERAMOUNT"); 
		String size = downloadFiles.getStringExtra("TRANSFERSIZE");
		String time = downloadFiles.getStringExtra("TRANSFERTIME");

	}
		
		// Taken from the AndFTP example third party client
		protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
		{
			Log.i(TAG, "Result: "+resultCode+ " from request: "+requestCode);
			if (intent != null)
			{
				String transferredBytesStr = intent.getStringExtra("TRANSFERSIZE");
				String transferTimeStr = intent.getStringExtra("TRANSFERTIME");
				Log.i(TAG, "Transfer status: " + intent.getStringExtra("TRANSFERSTATUS"));
				Log.i(TAG, "Transfer amount: " + intent.getStringExtra("TRANSFERAMOUNT") + " file(s)");
				Log.i(TAG, "Transfer size: " + transferredBytesStr + " bytes");
				Log.i(TAG, "Transfer time: " + transferTimeStr + " milliseconds");
				// Compute transfer rate.
				if ((transferredBytesStr != null) && (transferTimeStr != null))
				{
					try
					{
						long transferredBytes = Long.parseLong(transferredBytesStr);
						long transferTime = Long.parseLong(transferTimeStr);
						double transferRate = 0.0;
						if (transferTime > 0) transferRate = ((transferredBytes) * 1000.0) / (transferTime * 1024.0);
						Log.i(TAG, "Transfer rate: " + transferRate + " KB/s");
					} 
					catch (NumberFormatException e)
					{
						// Cannot parse string.
					}
				}
			}
		}
	
}