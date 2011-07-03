package at.tuwien.dsg.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import at.tuwien.dsg.entities.Network;

public class RequestDbAdapter {

	private static final String TAG = "RequestProvider";

    private static final String DATABASE_NAME = "request.db";
    private static final int DATABASE_VERSION = 2;
    
    public static final String REQUEST_TABLE_NAME = "request";
    public static final String HASHTAG_TABLE_NAME = "hashtag";
    public static final String CONDITION_TABLE_NAME = "condition";
    public static final String VARIABLE_TABLE_NAME = "variable";
    
    public static final String NETWORK_TABLE_NAME = "network";
    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String BASE_URL = "baseurl";
    public static final String CONSUMER_KEY = "consumerkey";
    public static final String CONSUMER_SECRET = "consumersecret";
    
    private static final String CREATE_TABLE_REQUEST =
		"CREATE TABLE IF NOT EXISTS " + REQUEST_TABLE_NAME + " ("
        + Requests._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + Requests.QUALIFIER + " TEXT,"
        + Requests.ADDRESSED_USER_NAME + " TEXT,"
        + Requests.OPERATION + " TEXT,"
        + Requests.OPERATION_EXECUTION_STATUS + " TEXT,"
        + Requests.SERVICE + " TEXT,"
        + Requests.URL + " TEXT,"
        + Requests.COMPLETE_REQUEST_TEXT + " TEXT,"
        + Requests.TWEET_ID + " LONG,"
        + Requests.SENDER_NAME + " TEXT,"
        + Requests.CREATED_AT + " LONG,"
        + Requests.DEPENDENT_ON_TWEETID  +" LONG"
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
    
    private static final String CREATE_TABLE_NETWORK =
		"CREATE TABLE IF NOT EXISTS " + NETWORK_TABLE_NAME + " ("
        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + NAME + " TEXT,"
        + BASE_URL + " TEXT,"
        + CONSUMER_KEY + " TEXT,"
        + CONSUMER_SECRET + " TEXT"
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
	        
	        db.execSQL(CREATE_TABLE_NETWORK);	        
	        
	        ContentValues values = new ContentValues();
	        values.put(NAME, "status.net");
	        values.put(BASE_URL, "http://192.168.0.10/statusnet/index.php/api/");
	        values.put(CONSUMER_KEY, "d627a2882d5e28dc5835a92f1e46760e");
	        values.put(CONSUMER_SECRET, "84ee86ede714b965344186c5dd74d330");
	        db.insert(NETWORK_TABLE_NAME, null, values);
	        
	        values = new ContentValues();
	        values.put(NAME, "identi.ca");
	        values.put(BASE_URL, "http://identi.ca/api/");
	        values.put(CONSUMER_KEY, "9a74ad0a805737218ba3da94a0236b53");
	        values.put(CONSUMER_SECRET, "dc3f43cba9e36cb84725f0f8d654ed6e");
	        db.insert(NETWORK_TABLE_NAME, null, values);
	        
