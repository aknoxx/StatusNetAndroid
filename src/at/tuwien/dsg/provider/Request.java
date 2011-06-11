package at.tuwien.dsg.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class Request {
    /**
     * Notes table
     */
    public static final class Requests implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI
                = Uri.parse("content://at.tuwien.dsg.provider.Request/requests");

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
}
