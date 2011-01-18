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

import java.util.regex.Pattern;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;


/** 
 * Contract class for interacting with the AudioBook Provider (based on best practices in ioschedule 2010)
 * 
 * 1: sets up interfaces for columns
 * 2: these interfaces are used by classes (also declared in this file) which add some functionality and methods mostly to use URIs to refer to useful elements in the objects values 
 * 
 * @author gina
 *
 */
public class AudioBookLibraryContract {

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that an entry
     * has never been updated, or doesn't exist yet.
     */
    public static final long UPDATED_NEVER = -2;

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that the last
     * update time is unknown, usually when inserted from a local file source.
     */
    public static final long UPDATED_UNKNOWN = -1;

    public interface SyncColumns {
    	String UPDATED = "updated";
    }
    /**
     * An interface which states the columns in the Documents table. 
     * Notes: expected datatypes are in the comments
     * ID: if using a numerical id as the primary key, this is supplied by the BASE_COLUMNS for samples and best practices see Notes module in ioschedule
     * 			The _ID field is added to the database tables automatically, without declaring that the tables implement the base columns. It is probably 
     * 			best to follow the vendor/speakers models where there is a string as a pseudo primary key...
     * 		if using a string id as a primary key, see samples and best practices in Vendors or Speakers module in ioschedule
     * 			-declare the _ID constant
     * 				Unique string identifying this vendor. 
        			String VENDOR_ID = "vendor_id";
     * 			-in implementation of the interface create a function to generate a sanitized version of the string that is save to use in a uri
     * 			-notice that the build uri functions have different return statements. 
     * 
     * @author gina
     *
     */
    interface DocumentColumns {
    	/*
    	 * DOCUMENT_ID should use this as a string which easily identifies the document, eitehr authogr and date, or maybe title string... until
    	 * I think of a string, use an integer from the base columns. 
    	 */
    	
		/** string type "Intensional Semantics" */
		String DOCUMENT_TITLE = "title";
		/** string type "Kai von Fintel, Irene Heim"*/
		String DOCUMENT_AUTHOR = "author";
		/** string type, convert to list of strings later, "(von Fintel 2004), (von Fintel and Heim)"*/
		String DOCUMENT_CITATION = "citations";
		/** string type, convert to list of strings later, "(von Fintel 2004), (von Fintel and Heim)"*/
		String DOCUMENT_CLASSIFICATION = "classifications";
		/** String with possibly multile publication dates 2002, Spring 2002, 2004, 2007*/
		String DOCUMENT_PUBLICATION_DATE ="docpubdate";
		/** integer type from System.currentTimeMillis()*/
		String DOCUMENT_ADDED_TIME = "timeadded";
		/** Integer type from System.currentTimeMillis(), used for ordering documents and expiring old audio files?*/
		String DOCUMENT_LAST_LISTENED_TIME = "timelastlistenedto";
		/** String of chunks for this document*/
		String DOCUMENT_CHUNKS = "chunks";
		/** String*/
		String DOCUMENT_FILENAME = "filename";
		/** String, TODO should probably be a URI*/
		String DOCUMENT_FULL_FILEPATH_AND_FILENAME ="fullpathandfilename";
		/** String TODO should probably be a URI*/
		String DOCUMENT_THUMBNAIL = "thumbnail";
		/** String */
		String STARRED= "starrred";
    }
    public static final String CONTENT_AUTHORITY = "ca.openlanguage.pdftoaudiobook";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_DOCUMENTS = "documents";
    private static final String PATH_CHUNKS = "chunks";
    
    private static final String PATH_EXPORT = "export";
    private static final String PATH_STARRED = "starred";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";