	        values = new ContentValues();
	        values.put(NAME, "twitter.com");
	        values.put(BASE_URL, "http://twitter.com/");
	        values.put(CONSUMER_KEY, "wfRZ0ziRJOS07W9KRmAtLQ");
	        values.put(CONSUMER_SECRET, "PQzIniSepykkKpPQog2a7Se9I0mX0rLasPIgiygaPkE");
	        db.insert(NETWORK_TABLE_NAME, null, values);
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
	        db.execSQL("DROP TABLE IF EXISTS " + REQUEST_TABLE_NAME);
	        db.execSQL("DROP TABLE IF EXISTS " + HASHTAG_TABLE_NAME);
	        db.execSQL("DROP TABLE IF EXISTS " + CONDITION_TABLE_NAME);
	        db.execSQL("DROP TABLE IF EXISTS " + VARIABLE_TABLE_NAME);
	        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_NETWORK);
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
        values.put(Requests.OPERATION_EXECUTION_STATUS, request.getOperationExecutionStatus());
        values.put(Requests.SERVICE, request.getService());
        values.put(Requests.URL, request.getUrl());
        values.put(Requests.COMPLETE_REQUEST_TEXT, request.getCompleteRequestText());
        values.put(Requests.TWEET_ID, request.getTweetId());
        values.put(Requests.SENDER_NAME, request.getRequester());
        values.put(Requests.CREATED_AT, request.getCreatedAt().getTime());        
        values.put(Requests.DEPENDENT_ON_TWEETID, request.getDependentOnTweetId());
        
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
	
	public List<Network> loadAllNetworks() {
		String[] networkSelection = 
			new String[] { 
				NAME,
				BASE_URL,
				CONSUMER_KEY,
				CONSUMER_SECRET
			};
		
		Cursor c =  mDb.query(NETWORK_TABLE_NAME, 
				networkSelection, null, null, null, null, null);

		if(c != null) {
			if(c.getCount() > 0) {
			
				List<Network> networks = new ArrayList<Network>();
				
				int nameColumn = c.getColumnIndex(NAME);
				int baseUrlColumn = c.getColumnIndex(BASE_URL);
				int consumerKeyColumn = c.getColumnIndex(CONSUMER_KEY);
				int consumerSecretColumn = c.getColumnIndex(CONSUMER_SECRET);
				
				c.moveToFirst();
				while (c.isAfterLast() == false) {
					Network network = new Network(
								c.getString(nameColumn),
								c.getString(baseUrlColumn), 
								c.getString(consumerKeyColumn), 
								c.getString(consumerSecretColumn)
								);
					
					networks.add(network);
					c.moveToNext();					
				}
				c.close();
				return networks;
			}
		}
		return null;
	}
	
	public List<at.tuwien.dsg.entities.Request> loadAllRequests() {
		List<at.tuwien.dsg.entities.Request> requests = new ArrayList<at.tuwien.dsg.entities.Request>();
		
		String[] requestSelection = 
			new String[] { 
				Requests._ID,
				Requests.QUALIFIER,
				Requests.ADDRESSED_USER_NAME,
				Requests.OPERATION,
				Requests.OPERATION_EXECUTION_STATUS,
				Requests.SERVICE,
				Requests.URL,
				Requests.COMPLETE_REQUEST_TEXT,
				Requests.TWEET_ID,
				Requests.SENDER_NAME,
				Requests.CREATED_AT,
				Requests.DEPENDENT_ON_TWEETID
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
				int operationExecutionStatusColumn = rc.getColumnIndex(Requests.OPERATION_EXECUTION_STATUS);
				int serviceColumn = rc.getColumnIndex(Requests.SERVICE);
				int urlColumn = rc.getColumnIndex(Requests.URL);
				int completeRequestTextColumn = rc.getColumnIndex(Requests.COMPLETE_REQUEST_TEXT);
				int tweetIdColumn = rc.getColumnIndex(Requests.TWEET_ID);
				int senderNameColumn = rc.getColumnIndex(Requests.SENDER_NAME);
				int createdAtColumn = rc.getColumnIndex(Requests.CREATED_AT);
				int dependentOnTweetIdColumn = rc.getColumnIndex(Requests.DEPENDENT_ON_TWEETID);
				
				rc.moveToFirst();
				while (rc.isAfterLast() == false) {
					at.tuwien.dsg.entities.Request r = new at.tuwien.dsg.entities.Request();
					r.setAddressedUser(rc.getString(addressedUserNameColumn));
					r.setCompleteRequestText(rc.getString(completeRequestTextColumn));
					r.setCreatedAt(new Date(rc.getLong(createdAtColumn)));
					r.setOperation(rc.getString(operationColumn));
					r.setOperationExecutionStatus(rc.getString(operationExecutionStatusColumn));
					r.setService(rc.getString(serviceColumn));
					r.setTweetId(rc.getLong(tweetIdColumn));
					r.setUrl(rc.getString(urlColumn));
					r.setQualifier(rc.getString(qualifierColumn));
					r.setRequester(rc.getString(senderNameColumn));
					r.setDependentOnTweetId(rc.getLong(dependentOnTweetIdColumn));
					
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
