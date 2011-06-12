package at.tuwien.dsg.common;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class Request {
    public static final String AUTHORITY = "at.tuwien.dsg.provider.RequestProvider";

    // This class cannot be instantiated
    private Request() {}
    
    /**
     * Requests table
     */
    public static final class Requests implements BaseColumns {
        // This class cannot be instantiated
        private Requests() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/requests");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        /**
         * The qualifier of the request
         * <P>Type: TEXT</P>
         */
        public static final String QUALIFIER = "qualifier";

        /**
         * The qualifier of the request
         * <P>Type: TEXT</P>
         */
        public static final String ADDRESSED_USER_NAME = "addressedUserName";
        
        /**
         * The qualifier of the request
         * <P>Type: TEXT</P>
         */
        public static final String OPERATION = "operation";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String SERVICE = "service";
        
        /**
         * The qualifier of the request
         * <P>Type: TEXT</P>
         */
        public static final String URL = "url";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String COMPLETE_REQUEST_TEXT = "completeRequestText";
        
        /**
         * The qualifier of the request
         * <P>Type: TEXT</P>
         */
        public static final String TWEET_ID = "tweetId";

        /**
         * The qualifier of the request
         * <P>Type: TEXT</P>
         */
        public static final String SENDER_NAME = "senderName";
        
        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_AT = "createdAt";
    }
    
    public static final class HashTags implements BaseColumns {
    	
    	// This class cannot be instantiated
        private HashTags() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/hashtags");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String REQUEST_ID = "requestId";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";
    }
    
    public static final class Conditions implements BaseColumns {
    	
    	// This class cannot be instantiated
        private Conditions() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/conditions");

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String REQUEST_ID = "requestId";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String USER_NAME = "username";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String VARIABLE = "variable";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String VALUE = "value";
    }
    
    public static final class Variables implements BaseColumns {
    	// This class cannot be instantiated
        private Variables() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/variables");


        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String REQUEST_ID = "requestId";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";
        
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String VALUE = "value";
    }
}