    /**
     * Documents are generated based on a pdf (or a txt file) which the user 
     * intends to turn into an "AudioBook"
     * 
     * Documents contain variables from BaseColumns (supplied by android) 
     * and variables useful for a typical Library context
     * and variables specific to the application of creating and ranking audiobooks
    
     * @author gina
     *
     */
    public static class Documents implements DocumentColumns, BaseColumns{
    	/*
    	 * needed for all Modules
    	 */
    	public static final Uri CONTENT_URI =
    		BASE_CONTENT_URI.buildUpon().appendPath(PATH_DOCUMENTS).build();
    	//for more info see Vendors in ioschedule
    	public static final Uri CONTENT_STARRED_URI =
    		CONTENT_URI.buildUpon().appendPath(PATH_STARRED).build();
    	//for more info see Notes Module in ioschedule
    	public static final Uri CONTENT_EXPORT_URI =
    		CONTENT_URI.buildUpon().appendPath(PATH_EXPORT).build();
        //for more info see Android documentation for providers
    	public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/vnd.openlanguage.document";
    	public static final String CONTENT_ITEM_TYPE =
    		"vnd.android.cursor.item/vnd.openlanguage.document";
    	public static Uri buildDocumentUri(long documentId){
    		//for long data type as id, see notes module which appears to be the only module using integers as primary keys
    		return ContentUris.withAppendedId(CONTENT_URI, documentId);
    		//for string data type as id, see vendors module use:return CONTENT_URI.buildUpon().appendPath(vendorId).build();
    	}
    	public static String generateDocumentId(String documentTitle){
    		///for use of tables which use strings as primary key, must sanitize the input to be uri safe
    		return santizeId(documentTitle);
    	}
    	public static long getDocumentId(Uri uri){
    		//for long data type as id, see notes module
    		return ContentUris.parseId(uri);   
    		//for string data types as id, see vendors module use return uri.getPathSegments().get(1); //or return uri.getPathSegments().get(1);
    	}
    	/** Default "ORDER BY" clause is by date added */
        public static final String DEFAULT_SORT = DocumentColumns.DOCUMENT_ADDED_TIME+ " DESC";
        //for alphabetical by title
        //public static final String DEFAULT_SORT = VendorsColumns.NAME + " COLLATE NOCASE ASC";
        
        
        /*
    	 * Needed for searchable Modules
    	 */
    	public static Uri buildSearchUri(String query){
    		return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
    	}
    	public static boolean isSearchUri(Uri uri){
    		//1 refers to the "search" position added in the buildSearchUri funciton above
    		return PATH_SEARCH.equals(uri.getPathSegments().get(1));
    	}
    	public static String getSearchQuery(Uri uri){
    		//2 refers to the "query" position added in the buildSearchUri function above
    		return uri.getPathSegments().get(2);
    	}
    	public static final String SEARCH_SNIPPET = "search_snippet";

        
        
        /*
         * Needed for tables which have foreign keys from other tables
         */
        /** {@link Chunks#CHUNK_ID} that this document references */
    	//public static final String CHUNK_ID = "chunk_id";
    	/**
         * Build {@link Uri} that references any {@link Sessions} associated
         * with the requested {@link #SPEAKER_ID}.
        public static Uri buildSessionsDirUri(String speakerId) {
            return CONTENT_URI.buildUpon().appendPath(speakerId).appendPath(PATH_SESSIONS).build();
        }
        */


    }
    
    
    /*
     * General classes and useful functions to be used with the Domain classes
     */
    
    public static class SearchSuggest{
    	public static final Uri CONTENT_URI = 
    		BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_SUGGEST).build();
    	public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1
    		+ " COLLATE NOCASE ASC";
    }
    /**
     * * Sanitize the given string to be {@link Uri} safe for building
     * {@link ContentProvider} paths. For more info see best practices in ioschedule utils package.
     * @param input to become a uri
     * @return output safe to become a uri (numbers letters underscores and hyphens)
     */
    public static String santizeId(String input){
    	return sanitizeId(input, false);
    }
    /**
     * * Sanitize the given string to be {@link Uri} safe for building. Allows client to specify if 
     * (parenthetical expressions) should be removed from the string.
     * 
     * {@link ContentProvider} paths.
     * @param input to become a uri
     * @return output safe to become a uri (numbers letters underscores and hyphens)
     */
    public static String sanitizeId(String input, boolean stripParentheses){
    	/** Used to sanitize a string to be {@link Uri} safe. */
        Pattern sSanitizePattern = Pattern.compile("[^a-z0-9-_]");
        Pattern sParenthesesPattern = Pattern.compile("\\(.*?\\)");

    	if (input == null) return null;
    	if (stripParentheses) {
    		//Strip out all parenthetical statements when requested
    		input = sParenthesesPattern.matcher(input).replaceAll("");
    	}
    	return sSanitizePattern.matcher(input.toLowerCase()).replaceAll("");
    }
    //null constructor
    private AudioBookLibraryContract(){}
}

















