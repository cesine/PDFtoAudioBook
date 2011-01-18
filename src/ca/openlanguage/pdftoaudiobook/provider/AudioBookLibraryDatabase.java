/*

   Copyright 2011 Gina Cook

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */ 
 package ca.openlanguage.pdftoaudiobook.provider;
 

import android.net.Uri;
import android.provider.BaseColumns;





public class AudioBookLibraryDatabase {
    public static final String AUTHORITY = "ca.openlanguage.pdftoaudiobook.provider.AudiobookLibrary";

    // This class cannot be instantiated
    private AudioBookLibraryDatabase() {}
    
    /**
     * audiobooks table
     */
    public static final class AudiobookColumns implements BaseColumns {
        // This class cannot be instantiated
        private AudiobookColumns() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/audiobooks");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of audiobooks.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openlanguage.pdftoaudiobook.audiobook";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single audiobook.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openlanguage.pdftoaudiobook.audiobook";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
    	
		/** string type "Intensional Semantics" */
		public static final String TITLE = "title";
		/** string type "Kai von Fintel, Irene Heim"*/
		public static final String AUTHOR = "author";
		/** string type, convert to list of strings later, "(von Fintel 2004), (von Fintel and Heim)"*/
		public static final String CITATION = "citations";
		/** string type, convert to list of strings later, "(von Fintel 2004), (von Fintel and Heim)"*/
		public static final String CLASSIFICATION = "classifications";
		/** String with possibly multiple publication dates 2002, Spring 2002, 2004, 2007*/
		public static final String PUBLICATION_DATE ="docpubdate";
		/** Integer type from System.currentTimeMillis(), used for ordering documents and expiring old audio files?*/
		public static final String LAST_LISTENED_TIME = "timelastlistenedto";
		/** String of chunks for this document*/
		public static final String CHUNKS = "chunks";
		/** String*/
		public static final String FILENAME = "filename";
		/** String, TODO should probably be a URI*/
		public static final String FULL_FILEPATH_AND_FILENAME ="fullpathandfilename";
		/** String TODO should probably be a URI*/
		public static final String THUMBNAIL = "thumbnail";
		/** String */
		public static final String STARRED= "starrred";
		/** Blob TODOs read section 3.3 and write a quick summary, compare the definitions of xxxx here with those in yyyyy*/
        public static final String TASKNOTES = "tasknotes";

        /**
         * The timestamp for when the audiobook was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the audiobook was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }
}
