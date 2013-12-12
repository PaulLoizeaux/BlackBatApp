/**
 * Works with SQL databases.
 * 
 * Unused at this time
 */

package net.quadforge.blackbatapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserData extends SQLiteOpenHelper {

	public UserData(Context context) {
		super(context, MissionParameters.DATABASE_NAME, null, MissionParameters.DATABASE_VERSION);
		
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(MissionParameters.TABLE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
