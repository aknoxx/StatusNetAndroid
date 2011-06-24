package at.tuwien.dsg;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import at.tuwien.dsg.common.Request;
import at.tuwien.dsg.common.Request.Conditions;
import at.tuwien.dsg.common.Request.HashTags;
import at.tuwien.dsg.common.Request.Variables;
import at.tuwien.dsg.common.RequestDbAdapter;
import at.tuwien.dsg.common.Request.Requests;

import java.util.HashMap;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class RequestProvider extends ContentProvider {

    private static HashMap<String, String> sRequestsProjectionMap;
    private static HashMap<String, String> sHashTagsProjectionMap;
    private static HashMap<String, String> sConditionsProjectionMap;
    private static HashMap<String, String> sVariablesProjectionMap;

    private static final int REQUESTS = 1;
    private static final int HASHTAGS = 2;
    private static final int CONDITIONS = 3;
    private static final int VARIABLES = 4;
    
    private static final int REQUEST_ID = 10;

    private static final UriMatcher sUriMatcher;

    private RequestDbAdapter mDbAdapter;

    @Override
    public boolean onCreate() {
    	mDbAdapter = new RequestDbAdapter(getContext());
    	mDbAdapter.open();
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
    	
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        //* For example:
          //  *   setTables("foo, bar")
         //   *   setTables("foo LEFT OUTER JOIN bar ON (foo.id = bar.foo_id)")
        /*qb.setTables(REQUEST_TABLE_NAME + " LEFT OUTER JOIN " + HASHTAG_TABLE_NAME
        		+ " ON (" + REQUEST_TABLE_NAME + "._id = " + HASHTAG_TABLE_NAME + ".requestId)");
        		*/
        

        switch (sUriMatcher.match(uri)) {
	        case REQUESTS:
	        	qb.setTables(RequestDbAdapter.REQUEST_TABLE_NAME);
	            qb.setProjectionMap(sRequestsProjectionMap);
	            break;
	        case HASHTAGS:
	        	qb.setTables(RequestDbAdapter.HASHTAG_TABLE_NAME);
	            qb.setProjectionMap(sHashTagsProjectionMap);
	            break;
	        case CONDITIONS:
	        	qb.setTables(RequestDbAdapter.CONDITION_TABLE_NAME);
	            qb.setProjectionMap(sConditionsProjectionMap);
	            break;
	        case VARIABLES:
	        	qb.setTables(RequestDbAdapter.VARIABLE_TABLE_NAME);
	            qb.setProjectionMap(sVariablesProjectionMap);
	            break;
	
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        //if (TextUtils.isEmpty(sortOrder)) {
            //orderBy = NotePad.Notes.DEFAULT_SORT_ORDER;
        //} else {
            orderBy = sortOrder;
        //}

        // Get the database and run the query
        SQLiteDatabase db = mDbAdapter.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case REQUESTS:
       // case LIVE_FOLDER_NOTES:
       //     return Requests.CONTENT_TYPE;

        case REQUEST_ID:
            return Requests.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
    	return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
    	return 0;
    }

    static {
    	
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Request.AUTHORITY, "requests", REQUESTS);
        sUriMatcher.addURI(Request.AUTHORITY, "hashtags", HASHTAGS);
        sUriMatcher.addURI(Request.AUTHORITY, "conditions", CONDITIONS);
        sUriMatcher.addURI(Request.AUTHORITY, "variables", VARIABLES);

        sRequestsProjectionMap = new HashMap<String, String>();
        sRequestsProjectionMap.put(Requests._ID, Requests._ID);
        sRequestsProjectionMap.put(Requests.QUALIFIER, Requests.QUALIFIER);
        sRequestsProjectionMap.put(Requests.ADDRESSED_USER_NAME, Requests.ADDRESSED_USER_NAME);
        sRequestsProjectionMap.put(Requests.OPERATION, Requests.OPERATION);
        sRequestsProjectionMap.put(Requests.SERVICE, Requests.SERVICE);
        sRequestsProjectionMap.put(Requests.URL, Requests.URL);
        sRequestsProjectionMap.put(Requests.COMPLETE_REQUEST_TEXT, Requests.COMPLETE_REQUEST_TEXT);
        sRequestsProjectionMap.put(Requests.TWEET_ID, Requests.TWEET_ID);
        sRequestsProjectionMap.put(Requests.SENDER_NAME, Requests.SENDER_NAME);
        sRequestsProjectionMap.put(Requests.CREATED_AT, Requests.CREATED_AT);

        sHashTagsProjectionMap = new HashMap<String, String>();
        sHashTagsProjectionMap.put(HashTags._ID, HashTags._ID);
        sHashTagsProjectionMap.put(HashTags.REQUEST_ID, HashTags.REQUEST_ID);
        sHashTagsProjectionMap.put(HashTags.NAME, HashTags.NAME);
        
        sConditionsProjectionMap = new HashMap<String, String>();
        sConditionsProjectionMap.put(Conditions._ID, Requests._ID);
        sConditionsProjectionMap.put(Conditions.REQUEST_ID, Conditions.REQUEST_ID);
        sConditionsProjectionMap.put(Conditions.USER_NAME, Conditions.USER_NAME);
        sConditionsProjectionMap.put(Conditions.VARIABLE, Conditions.VARIABLE);
        sConditionsProjectionMap.put(Conditions.VALUE, Conditions.VALUE);
        
        sVariablesProjectionMap = new HashMap<String, String>();
        sVariablesProjectionMap.put(Variables._ID, Variables._ID);
        sVariablesProjectionMap.put(Variables.REQUEST_ID, Variables.REQUEST_ID);
        sVariablesProjectionMap.put(Variables.NAME, Variables.NAME);
        sVariablesProjectionMap.put(Variables.VALUE, Variables.VALUE);
    }
    
    /**
     * A test package can call this to get a handle to the database underlying NotePadProvider,
     * so it can insert test data into the database. The test case class is responsible for
     * instantiating the provider in a test context; {@link android.test.ProviderTestCase2} does
     * this during the call to setUp()
     *
     * @return a handle to the database helper object for the provider's data.
     */
    /*DatabaseHelper getOpenHelperForTest() {
        return mDbHelper;
    }*/
}

