package ca.openlanguage.pdftoaudiobook.provider;



import android.net.Uri;
import android.provider.BaseColumns;



public class AudioBookLibraryDatabase {
	public static String AUTHORITY = "ca.openlanguage.pdftoaudiobook.AudiobookLibraryProvider";
    

    private static final String TAG = "AudioBookLibraryDatabase";


    // This class cannot be instantiated
    private AudioBookLibraryDatabase() {
		// TODO Auto-generated constructor stub
	}
    
    /**
     * Documents table
     */
    public static final class DocumentColumns implements BaseColumns {
        // This class cannot be instantiated
        private DocumentColumns() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/documents");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of documents.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openlanguage.pdftoaudiobook.document";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single document.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openlanguage.pdftoaudiobook.document";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "title DESC";

        /**
         * The title of the document
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";

        /**
         * The document itself
         * <P>Type: TEXT</P>
         */
        public static final String DOCUMENT = "document";

        /**
         * The timestamp for when the document was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the document was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }

}
