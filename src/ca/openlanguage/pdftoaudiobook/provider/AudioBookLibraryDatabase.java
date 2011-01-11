package ca.openlanguage.pdftoaudiobook.provider;

import android.net.Uri;
import android.provider.BaseColumns;





public class AudioBookLibraryDatabase {
    public static final String AUTHORITY = "ca.openlanguage.pdftoaudiobook.provider.AudiobookLibrary";

    // This class cannot be instantiated
    private AudioBookLibraryDatabase() {}
    
    /**
     * Notes table
     */
    public static final class NoteColumns implements BaseColumns {
        // This class cannot be instantiated
        private NoteColumns() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notes");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openlanguage.pdftoaudiobook.note";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openlanguage.pdftoaudiobook.note";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";

        /**
         * The note itself
         * <P>Type: TEXT</P>
         */
        public static final String NOTE = "note";

        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the note was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }
}
