package at.tuwien.dsg.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import at.tuwien.dsg.common.Request.Conditions;
import at.tuwien.dsg.common.Request.HashTags;
import at.tuwien.dsg.common.Request.Requests;
import at.tuwien.dsg.common.Request.Variables;
import at.tuwien.dsg.entities.Condition;

public class RequestDbAdapter {

	private static final String TAG = "RequestProvider";

    private static final String DATABASE_NAME = "request.db";
    private static final int DATABASE_VERSION = 2;
    
    public static final String REQUEST_TABLE_NAME = "request";
    public static final String HASHTAG_TABLE_NAME = "hashtag";
    public static final String CONDITION_TABLE_NAME = "condition";
    public static final String VARIABLE_TABLE_NAME = "variable";
    
    private static final String CREATE_TABLE_REQUEST =
		"CREATE TABLE IF NOT EXISTS " + REQUEST_TABLE_NAME + " ("
        + Requests._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + Requests.QUALIFIER + " TEXT,"
        + Requests.ADDRESSED_USER_NAME + " TEXT,"
        + Requests.OPERATION + " TEXT,"
        + Requests.SERVICE + " TEXT,"
        + Requests.URL + " TEXT,"
        + Requests.COMPLETE_REQUEST_TEXT + " TEXT,"
        + Requests.TWEET_ID + " LONG,"
        + Requests.SENDER_NAME + " TEXT,"
        + Requests.CREATED_AT + " LONG"
        + ");";
	
    private static final String CREATE_TABLE_HASHTAG =
		"CREATE TABLE IF NOT EXISTS " + HASHTAG_TABLE_NAME + " ("
        + HashTags._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + HashTags.REQUEST_ID + " LONG,"
        + HashTags.NAME + " TEXT"
        + ");";
	
    private static final String CREATE_TABLE_CONDITION =
		"CREATE TABLE IF NOT EXISTS " + CONDITION_TABLE_NAME + " ("
        + Conditions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + Conditions.REQUEST_ID + " LONG,"
        + Conditions.USER_NAME + " TEXT,"
        + Conditions.VARIABLE + " TEXT,"
        + Conditions.VALUE + " TEXT"
        + ");";
	
