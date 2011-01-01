package com.monstarlab.servicedroid.model;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.monstarlab.servicedroid.R;
import com.monstarlab.servicedroid.model.Models.BibleStudies;
import com.monstarlab.servicedroid.model.Models.Calls;
import com.monstarlab.servicedroid.model.Models.Literature;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;

public class ServiceProvider extends ContentProvider {
	
	private static final String TAG = "ServiceProvider";
	
	private static final String DATABASE_NAME = "servicedroid";
    private static final int DATABASE_VERSION = 3;
    
    private static final String TIME_ENTRIES_TABLE = "time_entries";
    private static final String CALLS_TABLE = "calls";
    private static final String BIBLE_STUDIES_TABLE = "bible_studies";
    private static final String RETURN_VISITS_TABLE = "return_visits";
    private static final String LITERATURE_TABLE = "literature";
    private static final String PLACEMENTS_TABLE = "placements";
    
    private static HashMap<String, String> sTimeProjectionMap;
    private static HashMap<String, String> sCallProjectionMap;
    private static HashMap<String, String> sRVProjectionMap;
    private static HashMap<String, String> sBibleStudyProjectionMap;
    private static HashMap<String, String> sLiteratureProjectionMap;
    private static HashMap<String, String> sPlacementProjectionMap;
    
    private static final int TIME_ENTRIES = 1;
    private static final int TIME_ENTRY_ID = 2;
    private static final int CALLS = 3;
    private static final int CALLS_ID = 4;
    private static final int RETURN_VISITS = 5;
    private static final int RETURN_VISITS_ID = 6;
    private static final int PLACEMENTS = 7;
    private static final int PLACEMENTS_ID = 8;
    private static final int LITERATURE = 9;
    private static final int LITERATURE_ID = 10;
    private static final int PLACED_MAGAZINES = 11;
    private static final int PLACED_BROCHURES = 12;
    private static final int PLACED_BOOKS = 13;
    private static final int PLACEMENTS_DETAILS = 14;
    private static final int PLACEMENTS_DETAILS_ID = 15;
    private static final int BIBLE_STUDIES = 16;
    private static final int BIBLE_STUDIES_ID = 17;
    
    private static final UriMatcher sUriMatcher;
    
