/**
 * Extended WifiConfiguration that is able to store FTP server information
 */

package net.quadforge.blackbatapp;

import java.util.ArrayList;

import android.net.Uri;
import android.net.wifi.WifiConfiguration;

public class NetworkConfiguration extends WifiConfiguration {

	private ArrayList<DataStructure> dataStructures;
	protected String networkRootPath;
	
	/**
	 * Construct a new NetworkConfiguraiton object
	 * No FTP server is specified
	 */
	public NetworkConfiguration() {
		super();
		
		networkRootPath = "";
		dataStructures = new ArrayList<DataStructure>();
		initializeDataStructures();
	}
	
	/**
	 * Construct a new NetworkConfiguration object
	 * @param path Address of the FTP server to connect to
	 */
	public NetworkConfiguration(String path) {
		super();
		
		networkRootPath = path;
		dataStructures = new ArrayList<DataStructure>();
		initializeDataStructures();
	}
	
	private void initializeDataStructures() {
		
		if (networkRootPath == "http://192.168.1.1/") {
			DataStructure a = new DataStructure("Open.txt"); // <-- add code to dynamically add files
			dataStructures.add(a);
			
			DataStructure b = new DataStructure("Wrt.txt"); // <-- add code to dynamically add files
			dataStructures.add(b);
		}
		
		
		if (networkRootPath == "http://192.168.1.2/") {
			DataStructure c = new DataStructure("test.png"); // <-- add code to dynamically add files
			dataStructures.add(c);
			
			DataStructure d = new DataStructure("Linksys.txt"); // <-- add code to dynamically add files
			dataStructures.add(d);
		}
		
	}
	
	/**
	 * Returns URIs of the HTTP server address and the file we want to download
	 * No longer in use as we don't connect over HTTP now
	 * @return An ArrayList of the URIs we want to connect to
	 */
	@Deprecated
	public ArrayList<Uri> getDownloadUris() {
		
		ArrayList<Uri> list = new ArrayList<Uri>();
		
		for (DataStructure d : dataStructures) {
			list.add(d.downloadUri);
		}
		
		return list;
	}
	
	private class DataStructure {
		
		private String fileName, destinationFileName;
		protected Uri downloadUri;
		private boolean downloaded;
		
		DataStructure(String input) {
			fileName = input;
			downloadUri = Uri.parse(networkRootPath + fileName);
			downloaded = false;
		}
		
		public void setFileName(String input) {
			fileName = input;
		}
		
		public String getFileName() {
			return fileName;
		}
		
		public void setDownloaded(boolean input) {
			downloaded = input;
		}
		
		public boolean getDownloaded() {
			return downloaded;
		}
		
	}
}
