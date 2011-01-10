package ca.openlanguage.pdftoaudiobook.provider;

import java.sql.SQLException;
import java.util.HashMap;

import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryContract.Documents;
import ca.openlanguage.provider.DocumentProviderMetadata;
import ca.openlanguage.provider.DocumentProviderMetadata.DocumentTableMetadata;

import com.google.android.apps.iosched.util.SelectionBuilder;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class TestContentPovider extends ContentProvider {
	private static final String TAG = "DocumentProvider";
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
	

	private static final int DOCUMENTS = 100;
	private static final int DOCUMENTS_ID = 101;
	private static final int DOCUMENTS_STARRED = 102;
	private static final int DOCUMENTS_SEARCH = 103;
	private static final int DOCUMENTS_EXPORT = 104;
	
	private static final int CHUNKS = 200;
	//TODO: fill out the rest as needed for additional modules
	
	
	private static final int SEARCH_SUGGEST = 800;
	
	private static final String MIME_XML = "text/xml";
	
	
	/*
	 * A projection map for colums, similar to "as" construct in an sql statement to rename columns
	 * 
	 * add new columns here
	 * 
	 */
	private static HashMap<String, String> sDocumentsProjectionMap;
	static {
		sDocumentsProjectionMap = new HashMap<String, String>();
		sDocumentsProjectionMap.put(DocumentTableMetadata._ID, DocumentTableMetadata._ID);
		sDocumentsProjectionMap.put(DocumentTableMetadata.DOCUMENT_TITLE, DocumentTableMetadata.DOCUMENT_TITLE);
		sDocumentsProjectionMap.put(DocumentTableMetadata.DOCUMENT_AUTHOR, DocumentTableMetadata.DOCUMENT_AUTHOR);
		sDocumentsProjectionMap.put(DocumentTableMetadata.DOCUMENT_CITATION, DocumentTableMetadata.DOCUMENT_CITATION);
		sDocumentsProjectionMap.put(DocumentTableMetadata.DOCUMENT_ADDED_DATE, DocumentTableMetadata.DOCUMENT_ADDED_DATE);
	}
	
	
	private static final UriMatcher sUriMatcher;
	private static final UriMatcher sUriMatcherAudioBooks =buildUriMatcher();
	/**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations (specialized queries, as well as the typical dir of 
     * all rows, individual items etc) supported by 
     * this {@link ContentProvider}.
     */
	private static UriMatcher buildUriMatcher(){
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = AudioBookLibraryContract.CONTENT_AUTHORITY;
		
		matcher.addURI(authority, "documents", DOCUMENTS);
		matcher.addURI(authority, "documents/*", DOCUMENTS_ID);
		matcher.addURI(authority, "documents/starred", DOCUMENTS_STARRED);
		matcher.addURI(authority, "documents/search/*", DOCUMENTS_SEARCH);
		matcher.addURI(authority, "documents/export", DOCUMENTS_EXPORT);
		
		//TODO: fill out the rest as needed for additional modules
		
		matcher.addURI(authority, "search_suggest_query", SEARCH_SUGGEST);
		return matcher;
	}
	private static final int INCOMING_DOCUMENT_COLLECTION_URI_INDICATOR = 1;
	private static final int INCOMING_SINGLE_DOCUMENT_URI_INDICATOR = 2;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(DocumentProviderMetadata.AUTHORITY, "documents", INCOMING_DOCUMENT_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(DocumentProviderMetadata.AUTHORITY, "documents/#", INCOMING_SINGLE_DOCUMENT_URI_INDICATOR);
		
	}
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
        if (LOGV) Log.v(TAG, "delete(uri=" + uri + ")");
//      final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//      final SelectionBuilder builder = buildSimpleSelection(uri);
//      return builder.where(selection, selectionArgs).delete(db);
      throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcherAudioBooks.match(uri);
		switch (match) {
		case DOCUMENTS:
			return Documents.CONTENT_TYPE;
		case DOCUMENTS_SEARCH:
			return Documents.CONTENT_TYPE;
		case DOCUMENTS_STARRED:
			return Documents.CONTENT_TYPE;
		case DOCUMENTS_ID:
			return Documents.CONTENT_ITEM_TYPE;
		//the export use case might work up until here where it would fail
		case DOCUMENTS_EXPORT:
			return MIME_XML;
		//TODO add the other domain modules
		default:
			throw new UnsupportedOperationException("Unkown uri: " +uri);
		}
		
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//validate the requested uri
		if (LOGV) Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcherAudioBooks.match(uri);

        switch (match) {
	        case DOCUMENTS:{
				Long now = Long.valueOf(System.currentTimeMillis());
				
				/*
				 * validate input data
				 */
				if (values.containsKey(Documents.DOCUMENT_TITLE) == false ){
					
					//asked to be in a try catch, maybe change it later
					try {
						throw new SQLException("Failed to insert row because document title is missing "+uri);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (values.containsKey(Documents.DOCUMENT_AUTHOR) == false ){
					values.put(Documents.DOCUMENT_AUTHOR, "Unknown Author");
				}
//				if (values.containsKey(DocumentTableMetadata.DOCUMENT_CITATION) == false ){
//					values.put(DocumentTableMetadata.DOCUMENT_CITATION, "Unknown Citation");
//				}
//				if (values.containsKey(DocumentTableMetadata.DOCUMENT_ADDED_DATE) == false ){
//					values.put(DocumentTableMetadata.DOCUMENT_ADDED_DATE, now);
//				}
				
				/*
				 * insert into database and return the uri with the new row id
				 */
				
				
				long rowId = db.insert("documents", null, values);
				
				//if insert worked, display that row by setting the uri to that id and then the activity will display it
				if (rowId > 0){
					Uri insertedDocumentUri = ContentUris.withAppendedId(DocumentTableMetadata.CONTENT_URI, rowId);
					getContext().getContentResolver().notifyChange(insertedDocumentUri, null);
					return insertedDocumentUri;
				} else {
				
				
					/*
					 * if insert failed, throw an exception. its in a try catch because eclipse insisted
					 */
					try {
						throw new SQLException("Failed to insert row into " +uri);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
	        }
	        default:{
	        	throw new UnsupportedOperationException("Unknown uri: " + uri);
	        }
        }
		
		
		
	}//end insert

	
	/*
	 * Create a database, taken from page 99, which was taken from the NotPad Sample code in v8
	 */
	
	//original private DatabaseHelper mOpenHelper;
	private AudioBookLibraryDatabase mOpenHelper;
	//inner class to define databasehelper, at the moment extending SQLiteOpenHelper
	
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		Log.v(TAG, "in the oncreate " + getContext() + ")");
		final Context context = getContext();
		//original mOpenHelper = new DatabaseHelper(getContext());
		mOpenHelper = new AudioBookLibraryDatabase(getContext());
		//works: AudioBookLibraryDatabase testAudioDBClass = new AudioBookLibraryDatabase(getContext());
		//AudioBookLibraryDatabase audiobookdatabaeshelper = new AudioBookLibraryDatabase(getContext());
		//audiobookdatabaeshelper.onCreate();
		//audiobookdatabaeshelper.testInsertRow();
		mOpenHelper.testInsertRow();
		SelectionBuilder builder = new SelectionBuilder();
		String mesg = builder.toString();
		return true;
	}
	
 

	/**
	 * Checks the uri for two types of queries, either return all the rows of the table, or return the row matching the id (passed in on the end of the uri)
	 */
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		//swich based on the uri for the two types of queries
		switch (sUriMatcher.match(uri)) {
			case INCOMING_DOCUMENT_COLLECTION_URI_INDICATOR:
				qb.setTables(DocumentTableMetadata.TABLE_NAME);
				qb.setProjectionMap(sDocumentsProjectionMap);
				break;
			case INCOMING_SINGLE_DOCUMENT_URI_INDICATOR:
				qb.setTables(DocumentTableMetadata.TABLE_NAME);
				qb.setProjectionMap(sDocumentsProjectionMap);
				qb.appendWhere(DocumentTableMetadata._ID + "="
						+ uri.getPathSegments().get(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		//if no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)){
			orderBy = DocumentTableMetadata.DEFAULT_SORT_ORDER;
		}else{
			orderBy = sortOrder;
		}
		
		//get the database and then run the query, putting the result set into the cursor
		SQLiteDatabase db = 
			mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		
		//included in the source code p 101 but never used
		//int i = c.getCount();
		
		//Tell the cursor which uri to watch so it knows if the data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
		
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)){
		case INCOMING_DOCUMENT_COLLECTION_URI_INDICATOR:
			count = db.update(DocumentTableMetadata.TABLE_NAME
					, values
					, where
					, whereArgs);
			break;
		case INCOMING_SINGLE_DOCUMENT_URI_INDICATOR:
			String rowId = uri.getPathSegments().get(1);
			count = db.update(DocumentTableMetadata.TABLE_NAME
					, values
					, DocumentTableMetadata._ID + "=" +rowId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "")
					, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI "+uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
		
	}

}



