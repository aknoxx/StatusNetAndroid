package at.tuwien.dsg.common;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for RequestProvider
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
        
        public static final String QUALIFIER = "qualifier";
        public static final String ADDRESSED_USER_NAME = "addressedUserName";
        public static final String OPERATION = "operation";
        public static final String SERVICE = "service"; 
        public static final String OPERATION_EXECUTION_STATUS = "operationExecutionStatus";
        public static final String URL = "url";
        public static final String COMPLETE_REQUEST_TEXT = "completeRequestText";
        public static final String TWEET_ID = "tweetId";
        public static final String SENDER_NAME = "senderName";
        public static final String CREATED_AT = "createdAt";        
        public static final String IS_CLOSED_SEQUENCE = "isClosedSequence";      
        public static final String ORDERING = "ordering";   
        public static final String DEPENDENT_ON_TWEETID = "dependentOnTweetId";
        public static final String DEPENDENT_ON_NUMBER = "dependentOnNumber";
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = CREATED_AT + " DESC";
    }
    
    public static final class HashTags implements BaseColumns {
    	
    	// This class cannot be instantiated
        private HashTags() {}

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/hashtags");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        public static final String REQUEST_ID = "requestId";
        public static final String NAME = "name";
    }
    
    public static final class Conditions implements BaseColumns {
    	
    	// This class cannot be instantiated
        private Conditions() {}

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/conditions");
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        public static final String REQUEST_ID = "requestId";
        public static final String USER_NAME = "username";
        public static final String VARIABLE = "variable";
        public static final String VALUE = "value";
    }
    
    public static final class Variables implements BaseColumns {
    	// This class cannot be instantiated
        private Variables() {}

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/variables");
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        public static final String REQUEST_ID = "requestId";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }
}