    static {
    	sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	sUriMatcher.addURI(Models.AUTHORITY, "time_entries", TIME_ENTRIES);
    	sUriMatcher.addURI(Models.AUTHORITY, "time_entries/#", TIME_ENTRY_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "calls", CALLS);
    	sUriMatcher.addURI(Models.AUTHORITY, "calls/#", CALLS_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "returnvisits", RETURN_VISITS);
    	sUriMatcher.addURI(Models.AUTHORITY, "returnvisits/#", RETURN_VISITS_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "placements", PLACEMENTS);
    	sUriMatcher.addURI(Models.AUTHORITY, "placements/#", PLACEMENTS_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "literature", LITERATURE);
    	sUriMatcher.addURI(Models.AUTHORITY, "literature/#", LITERATURE_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "placements/magazines", PLACED_MAGAZINES);
    	sUriMatcher.addURI(Models.AUTHORITY, "placements/brochures", PLACED_BROCHURES);
    	sUriMatcher.addURI(Models.AUTHORITY, "placements/books", PLACED_BOOKS);
    	sUriMatcher.addURI(Models.AUTHORITY, "placements/details", PLACEMENTS_DETAILS);
    	sUriMatcher.addURI(Models.AUTHORITY, "placements/details/#", PLACEMENTS_DETAILS_ID);
    	sUriMatcher.addURI(Models.AUTHORITY, "biblestudies", BIBLE_STUDIES);
    	sUriMatcher.addURI(Models.AUTHORITY, "biblestudies/#", BIBLE_STUDIES_ID);
    	
    	sTimeProjectionMap = new HashMap<String, String>();
    	sTimeProjectionMap.put(TimeEntries._ID, TimeEntries._ID);
    	sTimeProjectionMap.put(TimeEntries.LENGTH, TimeEntries.LENGTH);
    	sTimeProjectionMap.put(TimeEntries.DATE, TimeEntries.DATE);
    	
    	sCallProjectionMap = new HashMap<String, String>();
    	sCallProjectionMap.put(Calls._ID, CALLS_TABLE + "." + Calls._ID);
    	sCallProjectionMap.put(Calls.NAME, CALLS_TABLE + "." + Calls.NAME);
    	sCallProjectionMap.put(Calls.ADDRESS, CALLS_TABLE + "." + Calls.ADDRESS);
    	sCallProjectionMap.put(Calls.NOTES, CALLS_TABLE + "." + Calls.NOTES);
    	sCallProjectionMap.put(Calls.DATE, CALLS_TABLE + "." + Calls.DATE);
    	sCallProjectionMap.put(Calls.TYPE, CALLS_TABLE + "." + Calls.TYPE);
    	sCallProjectionMap.put(Calls.LOCATION, CALLS_TABLE + "." + Calls.LOCATION);
    	sCallProjectionMap.put(Calls.IS_STUDY, "((select count(bible_studies._id) from bible_studies where bible_studies.call_id = calls._id and bible_studies.date_end isnull) <> 0) as 'is_study'");
    	sCallProjectionMap.put(Calls.LAST_VISITED, "coalesce((select return_visits.date from return_visits where return_visits.call_id = calls._id order by return_visits.date desc limit 1),calls.date) as 'last_visited'");
    	
    	sRVProjectionMap = new HashMap<String, String>();
    	sRVProjectionMap.put(ReturnVisits._ID, ReturnVisits._ID);
    	sRVProjectionMap.put(ReturnVisits.DATE, ReturnVisits.DATE);
    	sRVProjectionMap.put(ReturnVisits.CALL_ID, ReturnVisits.CALL_ID);
    	
    	sLiteratureProjectionMap = new HashMap<String, String>();
    	sLiteratureProjectionMap.put(Literature._ID, Literature._ID);
    	sLiteratureProjectionMap.put(Literature.TYPE, Literature.TYPE);
    	sLiteratureProjectionMap.put(Literature.TITLE, Literature.TITLE);
    	sLiteratureProjectionMap.put(Literature.PUBLICATION, LITERATURE_TABLE + "." + Literature.PUBLICATION);
    	
    	sPlacementProjectionMap = new HashMap<String, String>();
    	sPlacementProjectionMap.put(Placements._ID, PLACEMENTS_TABLE + "." + Placements._ID);
    	sPlacementProjectionMap.put(Placements.DATE, PLACEMENTS_TABLE + "." + Placements.DATE);
    	sPlacementProjectionMap.put(Placements.CALL_ID, PLACEMENTS_TABLE + "." + Placements.CALL_ID);
    	sPlacementProjectionMap.put(Placements.LITERATURE_ID, PLACEMENTS_TABLE + "." + Placements.LITERATURE_ID);
    	sPlacementProjectionMap.put(Literature.PUBLICATION, LITERATURE_TABLE + "." + Literature.PUBLICATION);
    	sPlacementProjectionMap.put(Literature.TYPE, LITERATURE_TABLE + "." + Literature.TYPE);
    	
    	sBibleStudyProjectionMap = new HashMap<String, String>();
    	sBibleStudyProjectionMap.put(BibleStudies._ID, BibleStudies._ID);
    	sBibleStudyProjectionMap.put(BibleStudies.DATE_START, BibleStudies.DATE_START);
    	sBibleStudyProjectionMap.put(BibleStudies.DATE_END, BibleStudies.DATE_END);
    	sBibleStudyProjectionMap.put(BibleStudies.CALL_ID, BibleStudies.CALL_ID);
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

		protected Context mContext;
    	
    	public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table " + TIME_ENTRIES_TABLE + " (" 
			    + TimeEntries._ID + " integer primary key autoincrement,"
			    + TimeEntries.LENGTH + " integer,"
			    + TimeEntries.DATE + " date not null default current_timestamp);");
			
			db.execSQL("create table " +  CALLS_TABLE + " (" 
				+ Calls._ID + " integer primary key autoincrement,"
			    + Calls.NAME + " varchar(128),"
			    + Calls.ADDRESS + " varchar(128),"
			    + Calls.LOCATION + " varchar(128),"
			    + Calls.DATE + " date default current_timestamp,"
			    + Calls.NOTES + " text,"
			    + Calls.TYPE + " integer default 1 );");
			
			db.execSQL("create table " +  BIBLE_STUDIES_TABLE + " (" 
					+ BibleStudies._ID + " integer primary key autoincrement,"
				    + BibleStudies.DATE_START + " date default current_timestamp,"
				    + BibleStudies.DATE_END + " date,"
				    + BibleStudies.CALL_ID + " integer references calls(id) );");
			
			db.execSQL("create table " +  RETURN_VISITS_TABLE + " (" 
				+ ReturnVisits._ID + " integer primary key autoincrement,"
			    + ReturnVisits.DATE + " date default current_timestamp,"
			    + ReturnVisits.CALL_ID + " integer references calls(id) )");
			
			db.execSQL("create table " +  LITERATURE_TABLE + " (" 
				+ Literature._ID + " integer primary key autoincrement,"
				+ Literature.TYPE + " integer default 0,"
				+ Literature.PUBLICATION + " varchar(256),"
			    + Literature.TITLE + " varchar(256))");
			
			db.execSQL("create table " +  PLACEMENTS_TABLE + " (" 
				+ Placements._ID + " integer primary key autoincrement,"
			    + Placements.DATE + " date default current_timestamp,"
			    + Placements.CALL_ID + " integer references calls(id),"
			    + Placements.LITERATURE_ID + " integer references literature(id))");
			
			//some default books
			ContentValues values = new ContentValues();
			values.put(Literature.TYPE, Literature.TYPE_BOOK);
			values.put(Literature.PUBLICATION, mContext.getString(R.string.bible));
			db.insert(LITERATURE_TABLE, Literature.TITLE, values);
			
			values = new ContentValues();
			values.put(Literature.TYPE, Literature.TYPE_BOOK);
			values.put(Literature.PUBLICATION, mContext.getString(R.string.bible_teach));
			db.insert(LITERATURE_TABLE, Literature.TITLE, values);
			
			//some default brochures
			
			values = new ContentValues();
			values.put(Literature.TYPE, Literature.TYPE_BROCHURE);
			values.put(Literature.PUBLICATION, mContext.getString(R.string.require_brochure));
			db.insert(LITERATURE_TABLE, Literature.TITLE, values);
			
			values = new ContentValues();
			values.put(Literature.TYPE, Literature.TYPE_BROCHURE);
			values.put(Literature.PUBLICATION, mContext.getString(R.string.comfort_brochure));
			db.insert(LITERATURE_TABLE, Literature.TITLE, values);
			
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
			Log.d(TAG, "Upgrading database from version " + oldVersion);
            
			switch (oldVersion) {
			
			case 2:
				db.execSQL("alter table " + CALLS_TABLE + " add column " + Calls.LOCATION + " varchar(128)");
				break;
				
			case 1:
				db.execSQL("alter table " + CALLS_TABLE + " add column " + Calls.TYPE + " integer default 1");
				break;
				
			default:
				Log.e(TAG, "Failed updating database to new version: no upgrade SQL exists.");
				break;
			
			}
			
			
		}
    	
    }
    
    private DatabaseHelper mDbHelper;
    
    @Override
    public boolean onCreate() {
    	mDbHelper = new DatabaseHelper(getContext());
    	return true;
    }

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRIES:
			count = db.delete(TIME_ENTRIES_TABLE, where, whereArgs);
			break;
		case TIME_ENTRY_ID:
			String tId = uri.getPathSegments().get(1);
			count = db.delete(TIME_ENTRIES_TABLE, TimeEntries._ID + "=" + tId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
		case CALLS:
			count = db.delete(CALLS_TABLE, where, whereArgs);
			break;
		case CALLS_ID:
			String callId = uri.getPathSegments().get(1);
			count = db.delete(CALLS_TABLE, Calls._ID + "=" + callId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
			
		case RETURN_VISITS:
			count = db.delete(RETURN_VISITS_TABLE, where, whereArgs);
			break;
		case RETURN_VISITS_ID:
			String rvId = uri.getPathSegments().get(1);
			count = db.delete(RETURN_VISITS_TABLE, Calls._ID + "=" + rvId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
			
		case PLACEMENTS:
			count = db.delete(PLACEMENTS_TABLE, where, whereArgs);
			break;
		case PLACEMENTS_ID:
			String placementId = uri.getPathSegments().get(1);
			count = db.delete(PLACEMENTS_TABLE, Placements._ID + "=" + placementId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
			
		case LITERATURE:
			count = db.delete(LITERATURE_TABLE, where, whereArgs);
			break;
		case LITERATURE_ID:
			String litId = uri.getPathSegments().get(1);
			count = db.delete(LITERATURE_TABLE, Literature._ID + "=" + litId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
			
		case BIBLE_STUDIES:
			count = db.delete(BIBLE_STUDIES_TABLE, where, whereArgs);
			break;
		case BIBLE_STUDIES_ID:
			String bsId = uri.getPathSegments().get(1);
			count = db.delete(BIBLE_STUDIES_TABLE, BibleStudies._ID + "=" + bsId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRIES:
			return TimeEntries.CONTENT_TYPE;
		case TIME_ENTRY_ID:
			return TimeEntries.CONTENT_ITEM_TYPE;
		case CALLS:
			return Calls.CONTENT_TYPE;
		case CALLS_ID:
			return Calls.CONTENT_ITEM_TYPE;
			
		case RETURN_VISITS:
			return ReturnVisits.CONTENT_TYPE;
		case RETURN_VISITS_ID:
			return ReturnVisits.CONTENT_ITEM_TYPE;
			
		case PLACEMENTS:
			return Placements.CONTENT_TYPE;
		case PLACEMENTS_ID:
			return Placements.CONTENT_ITEM_TYPE;
			
		case LITERATURE:
			return Literature.CONTENT_TYPE;
		case LITERATURE_ID:
			return Literature.CONTENT_ITEM_TYPE;
			
		case BIBLE_STUDIES:
			return BibleStudies.CONTENT_TYPE;
		case BIBLE_STUDIES_ID:
			return BibleStudies.CONTENT_ITEM_TYPE;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		//do some table specific setup
		
		String tableName;
		String nullColumn;
		Uri contentUri;
		
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRIES:
			tableName = TIME_ENTRIES_TABLE;
			nullColumn = TimeEntries.LENGTH;
			contentUri = TimeEntries.CONTENT_URI;
			break;
		case CALLS:
			tableName = CALLS_TABLE;
			nullColumn = Calls.NAME;
			contentUri = Calls.CONTENT_URI;
			break;
		case RETURN_VISITS:
			tableName = RETURN_VISITS_TABLE;
			nullColumn = ReturnVisits.DATE;
			contentUri = ReturnVisits.CONTENT_URI;
			break;
			
		case PLACEMENTS:
			tableName = PLACEMENTS_TABLE;
			nullColumn = Placements.LITERATURE_ID;
			contentUri = Placements.CONTENT_URI;
			break;
			
		case LITERATURE:
			tableName = LITERATURE_TABLE;
			nullColumn = Literature.TITLE;
			contentUri = Literature.CONTENT_URI;
			break;
			
		case BIBLE_STUDIES:
			tableName = BIBLE_STUDIES_TABLE;
			nullColumn = BibleStudies.DATE_END;
			contentUri = BibleStudies.CONTENT_URI;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI + "+uri);
		}
		
		
		ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
		
		//the inserting is the same across the board
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		long rowId = db.insert(tableName, nullColumn, values);
		if(rowId > 0) {
			//insert worked
			Uri insertedUri = ContentUris.withAppendedId(contentUri, rowId);
			getContext().getContentResolver().notifyChange(insertedUri, null);
			return insertedUri;
		}
		
		//insert failed
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,	String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		//setup for various tables
		
		String orderBy = null;
		String groupBy = null;
		
		if(!TextUtils.isEmpty(sortOrder)) {
			orderBy = sortOrder;
		}
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRY_ID:
			qb.appendWhere(TimeEntries._ID + "=" + uri.getPathSegments().get(1));
			//falls through
		case TIME_ENTRIES:
			qb.setTables(TIME_ENTRIES_TABLE);
			qb.setProjectionMap(sTimeProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = TimeEntries.DEFAULT_SORT_ORDER;
			}
			break;
		case CALLS_ID:
			qb.appendWhere(Calls._ID + "=" + uri.getPathSegments().get(1));
			//falls through
		case CALLS:
			qb.setTables(CALLS_TABLE);
			qb.setProjectionMap(sCallProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = Calls.DEFAULT_SORT_ORDER;
			}
			break;
			
		case RETURN_VISITS_ID:
			qb.appendWhere(ReturnVisits._ID + "=" + uri.getPathSegments().get(1));
			//falls through
		case RETURN_VISITS:
			qb.setTables(RETURN_VISITS_TABLE);
			qb.setProjectionMap(sRVProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = ReturnVisits.DEFAULT_SORT_ORDER;
			}
			break;
			
		case PLACEMENTS_ID:
			qb.appendWhere(Placements._ID + "=" + uri.getPathSegments().get(1));
			//falls through
		case PLACEMENTS:
			qb.setTables(PLACEMENTS_TABLE);
			qb.setProjectionMap(sPlacementProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = Placements.DEFAULT_SORT_ORDER;
			}
			break;
			
		case LITERATURE_ID:
			qb.appendWhere(Literature._ID + "=" + uri.getPathSegments().get(1));
			//falls through
		case LITERATURE:
			qb.setTables(LITERATURE_TABLE);
			qb.setProjectionMap(sLiteratureProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = Literature.DEFAULT_SORT_ORDER;
			}
			break;
			
		case BIBLE_STUDIES_ID:
			qb.appendWhere(BibleStudies._ID + "=" + uri.getPathSegments().get(1));
			//falls through
		case BIBLE_STUDIES:
			qb.setTables(BIBLE_STUDIES_TABLE);
			qb.setProjectionMap(sBibleStudyProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = BibleStudies.DEFAULT_SORT_ORDER;
			}
			
			groupBy = BibleStudies.CALL_ID;
			break;
			
		case PLACED_MAGAZINES:
			qb.setTables(PLACEMENTS_TABLE + " INNER JOIN " + LITERATURE_TABLE + " ON (" 
					+ PLACEMENTS_TABLE +"."+ Placements.LITERATURE_ID +" = "+LITERATURE_TABLE+"." + Literature._ID + " AND " 
					+ LITERATURE_TABLE + "." + Literature.TYPE + "=" + Literature.TYPE_MAGAZINE +")");
			qb.setProjectionMap(sPlacementProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = PLACEMENTS_TABLE + "."+ Placements.DEFAULT_SORT_ORDER;
			}
			break;
			
		case PLACED_BROCHURES:
			qb.setTables(PLACEMENTS_TABLE + " INNER JOIN " + LITERATURE_TABLE + " ON (" 
					+ PLACEMENTS_TABLE +"."+ Placements.LITERATURE_ID +" = "+LITERATURE_TABLE+"." + Literature._ID + " AND " 
					+ LITERATURE_TABLE + "." + Literature.TYPE + "=" + Literature.TYPE_BROCHURE +")");
			qb.setProjectionMap(sPlacementProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = PLACEMENTS_TABLE + "."+ Placements.DEFAULT_SORT_ORDER;
			}
			break;
			
		case PLACED_BOOKS:
			qb.setTables(PLACEMENTS_TABLE + " INNER JOIN " + LITERATURE_TABLE + " ON (" 
					+ PLACEMENTS_TABLE +"."+ Placements.LITERATURE_ID +" = "+LITERATURE_TABLE+"." + Literature._ID + " AND " 
					+ LITERATURE_TABLE + "." + Literature.TYPE + "=" + Literature.TYPE_BOOK +")");
			qb.setProjectionMap(sPlacementProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = PLACEMENTS_TABLE + "."+ Placements.DEFAULT_SORT_ORDER;
			}
			break;
			
		case PLACEMENTS_DETAILS_ID:
			qb.appendWhere(PLACEMENTS_TABLE+"."+Placements._ID + "=" + uri.getPathSegments().get(2));
			//falls through
		case PLACEMENTS_DETAILS:
			qb.setTables(PLACEMENTS_TABLE + " INNER JOIN " + LITERATURE_TABLE + " ON (" 
					+ PLACEMENTS_TABLE +"."+ Placements.LITERATURE_ID +" = "+LITERATURE_TABLE+"." + Literature._ID + ")");
			qb.setProjectionMap(sPlacementProjectionMap);
			if(TextUtils.isEmpty(orderBy)) {
				orderBy = PLACEMENTS_TABLE + "."+ Placements.DEFAULT_SORT_ORDER;
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		//standard query stuff
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy, null, orderBy);
		
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		switch(sUriMatcher.match(uri)) {
		case TIME_ENTRIES:
			count = db.update(TIME_ENTRIES_TABLE, values, where, whereArgs);
			break;
		case TIME_ENTRY_ID:
			String tId = uri.getPathSegments().get(1);
			count = db.update(TIME_ENTRIES_TABLE, values, TimeEntries._ID + "=" + tId + (!TextUtils.isEmpty(where) ? " AND ( " + where + ")" : ""), whereArgs);
			break;
		case CALLS:
			count = db.update(CALLS_TABLE, values, where, whereArgs);
			break;
		case CALLS_ID:
			String callId = uri.getPathSegments().get(1);
			count = db.update(CALLS_TABLE, values, Calls._ID + "=" + callId + (!TextUtils.isEmpty(where) ? " AND ( " + where + " )" : ""), whereArgs);
			break;
		case RETURN_VISITS:
			count = db.update(RETURN_VISITS_TABLE, values, where, whereArgs);
			break;
		case RETURN_VISITS_ID:
			String rvId = uri.getPathSegments().get(1);
			count = db.update(RETURN_VISITS_TABLE, values, ReturnVisits._ID + "=" + rvId + (!TextUtils.isEmpty(where) ? " AND ( " + where + " )" : ""), whereArgs);
			break;
			
		case PLACEMENTS:
			count = db.update(PLACEMENTS_TABLE, values, where, whereArgs);
			break;
		case PLACEMENTS_ID:
			String placementId = uri.getPathSegments().get(1);
			count = db.update(PLACEMENTS_TABLE, values, Placements._ID + "=" + placementId + (!TextUtils.isEmpty(where) ? " AND ( " + where + " )" : ""), whereArgs);
			break;
			
		case LITERATURE:
			count = db.update(LITERATURE_TABLE, values, where, whereArgs);
			break;
		case LITERATURE_ID:
			String litId = uri.getPathSegments().get(1);
			count = db.update(LITERATURE_TABLE, values, Literature._ID + "=" + litId + (!TextUtils.isEmpty(where) ? " AND ( " + where + " )" : ""), whereArgs);
			break;
			
		case BIBLE_STUDIES:
			count = db.update(BIBLE_STUDIES_TABLE, values, where, whereArgs);
			break;
		case BIBLE_STUDIES_ID:
			String bsId = uri.getPathSegments().get(1);
			count = db.update(BIBLE_STUDIES_TABLE, values, BibleStudies._ID + "=" + bsId + (!TextUtils.isEmpty(where) ? " AND ( " + where + " )" : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
