package ca.openlanguage.pdftoaudiobook.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryDatabase.DocumentColumns;

public class AudioBookLibraryProvider extends ContentProvider {

    private static final String TAG = "AudioBookLibraryProvider";

    private static final String DATABASE_NAME = "audiobooklibrary.db";
    private static final int DATABASE_VERSION = 2;
    private static final String DOCUMENTS_TABLE_NAME = "documents";

    private static HashMap<String, String> sDocumentsProjectionMap;
    private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int DOCUMENTS = 1;
    private static final int DOCUMENT_ID = 2;
    private static final int LIVE_FOLDER_DOCUMENTS = 3;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	db.execSQL("DROP TABLE IF EXISTS " + DOCUMENTS_TABLE_NAME);
            String createStatement = ("CREATE TABLE " + DOCUMENTS_TABLE_NAME + " ("
                    + DocumentColumns._ID + " INTEGER PRIMARY KEY,"
                    + DocumentColumns.TITLE + " TEXT,"
                    + DocumentColumns.DOCUMENT + " TEXT,"
                    + DocumentColumns.CREATED_DATE + " INTEGER,"
                    + DocumentColumns.MODIFIED_DATE + " INTEGER"
                    + ");");
            db.execSQL(createStatement);
            
            //the values in this is faulty
//            ContentValues values = new ContentValues();
//            values.put("title", "testing frmo database helpers oncreate statement.");
//            values.put("_id", "1");
//            Long result = db.insert(DOCUMENTS_TABLE_NAME, null, values);
//            String debug = result.toString();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS documents");
            onCreate(db);
        }
    }//end databasehelper

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
//        ContentValues values = new ContentValues();
//        values.put("title", "testing frmo providers oncreate statement.");
//        Long result = db.insert(DOCUMENTS_TABLE_NAME, null, values);
//        String debug = result.toString();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DOCUMENTS_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case DOCUMENTS:
            qb.setProjectionMap(sDocumentsProjectionMap);
            break;

        case DOCUMENT_ID:
            qb.setProjectionMap(sDocumentsProjectionMap);
            qb.appendWhere(DocumentColumns._ID + "=" + uri.getPathSegments().get(1));
            break;

        case LIVE_FOLDER_DOCUMENTS:
            qb.setProjectionMap(sLiveFolderProjectionMap);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DocumentColumns.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        
//        ContentValues values = new ContentValues();
//        values.put("title", "testing frmo database helpers oncreate statement.");
//        
//        Long result = db.insert(DOCUMENTS_TABLE_NAME, null, values);
//        String debug = result.toString();
        
        //Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        Cursor c = qb.query(db, projection, null, null, null, null, orderBy );
        
        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case DOCUMENTS:
        case LIVE_FOLDER_DOCUMENTS:
            return DocumentColumns.CONTENT_TYPE;

        case DOCUMENT_ID:
            return DocumentColumns.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
    	if (sUriMatcher.match(uri) != DOCUMENTS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(DocumentColumns.CREATED_DATE) == false) {
            values.put(DocumentColumns.CREATED_DATE, now);
        }

        if (values.containsKey(DocumentColumns.MODIFIED_DATE) == false) {
            values.put(DocumentColumns.MODIFIED_DATE, now);
        }

        if (values.containsKey(DocumentColumns.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(DocumentColumns.TITLE, "unknowntitle");
        }

        if (values.containsKey(DocumentColumns.DOCUMENT) == false) {
            values.put(DocumentColumns.DOCUMENT, "unknowndoc");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    	mOpenHelper.onUpgrade(db, 1, 2);
        mOpenHelper.onCreate(db);
        long rowId = db.insert(DOCUMENTS_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri documentUri = ContentUris.withAppendedId(DocumentColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(documentUri, null);
            return documentUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case DOCUMENTS:
            count = db.delete(DOCUMENTS_TABLE_NAME, where, whereArgs);
            break;

        case DOCUMENT_ID:
            String documentId = uri.getPathSegments().get(1);
            count = db.delete(DOCUMENTS_TABLE_NAME, DocumentColumns._ID + "=" + documentId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case DOCUMENTS:
            count = db.update(DOCUMENTS_TABLE_NAME, values, where, whereArgs);
            break;

        case DOCUMENT_ID:
            String documentId = uri.getPathSegments().get(1);
            count = db.update(DOCUMENTS_TABLE_NAME, values, DocumentColumns._ID + "=" + documentId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AudioBookLibraryDatabase.AUTHORITY, "documents", DOCUMENTS);
        sUriMatcher.addURI(AudioBookLibraryDatabase.AUTHORITY, "documents/#", DOCUMENT_ID);
        sUriMatcher.addURI(AudioBookLibraryDatabase.AUTHORITY, "live_folders/documents", LIVE_FOLDER_DOCUMENTS);

        sDocumentsProjectionMap = new HashMap<String, String>();
        sDocumentsProjectionMap.put(DocumentColumns._ID, DocumentColumns._ID);
        sDocumentsProjectionMap.put(DocumentColumns.TITLE, DocumentColumns.TITLE);
        sDocumentsProjectionMap.put(DocumentColumns.DOCUMENT, DocumentColumns.DOCUMENT);
        sDocumentsProjectionMap.put(DocumentColumns.CREATED_DATE, DocumentColumns.CREATED_DATE);
        sDocumentsProjectionMap.put(DocumentColumns.MODIFIED_DATE, DocumentColumns.MODIFIED_DATE);

        // Support for Live Folders.
        sLiveFolderProjectionMap = new HashMap<String, String>();
        sLiveFolderProjectionMap.put(LiveFolders._ID, DocumentColumns._ID + " AS " +
                LiveFolders._ID);
        sLiveFolderProjectionMap.put(LiveFolders.NAME, DocumentColumns.TITLE + " AS " +
                LiveFolders.NAME);
        // Add more columns here for more robust Live Folders.
    }
}
