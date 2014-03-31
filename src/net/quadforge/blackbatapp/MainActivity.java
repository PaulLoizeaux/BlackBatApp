/** 
 * Connects to wifi networks, connects to an FTP server, then disconnects from the wifi network
 * 
 * BlackBat Application - Version 0.1.0
 * ============================
 * last updated: 11/29/2013
 * 
 */

package net.quadforge.blackbatapp;

import java.io.File; // Allows us to output wifi log
import java.io.FileNotFoundException;
import java.io.FileOutputStream; // Allows output to file
import java.io.FileWriter; // Allows us to output wifi log
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.net.ftp.FTPClient; // Allows us to download via FTP
import org.apache.commons.net.ftp.FTPClientConfig; // Gives us configuration options for the above FTP client
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.util.Date; // Used to output time on wifi connected debug

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment; // Not used
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	// Allows us to output the date on each screen refresh for wifi scanning
	private Date outputDate;
	
	// Allows us to tag our output to Android's LogCat
	private static final String TAG = MainActivity.class.getName();
	
	// Used when creating an AndFTP intent to tell it what we want done
	//private static final int DOWNLOAD_FILES_REQUEST = 1;
	
	private TextView debug, debug1, debug2, debug3, debug4, debug5, wifiScanList; // debug, debug3, and debug5 not used
	private WifiManager wifiManager;
	private List<ScanResult> scanList; // List of all wifi networks that we can see
	//private List<WifiLog> loggedNetworks; // Not used
	private List<String> allScans;
	private ConnectivityManager connectManager;
	private NetworkInfo networkInfo;
	private Timer scanTimer;
	private NetworkConfiguration targetedNetwork; // Stores the wifi network we want to connect to each scan
	
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
		
		// Initialize lists
		scanList              = new ArrayList<ScanResult>();
		//loggedNetworks        = new ArrayList<WifiLog>();
		allScans = new ArrayList<String> (); // Place to store all the scans seen by the application
		
		// Initialize the variable to store the network we want to connect to
		targetedNetwork = new NetworkConfiguration();
		
		// Debugging 
		
		// Start thread. Needs to be here because onResume() may run more than once and you can only start a thread once
		// connectToNetwork.start();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// Check to see if wifi is enabled.
		if (!wifiManager.isWifiEnabled()) {
			
			// Wifi is off, turn it on
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
		
		Log.d(TAG, "onPause activated");
		
		finish();
	}

	@Override
	protected void onStop() {
		super.onStop(); // Always call this first
		
		Log.d(TAG, "onStop activated");
		
		finish();
	}
	
	/**
	 * Saves found wifi networks to a file
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.d(TAG, "onDestroy activated");
		
		// Overall: Save allScans to a file so we can read this log of wifi data seen on the flight once quad is retrieved

		// Open a new file to save scans to, embed time and date in file name
		File outputFile = new File(Environment.getExternalStorageDirectory() + "QuadForge/WifiScan.txt");

		try
		{
			// Make a FileWriter so we can save characters
			FileWriter wifiWriter = new FileWriter(outputFile);

			// Output allScans as a huge String to that file
			// This might need line breaks between each line, I'm not sure
			wifiWriter.write(allScans.toString());

			// Done using the writer so we close it
			wifiWriter.close();
			
			Log.i(TAG, "Wifi scans saved to file");

		}
		catch (IOException e)
		{
			e.getStackTrace();
		}
		
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Receives new scans, calls other methods to do connecting and downloading. Does do the work of displaying debug info to screen
	 */
	private Runnable scanReceiver = new Runnable() {

		@Override
		public void run() {
			
			// Initialize date, used in screen output of wifi scans
			outputDate = new Date();
			
			
			Handler again = new Handler();
			again.postDelayed(scanReceiver, MissionParameters.WIFI_SCAN_INTERVAL); //TODO Time of delay between each wifi scan. Change to a variable, changable via settings file
			
			
			// Display current network
			networkInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			
			if (networkInfo.isConnected()) {
				
				// Outputs the wifi network SSID/name and also the current date and time
				debug1.setText("Current wifi network: " + wifiManager.getConnectionInfo().getSSID() + " " + outputDate.toString());
				
				Log.d(TAG, "Connected to " + wifiManager.getConnectionInfo().getSSID());
			}
			else {
				
				// Outputs that no wifi netork is connect and the current date and time 
				debug1.setText("Current wifi network: No network connected... " + outputDate.toString());
				
				Log.d(TAG, "Not connected to a wifi network");
			}
			
			StringBuilder scanDisplay = new StringBuilder();
			
			// Get the results of the last wifi scan
			scanList = wifiManager.getScanResults();
			
			Log.d(TAG, "Got new scan results.");
			
			// A for each loop that goes into each entry in the scanList List
			for (ScanResult r : scanList) {
				
				// Adds the SSID, the received signal level, capabilities and adds a line break
				scanDisplay.append(r.SSID + " " + r.level + "\n" + r.capabilities + "\n");
				// Passes a single wifi network to the checkForKnownNetwork method
				//checkForKnownNetwork(r);
				
				Log.d(TAG, "Starting search for expected wifi network.");
				
				// Connects to all wifi networks we can see that are open
				if (validWifi(r))
				{
					
					// Use NetworkConfiguration class to store network to connect to
					
					// Since the wifi network is open copy the parameters we need
					targetedNetwork.SSID = r.SSID;
					targetedNetwork.BSSID = r.BSSID;
					targetedNetwork.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
					int networkID = 0;
					
					// This only adds the network, it does not connect to it. If this returns -1 we have a problem
					networkID = wifiManager.addNetwork(targetedNetwork); // This is failing for some reason on the phone I have for testing
					
					Log.d(TAG, "Found an expected wifi network." + networkID);
		    			
		    		// Make sure we are disconnected from any wifi network
		    		wifiManager.disconnect();
		    			
		    		// Enable the configured target network	    			
		    		wifiManager.enableNetwork(networkID, true);
		    		
		    		// Call file download method, this will return once it is done
		    		ftpDownload();
		    		
		    		// Disconnect from the wifi network
		    		wifiManager.disableNetwork(networkID);
		    		
		    		// Remove the wifi network
		    		wifiManager.removeNetwork(networkID);

					
				}
				
				// Save all wifi networks seen
				allScans.add(r.toString());
				
			}
			
			// Save the scanDisplay to an ArrayList so we can write to disk at end of program
			
			// Set this textView to the results of the scan that we organized in the above for each loop
			wifiScanList.setText(scanDisplay.toString());

		}
		
	};
	
	/**
	 * Check if the scanned wifi network has open encryption
	 * @param r A ScanResult object to check if it is Open encryption
	 * @return True if the wifi network has open encryption, false if it is something else
	 */
	private boolean validWifi(ScanResult r)
	{
		
		// Check if authentication is Open, if it is return true, if it isn't return false
		if (r.capabilities.equals("[ESS]") && !r.BSSID.equals(wifiManager.getConnectionInfo().getBSSID()))
		{
			
			// Set the boolean flag to indicate we are in the process of connecting to a wifi network			
			return true;
			
			// Connect to network
		}
		return false;		
	}
	
	/**
	 * Connects to an FTP server and downloads all files in the /www/ directory
	 */
	private void ftpDownload()
	{

		// Address of the FTP server
		String server = "192.168.1.2";

		// The FTP Client object
		FTPClient ftp = new FTPClient();

		boolean error = false; // Boolean flag if the problem encounters any problem

		try {

			int reply; // Store the FTP server command result reply

			// FTP server to connect to
			ftp.connect(server);

			// FTP username
			ftp.user("root");

			// FTP password
			ftp.pass("root");

			// Log the FTP server we connect to
			Log.i(TAG, "Connected to " + server);

			// Log the response from the FTP server
			Log.i(TAG, ftp.getReplyString());

			// After connection attempt, you should check the reply code to verify success.
			reply = ftp.getReplyCode();

			// Check the reply code to see if it is a successful response or a failure response
			if(!FTPReply.isPositiveCompletion(reply)) {

				// If the response is a failure, disconnect from the server
				ftp.disconnect();

				// Print out that the connect didn't work to the server
				Log.e(TAG, "FTP server refused connection.");

				// Stop running the program
				System.exit(1);
			}

			// Store list of files in target directory from the FTP server

			FTPFile[] filesToDownload = ftp.listFiles("/www/"); // At this time test files are stored in /www/ directory

			// Download each file listed in the directory

			for (FTPFile ftpDownloadFile : filesToDownload)
			{

				// Make sure this entry is a file
				if(ftpDownloadFile.isFile())
				{

					// Make a local file to store the downloaded file in
					File saveFile = new File(Environment.getExternalStorageDirectory() + "QuadForge" + ftpDownloadFile.getName()); // Directory needs to exist

					// Make an output stream to save this file in
					OutputStream saveFileStream = new FileOutputStream(saveFile);

					// Download the file and put it into the output stream for saving
					ftp.retrieveFile(ftpDownloadFile.getName(), saveFileStream);
				}

			}

			ftp.logout();
		} catch(IOException e) {
			error = true;
			e.printStackTrace();
		} finally {
			if(ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {
					// do nothing
				}
			}
			System.exit(error ? 1 : 0);
		}

	}

}