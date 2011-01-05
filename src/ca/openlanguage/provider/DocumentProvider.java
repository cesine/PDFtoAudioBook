/**
 * 
 */
package ca.openlanguage.provider;

import java.sql.SQLException;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import ca.openlanguage.provider.DocumentProviderMetadata.DocumentTableMetadata;

/**
 * @author gina
 *
 */
public class DocumentProvider extends ContentProvider {

	private static final String TAG = "DocumentProvider";
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
		SQLiteDatabase db  = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)){
		case INCOMING_DOCUMENT_COLLECTION_URI_INDICATOR:
			count = db.delete(DocumentTableMetadata.TABLE_NAME, where, whereArgs);
			break;
		case INCOMING_SINGLE_DOCUMENT_URI_INDICATOR:
			String rowId = uri.getPathSegments().get(1);
			count = db.delete(DocumentTableMetadata.TABLE_NAME 
					, DocumentTableMetadata._ID + "=" + rowId 
						+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "")
					, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI "+ uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)){
		case INCOMING_DOCUMENT_COLLECTION_URI_INDICATOR:
			return DocumentTableMetadata.CONTENT_TYPE;
		case INCOMING_SINGLE_DOCUMENT_URI_INDICATOR:
			return DocumentTableMetadata.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI "+uri);
		}
		
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//validate the requested uri
		if (sUriMatcher.match(uri) != INCOMING_DOCUMENT_COLLECTION_URI_INDICATOR){
			throw new IllegalArgumentException("Unknown URI "+uri);
		}

		Long now = Long.valueOf(System.currentTimeMillis());
		
		/*
		 * validate input data
		 */
		if (values.containsKey(DocumentTableMetadata.DOCUMENT_TITLE) == false ){
			
			//asked to be in a try catch, maybe change it later
			try {
				throw new SQLException("Failed to insert row because document title is missing "+uri);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (values.containsKey(DocumentTableMetadata.DOCUMENT_AUTHOR) == false ){
			values.put(DocumentTableMetadata.DOCUMENT_AUTHOR, "Unknown Author");
		}
		if (values.containsKey(DocumentTableMetadata.DOCUMENT_CITATION) == false ){
			values.put(DocumentTableMetadata.DOCUMENT_CITATION, "Unknown Citation");
		}
		if (values.containsKey(DocumentTableMetadata.DOCUMENT_ADDED_DATE) == false ){
			values.put(DocumentTableMetadata.DOCUMENT_ADDED_DATE, now);
		}
		
		
		
		/*
		 * insert into database and return the uri with the new row id
		 */
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(DocumentTableMetadata.TABLE_NAME, DocumentTableMetadata.DOCUMENT_TITLE, values);
		
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

	
	/*
	 * Create a database
	 */
	
	private DatabaseHelper mOpenHelper;
	//inner class to define databasehelper, at the moment extending SQLiteOpenHelper
	private static class DatabaseHelper extends SQLiteOpenHelper{
		DatabaseHelper(Context context){
			super(context, DocumentProviderMetadata.DATABASE_NAME, null, DocumentProviderMetadata.DATABASE_VERSION);
		}
		public void onCreate(SQLiteDatabase db){
			db.execSQL("CREATE TABLE " + DocumentTableMetadata.TABLE_NAME + " ("
					+" INTEGER PRIMARY KEY,"
					+ DocumentTableMetadata.DOCUMENT_TITLE + " TEXT,"
					+ DocumentTableMetadata.DOCUMENT_AUTHOR + " TEXT,"
					+ DocumentTableMetadata.DOCUMENT_CITATION + " TEXT,"
					+ DocumentTableMetadata.DOCUMENT_ADDED_DATE + " INTEGER"
					+ ");" 
					);
		}
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will distroy all old data");
			db.execSQL("DROP TABLE IF EXISTS "+DocumentTableMetadata.TABLE_NAME);
			onCreate(db);
		
		}
	}
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
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
