package at.tuwien.dsg.common;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;
import at.tuwien.dsg.common.Request.Requests;

import java.util.Date;
import java.util.HashMap;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class RequestProvider extends ContentProvider {

    private static HashMap<String, String> sRequestsProjectionMap;

    private static final int REQUESTS = 1;
    private static final int REQUEST_ID = 2;

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
        qb.setTables(RequestDbAdapter.REQUEST_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case REQUESTS:
            qb.setProjectionMap(sRequestsProjectionMap);
            break;

        case REQUEST_ID:
            qb.setProjectionMap(sRequestsProjectionMap);
            qb.appendWhere(Requests._ID + "=" + uri.getPathSegments().get(1));
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
        sUriMatcher.addURI(Request.AUTHORITY, "requests/#", REQUEST_ID);    

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
        sRequestsProjectionMap.put(Requests.REQUEST_ID, Requests.REQUEST_ID);
        sRequestsProjectionMap.put(Requests.NAME, Requests.NAME);
        sRequestsProjectionMap.put(Requests.USER_NAME, Requests.USER_NAME);
        sRequestsProjectionMap.put(Requests.VARIABLE, Requests.VARIABLE);
        sRequestsProjectionMap.put(Requests.VALUE, Requests.VALUE);
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

