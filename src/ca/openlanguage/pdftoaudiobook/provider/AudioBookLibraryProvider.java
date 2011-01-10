package ca.openlanguage.pdftoaudiobook.provider;

import java.io.FileNotFoundException;


import java.util.Arrays;



import com.google.android.apps.iosched.util.SelectionBuilder;


import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryContract.Documents;
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryContract.SearchSuggest;
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryDatabase.DocumentsSearchColumns;
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryDatabase.Tables;
import android.app.SearchManager;
import android.content.ContentProvider;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;

import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.OpenableColumns;
import android.util.Log;

/*
 * Provider that stores {@link AudioBookLibraryContract} data. 
 * Data is usually(?) inserted by a {@link AsyncService} and 
 * queried by activities in the ui package
 */
public class AudioBookLibraryProvider extends ContentProvider {
	private static final String TAG = "AudioBookLibraryProvider";
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
	
	private AudioBookLibraryDatabase mOpenHelper;
	private static final UriMatcher sUriMatcher = buildUriMatcher();
	
	
	private static final int DOCUMENTS = 100;
	private static final int DOCUMENTS_ID = 101;
	private static final int DOCUMENTS_STARRED = 102;
	private static final int DOCUMENTS_SEARCH = 103;
	private static final int DOCUMENTS_EXPORT = 104;
	
	private static final int CHUNKS = 200;
	//TODO: fill out the rest as needed for additional modules
	
	
	private static final int SEARCH_SUGGEST = 800;
	
	private static final String MIME_XML = "text/xml";
	
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

	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (LOGV) Log.v(TAG, "delete(uri=" + uri + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        return builder.where(selection, selectionArgs).delete(db);
	}
	/** {@inheritDoc} 
	 * getType is a switch based on the constants to determine which content type to return, it generallyy returns the dir type, except for 
	 * _ID where it returns the itemtype.
	 */
	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
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

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (LOGV) Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
	        case DOCUMENTS:{
	        	final long documentId = db.insertOrThrow(Tables.DOCUMENTS, null, values);
	        	//id as string use this: return Vendors.buildVendorUri(values.getAsString(Vendors.VENDOR_ID));
	        	return ContentUris.withAppendedId(Documents.CONTENT_URI, documentId);
	        }
	        case SEARCH_SUGGEST:{
	        	db.insertOrThrow(Tables.SEARCH_SUGGEST, null, values);
	        	return SearchSuggest.CONTENT_URI;
	        }
	        default:{
	        	throw new UnsupportedOperationException("Unknown uri: " + uri);
	        }
        }
	}

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		mOpenHelper = new AudioBookLibraryDatabase(context);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (LOGV) Log.v(TAG, 
				"query(uri=" + uri 
				+ ", proj=" +  Arrays.toString(projection) + ")");
		
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			default: {
				//most cases are handled with the simple selection builder
				final SelectionBuilder builder = buildExpandedSelection(uri, match) ;
				return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
			}
			case DOCUMENTS_EXPORT:{
				//TODO check this usecase, supspect that it wont work due to missing util classes
				final String[] columns = { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };
				final MatrixCursor cursor = new MatrixCursor(columns, 1);
				cursor.addRow(new String[] { "documentsaudiobooks.xml", null});
				return cursor;
			}
			case SEARCH_SUGGEST:{
				final SelectionBuilder builder = new SelectionBuilder();
				
				//adjust incoming query to become SQl text match
				selectionArgs[0] = selectionArgs[0] + "%";
				builder.table(Tables.SEARCH_SUGGEST);
				builder.where(selection, selectionArgs);
				//TODO findout what the searchmanager does
				builder.map(SearchManager.SUGGEST_COLUMN_QUERY,
						SearchManager.SUGGEST_COLUMN_TEXT_1);
				
				projection = new String[] { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_QUERY };
				final String limit = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
				return builder.query(db, projection, null, null, SearchSuggest.DEFAULT_SORT, limit);
			}
		}

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (LOGV) Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        return builder.where(selection, selectionArgs).update(db, values);
	}
	/*
	 * additional functions and inner classes
	 */
    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
	private SelectionBuilder buildSimpleSelection(Uri uri){
		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match){
			case DOCUMENTS:{
				return builder.table(Tables.DOCUMENTS);
			}
			case DOCUMENTS_ID:{
				//for domain models where the id is a string: final String documentID = Documents.getDocumentId(uri);
				final String documentId = uri.getPathSegments().get(1);
				return builder.table(Tables.DOCUMENTS)
					.where(Documents._ID + "=?", documentId);
			}
			//TODO fill in the other modules
			case SEARCH_SUGGEST:{
				return builder.table(Tables.SEARCH_SUGGEST);
			
			}
			default:{
				throw new UnsupportedOperationException("Unknown uri: " + uri);
			}
		}		
	}
    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
	private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
		final SelectionBuilder builder = new SelectionBuilder();
		switch (match) {
			case DOCUMENTS:
				return builder.table(Tables.DOCUMENTS);
			case DOCUMENTS_ID: {
				final long documentId = Documents.getDocumentId(uri);
				return builder.table(Tables.DOCUMENTS).where(Documents._ID + "=?",
						Long.toString(documentId));
			}
			//dont need the maptotable section i think, TODO check  the selectionbuilder in utils to find out more what these fuctions do .mapToTable(Documents._ID, Tables.DOCUMENTS)
			case DOCUMENTS_STARRED:{
				return builder.table(Tables.DOCUMENTS)
					.where(Documents.STARRED + "=1");
			}
			case DOCUMENTS_SEARCH: {
				final String query = Documents.getSearchQuery(uri);
				//TODO test this use case, might not work, modeled after vendors module which has both IDs .mapToTable(Documents._ID, Tables.DOCUMENTS)
				return builder.table(Tables.DOCUMENTS)
					.map(Documents.SEARCH_SNIPPET, Subquery.DOCUMENTS_SNIPPET)
					.where(DocumentsSearchColumns.BODY + " MATCH ?", query);
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}
	private interface Subquery {
		String DOCUMENTS_SNIPPET = "snippet("+ Tables.DOCUMENTS_SEARCH + ",'{','}','\u2026')";
	}
	private interface Qualified {
		String DOCUMENTS_STARRED = Tables.DOCUMENTS + "." + Documents.STARRED;
	}
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    	final int match = sUriMatcher.match(uri);
    	switch (match){
    	/* the notes module exports as xml, this is probably not hte prefered export format for documents/auidobooks in the is app. revisit the export functionality later. 
		 *
		 case DOCUMENTS_EXPORT:{
    			 try {
    			 
    				final File documetnsaudiobooksFile ;//= notesExporter.writeExportedNotes(getContext())
    				return ParcelFileDescriptor
    					.open(documetnsaudiobooksFile, ParcelFileDescriptor.MODE_READ_ONLY);
    			}catch (IOException e){
    				throw new FileNotFoundException("Unable to export notes: " + e.toString());
    			}
    			
    		}
    		*/
    		default:{
    			throw new UnsupportedOperationException("Unknown uri: "+ uri);
    		}
    	}
    }
	
	/**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     *     
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }
     */
}
