package net.quadforge.blackbatapp;

import java.util.ArrayList;
import java.util.List;
import android.net.wifi.ScanResult;

public class WifiLog {
	
	public Boolean inLastScan;
	private String BSSID;
	private String SSID;
	private String capabilities;
	private CharSequence discoveryDateAndTime;
	private int level;
	private int frequency;
	private long secondsInRange;
	private List<Integer> levelArray;
	private long averageLevel;
	private Boolean isInRange;
	
	// CONSTRUCTOR
	public WifiLog() {
		secondsInRange = (long)0;
		levelArray = new ArrayList<Integer>(0);
		isInRange = true;
	}
	
	// SET FUNCTIONS
	
	public void setInRange(Boolean input) {
		isInRange = input;
	}
	
	public void setBSSID(String input) {
		BSSID = input;
	}
	
	public void setSSID(String input) {
		SSID = input;
	}
	
	public void setLevel(int input) {
		level = input;
	}
	
	public void setFrequency(int input) {
		frequency = input;
	}
	
	public void setCapabilities(String input) {
		capabilities = input;
	}
	
	public void setDiscoveryDateAndTime(CharSequence input) {
		discoveryDateAndTime = input;
	}
	
	// GET FUNCTIONS
	
	public Boolean getInRange() {
		return isInRange;
	}
	
	public long getAverageLevel() {
		return averageLevel;
	}
	
	public long getSecondsInRange() {
		return secondsInRange;
	}
	
	public CharSequence getDiscoveryDateAndTime() {
		return discoveryDateAndTime;
	}
	
	public String getCapabilities() {
		return capabilities;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public int getLevel() {
		return level;
	}
	
	public String getBSSID() {
		return BSSID;
	}
	
	public String getSSID() {
		return SSID;
	}
	
	// OTHER FUNCTIONS
	
	public void addTimeTick() {
		secondsInRange += MissionParameters.WIFI_TIME_INCREMENT;
	}
	
	public void addLevelEntry(int input) {
		levelArray.add(input);
	}
	
	public void calcAverageLevel() {
		int total = 0;
		
		for(int i : levelArray) {
			total += i;
		}
		
		averageLevel = total / levelArray.size();
	}
	
	public void update(int level) {
		addTimeTick();
		setInRange(true);
		addLevelEntry(level);
		calcAverageLevel();
	}
	
	public void initialize(ScanResult r, CharSequence c) {
		BSSID = r.BSSID;
		SSID = r.SSID;
		level = r.level;
		frequency = r.frequency;
		capabilities = r.capabilities;
		discoveryDateAndTime = c;
	}
}
