package at.tuwien.dsg.common;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 */
/**
 * This class tests the content provider for the Note Pad sample application.
 *
 * To learn how to run an entire test package or one of its classes, please see
 * "Testing in Eclipse, with ADT" or "Testing in Other IDEs" in the Developer Guide.
 */
public class RequestProviderTest extends ProviderTestCase2<RequestProvider> {

    // A URI that the provider does not offer, for testing error handling.
    private static final Uri INVALID_URI =
        Uri.withAppendedPath(Request.Requests.CONTENT_URI, "invalid");

    // Contains a reference to the mocked content resolver for the provider under test.
    private MockContentResolver mMockResolver;

    // Contains an SQLite database, used as test data
    private SQLiteDatabase mDb;

    // Contains the test data, as an array of NoteInfo instances.
    /*private final NoteInfo[] TEST_NOTES = {
        new NoteInfo("Note0", "This is note 0"),
        new NoteInfo("Note1", "This is note 1"),
        new NoteInfo("Note2", "This is note 2"),
        new NoteInfo("Note3", "This is note 3"),
        new NoteInfo("Note4", "This is note 4"),
        new NoteInfo("Note5", "This is note 5"),
        new NoteInfo("Note6", "This is note 6"),
        new NoteInfo("Note7", "This is note 7"),
        new NoteInfo("Note8", "This is note 8"),
        new NoteInfo("Note9", "This is note 9") };*/

    // Number of milliseconds in one day (milliseconds * seconds * minutes * hours)
    private static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;

    // Number of milliseconds in one week
    private static final long ONE_WEEK_MILLIS = ONE_DAY_MILLIS * 7;

    // Creates a calendar object equal to January 1, 2010 at 12 midnight
    private static final GregorianCalendar TEST_CALENDAR =
        new GregorianCalendar(2010, Calendar.JANUARY, 1, 0, 0, 0);

    // Stores a timestamp value, set to an arbitrary starting point
    private final static long START_DATE = TEST_CALENDAR.getTimeInMillis();

    // Sets a MIME type filter, used to test provider methods that return more than one MIME type
    // for a particular note. The filter will retrieve any MIME types supported for the content URI.
    private final static String MIME_TYPES_ALL = "*/*";

    // Sets a MIME type filter, used to test provider methods that return more than one MIME type
    // for a particular note. The filter is nonsense, so it will not retrieve any MIME types.
    private final static String MIME_TYPES_NONE = "qwer/qwer";

    // Sets a MIME type filter for plain text, used to the provider's methods that only handle
    // plain text
    private final static String MIME_TYPE_TEXT = "text/plain";

    /*
     * Constructor for the test case class.
     * Calls the super constructor with the class name of the provider under test and the
     * authority name of the provider.
     */
    public RequestProviderTest() {
        super(RequestProvider.class, Request.AUTHORITY);
    }

    /*
     * Sets up the test environment before each test method. Creates a mock content resolver,
     * gets the provider under test, and creates a new database for the provider.
     */
    @Override
    protected void setUp() throws Exception {
        // Calls the base class implementation of this method.
        super.setUp();

        // Gets the resolver for this test.
        mMockResolver = getMockContentResolver();

        /*
         * Gets a handle to the database underlying the provider. Gets the provider instance
         * created in super.setUp(), gets the DatabaseOpenHelper for the provider, and gets
         * a database object from the helper.
         */
        //mMockResolver.get
        //mDb = getProvider().getOpenHelperForTest().getWritableDatabase();
    }

    /*
     *  This method is called after each test method, to clean up the current fixture. Since
     *  this sample test case runs in an isolated context, no cleanup is necessary.
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
