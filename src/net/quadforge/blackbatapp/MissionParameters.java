/**
 * Stores configuration settings for the mission.
 * 
 * Defines various settings in regards to things like scan times, database connections, wifi networks to look for, servers to connect to and files to download.
 * 
 * Most of this at this time is not used. This will transition at a later date to be a settings file so it is user editable.
 */

package net.quadforge.blackbatapp;

public abstract class MissionParameters {

	// Delays
	public static int BROADCAST_DELAY = 5000;
	public static int DOWNLOAD_DELAY = 2000;
	public static int WIFI_SWITCH_DELAY = 6000;

	// Database configuration information, not used currently
	public static String DATABASE_NAME = "Test";
	public static int DATABASE_VERSION = 2;
	public static String TABLE_NAME = "BlackBatData";
	public static String KEY_WORD = "ID";
	public static String KEY_DEFINITION = "test";
	public static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +KEY_WORD + " TEXT, " + KEY_DEFINITION + " TEXT);";
	
	// Debug
	public static String DEBUG_TAG = "bbdebug";

	// Time between wifi scans in milliseconds and initial scan delay.
	public static Long WIFI_SCAN_INTERVAL = 15000L;
	public static Long WIFI_SCAN_INITIAL_DELAY = 0L;

	// Do not change this.
	public static Long WIFI_TIME_INCREMENT = WIFI_SCAN_INTERVAL / 1000;

	// GPS settings
	public static Long GPS_SCAN_INTERVAL = (long) 5000;
	public static Boolean GPS_ON = false;

	// Name of text file for the wifi log
	public static String LOG_FILE_NAME = "WifiLog.txt";

	// Name of directory to host text file on phone.
	public static String LOG_DIRECTORY_NAME = "QuadForge Logs";

	// Format of date/time.
	public static String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

	// Temporary networks logged max before transitioning to Upload activity.
	public static int LOGGED_NETWORKS_MAX = 5000;

	// FTP information used in Upload activity.
	// Most likely not needed, will just pull data off SD card
	public static String FTP_SERVER_ADDRESS = "ftp://quadforge.net";
	public static String FTP_USERNAME = "";
	public static String FTP_PASSWORD = "";

	// Networks
	public static int NETWORK_COUNT = 2;// removal of 3rd network
	public static String NETWORK1_SSID = "OpenWrt";
	public static String NETWORK2_SSID = "AbrahamLinksys";

	// HTTP transfer information
	public static Boolean HTTP_XFER_ON = true;
	public static String GENERAL_DL_DESCRIPTION = "BlackBat Application automated download.";

	// File stuff
	public static String FILE1_URL = "http://192.168.1.1/Open.txt";
	public static String FILE1_NAME = "Open.txt";
	public static String FILE1_ASSOC = "OpenWrt";

	public static String FILE2_URL = "http://192.168.1.1/Wrt.txt";
	public static String FILE2_NAME = "Wrt.txt";
	public static String FILE2_ASSOC = "OpenWrt";

	public static String FILE3_URL = "http://192.168.1.2/Abraham.txt";
	public static String FILE3_NAME = "Abraham.txt";
	public static String FILE3_ASSOC = "AbrahamLinksys";

	public static String FILE4_URL = "http://192.168.1.2/Linksys.txt";
	public static String FILE4_NAME = "Linksys.txt";
	public static String FILE4_ASSOC = "AbrahamLinksys";

}