    private static final String CREATE_TABLE_VARIABLE =
		"CREATE TABLE IF NOT EXISTS " + VARIABLE_TABLE_NAME + " ("
        + Variables._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + Variables.REQUEST_ID + " LONG,"
        + Variables.NAME + " TEXT,"
        + Variables.VALUE + " TEXT"
        + ");";
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {   	
	        db.execSQL(CREATE_TABLE_REQUEST);
	        db.execSQL(CREATE_TABLE_HASHTAG);
	        db.execSQL(CREATE_TABLE_CONDITION);
	        db.execSQL(CREATE_TABLE_VARIABLE);
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
	        db.execSQL("DROP TABLE IF EXISTS " + REQUEST_TABLE_NAME);
	        db.execSQL("DROP TABLE IF EXISTS " + HASHTAG_TABLE_NAME);
	        db.execSQL("DROP TABLE IF EXISTS " + CONDITION_TABLE_NAME);
	        db.execSQL("DROP TABLE IF EXISTS " + VARIABLE_TABLE_NAME);
	        onCreate(db);
	    }
	}

	private Context mCtx;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	public RequestDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }   
    
    public RequestDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    public SQLiteDatabase getReadableDatabase() {
    	if(mDb != null) {
    		return mDb;
    	}
    	return null;
    }   
	
	public long saveRequest(at.tuwien.dsg.entities.Request request) {
		
		if(request.isSaved()) {
			return request.getDbId();
		}
		
		/**
		 * Insert main Request
		 */
		ContentValues values = new ContentValues();
        values.put(Requests.QUALIFIER, request.getQualifier());
        values.put(Requests.ADDRESSED_USER_NAME, request.getAddressedUser());
        values.put(Requests.OPERATION, request.getOperation());
        values.put(Requests.SERVICE, request.getService());
        values.put(Requests.URL, request.getUrl());
        values.put(Requests.COMPLETE_REQUEST_TEXT, request.getCompleteRequestText());
        values.put(Requests.TWEET_ID, request.getTweetId());
        values.put(Requests.SENDER_NAME, request.getRequester());
        values.put(Requests.CREATED_AT, request.getCreatedAt().getTime());
        
        long requestId;
        if((requestId = mDb.insert(REQUEST_TABLE_NAME, null, values)) < 0) {
        	return -1;
        }
        
        /**
         * Insert Hashtags
         */
        for (String ht : request.getHashTags()) {
        	values.clear();
			values.put(HashTags.REQUEST_ID, requestId);
			values.put(HashTags.NAME, ht);
			if(mDb.insert(HASHTAG_TABLE_NAME, null, values) < 0) {
				return -1;
			}
		}
        
        /**
         * Insert Condition
         */
        Condition c;
        if((c = request.getCondition()) != null) {        	
        	values.clear();
        	values.put(Conditions.REQUEST_ID, requestId);
        	values.put(Conditions.USER_NAME, c.getUsername());
        	values.put(Conditions.VARIABLE, c.getVariable());
        	values.put(Conditions.VALUE, c.getValue());
			if(mDb.insert(CONDITION_TABLE_NAME, null, values) < 0) {
				return -1;
			}
		}
        
        /**
         * Insert Variables
         */
        for (Iterator iter = request.getVariables().entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            values.clear();
			values.put(Variables.REQUEST_ID, requestId);
			values.put(Variables.NAME, (String)entry.getKey());
			values.put(Variables.VALUE, (String)entry.getValue());
			if(mDb.insert(VARIABLE_TABLE_NAME, null, values) < 0) {
				return -1;
			}
        }
        
        return requestId;
    }
	
	public List<at.tuwien.dsg.entities.Request> loadAllRequests() {
		List<at.tuwien.dsg.entities.Request> requests = new ArrayList<at.tuwien.dsg.entities.Request>();
		
		String[] requestSelection = 
			new String[] { 
				Requests._ID,
				Requests.QUALIFIER,
				Requests.ADDRESSED_USER_NAME,
				Requests.OPERATION,
				Requests.SERVICE,
				Requests.URL,
				Requests.COMPLETE_REQUEST_TEXT,
				Requests.TWEET_ID,
				Requests.SENDER_NAME,
				Requests.CREATED_AT
			};
		
		String[] hashtagSelection = 
			new String[] { 
				HashTags.REQUEST_ID,
				HashTags.NAME
			};
		
		String[] conditionSelection = 
			new String[] { 
				Conditions.REQUEST_ID,
				Conditions.USER_NAME,
				Conditions.VARIABLE,
				Conditions.VALUE
			};
		
		String[] variableSelection = 
			new String[] { 
				Variables.REQUEST_ID,
				Variables.NAME,
				Variables.VALUE
			};
		
		Cursor rc =  mDb.query(REQUEST_TABLE_NAME, 
				requestSelection, null, null, null, null, null);

		if(rc != null) {
			if(rc.getCount() > 0) {
			
				int idColumn = rc.getColumnIndex(Requests._ID);
				int qualifierColumn = rc.getColumnIndex(Requests.QUALIFIER);
				int addressedUserNameColumn = rc.getColumnIndex(Requests.ADDRESSED_USER_NAME);
				int operationColumn = rc.getColumnIndex(Requests.OPERATION);
				int serviceColumn = rc.getColumnIndex(Requests.SERVICE);
				int urlColumn = rc.getColumnIndex(Requests.URL);
				int completeRequestTextColumn = rc.getColumnIndex(Requests.COMPLETE_REQUEST_TEXT);
				int tweetIdColumn = rc.getColumnIndex(Requests.TWEET_ID);
				int senderNameColumn = rc.getColumnIndex(Requests.SENDER_NAME);
				int createdAtColumn = rc.getColumnIndex(Requests.CREATED_AT);
				
				rc.moveToFirst();
				while (rc.isAfterLast() == false) {
					at.tuwien.dsg.entities.Request r = new at.tuwien.dsg.entities.Request();
					r.setAddressedUser(rc.getString(addressedUserNameColumn));
					r.setCompleteRequestText(rc.getString(completeRequestTextColumn));
					r.setCreatedAt(new Date(rc.getLong(createdAtColumn)));
					r.setOperation(rc.getString(operationColumn));
					r.setService(rc.getString(serviceColumn));
					r.setTweetId(rc.getLong(tweetIdColumn));
					r.setUrl(rc.getString(urlColumn));
					r.setQualifier(rc.getString(qualifierColumn));
					r.setRequester(rc.getString(senderNameColumn));
					
					r.setSaved(true);
					r.setDbId(rc.getLong(idColumn));
					
					Cursor htc =  mDb.query(HASHTAG_TABLE_NAME, 
							hashtagSelection, null, null, null, null, null);
					
					if(htc != null) {
						if(htc.getCount() > 0) {
							htc.moveToFirst();
							
							int nameColumn = htc.getColumnIndex(HashTags.NAME);
							
							List<String> hashTags = new ArrayList<String>();
							
							for(int j=0; j<htc.getCount(); j++) {
								hashTags.add(htc.getString(nameColumn));
							}
							r.setHashTags(hashTags);
						}
					}
					
					Cursor cc =  mDb.query(CONDITION_TABLE_NAME, 
							conditionSelection, null, null, null, null, null);
					
					if(cc != null) {
						if(cc.getCount() > 0) {
							cc.moveToFirst();
							
							int usernameColumn = cc.getColumnIndex(Conditions.USER_NAME);
							int variableColumn = cc.getColumnIndex(Conditions.VARIABLE);
							int valueColumn = cc.getColumnIndex(Conditions.VALUE);
		
							r.setCondition(new Condition(
									cc.getString(usernameColumn), 
									cc.getString(variableColumn), 
									cc.getString(valueColumn)
									));
						}
					}
					
					Cursor vc =  mDb.query(VARIABLE_TABLE_NAME, 
							variableSelection, null, null, null, null, null);
					
					if(vc != null) {
						if(vc.getCount() > 0) {
							vc.moveToFirst();
							
							int nameColumn = vc.getColumnIndex(Variables.NAME);
							int valueColumn = vc.getColumnIndex(Variables.VALUE);
							
							for(int j=0; j<vc.getCount(); j++) {
								r.getVariables().put(vc.getString(nameColumn), 
										vc.getString(valueColumn));
							}
						}
					}
					requests.add(r);
					rc.moveToNext();
				}
				rc.close();
				return requests;
			}
		}
		return null;
	}
	
	public boolean deleteRequest(long requestId) {
		mDb.delete(HASHTAG_TABLE_NAME, 
    			HashTags.REQUEST_ID + "=" + requestId, null);
		mDb.delete(CONDITION_TABLE_NAME, 
    			Conditions.REQUEST_ID + "=" + requestId, null);
		mDb.delete(VARIABLE_TABLE_NAME, 
				Variables.REQUEST_ID + "=" + requestId, null);
		return mDb.delete(REQUEST_TABLE_NAME, 
    			Requests._ID + "=" + requestId, null) > 0;
	}
	
	public void clearDb() {
		mDb.execSQL("DELETE FROM " + REQUEST_TABLE_NAME);
		mDb.execSQL("DELETE FROM " + HASHTAG_TABLE_NAME);
		mDb.execSQL("DELETE FROM " + CONDITION_TABLE_NAME);
		mDb.execSQL("DELETE FROM " + VARIABLE_TABLE_NAME);
	}
}
