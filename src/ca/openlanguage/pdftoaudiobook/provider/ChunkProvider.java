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

/*
 * Chunk TODO: refactor chunk database into main database and into main provider, 
 * revert to some of the design principles in the Jan 09 itteration (which were based on best practices found in Google IO Schedule 2010)
 */

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
import ca.openlanguage.pdftoaudiobook.provider.ChunkDatabase.ChunkColumns;

public class ChunkProvider extends ContentProvider {

    private static final String TAG = "ChunkProvider";

    private static final String DATABASE_NAME = "audiobookchunks.db";
    private static final int DATABASE_VERSION = 2;
    private static final String CHUNKS_TABLE_NAME = "chunks";

    private static HashMap<String, String> sChunksProjectionMap;
    private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int CHUNKS = 1;
    private static final int CHUNK_ID = 2;
    private static final int LIVE_FOLDER_CHUNKS = 3;

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
            db.execSQL("CREATE TABLE " + CHUNKS_TABLE_NAME + " ("
                    + ChunkColumns._ID + " INTEGER PRIMARY KEY,"
                    + ChunkColumns.TITLE + " TEXT,"
                    + ChunkColumns.AUTHOR + " TEXT,"
                    + ChunkColumns.CITATION + " TEXT,"
                    + ChunkColumns.CLASSIFICATION + " TEXT,"
                    + ChunkColumns.CHUNKS+ " TEXT,"
                    + ChunkColumns.LAST_LISTENED_TIME + " TEXT,"
                    + ChunkColumns.FILENAME + " TEXT,"
                    + ChunkColumns.FULL_FILEPATH_AND_FILENAME + " TEXT,"
                    + ChunkColumns.PUBLICATION_DATE + " TEXT,"
                    + ChunkColumns.THUMBNAIL + " TEXT,"
                    + ChunkColumns.STARRED + " TEXT,"
                    
                    + ChunkColumns.TASKNOTES + " TEXT,"
                    + ChunkColumns.CREATED_DATE + " INTEGER,"
                    + ChunkColumns.MODIFIED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+ CHUNKS_TABLE_NAME );
            onCreate(db);
        }
    }//end databasehelper

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CHUNKS_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case CHUNKS:
        	//gets a cursor of all rows, with all columns (all should be entered into the projectionmap)
            qb.setProjectionMap(sChunksProjectionMap);
            break;

        case CHUNK_ID:
            qb.setProjectionMap(sChunksProjectionMap);
            //get the row (of selected columns in projetion, it should be all of them) for that ID
            //gets a cursor of the row which matches the id from the uri
            qb.appendWhere(ChunkColumns._ID + "=" + uri.getPathSegments().get(1));
            break;

        case LIVE_FOLDER_CHUNKS:
        	//gets a cursor of all rows, but with just a few columns
            qb.setProjectionMap(sLiveFolderProjectionMap);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = ChunkColumns.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case CHUNKS:
        case LIVE_FOLDER_CHUNKS:
            return ChunkColumns.CONTENT_TYPE;

        case CHUNK_ID:
            return ChunkColumns.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
    	//initial values are null
    	
    	
        // Validate the requested uri
        if (sUriMatcher.match(uri) != CHUNKS) {
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
        
        if (values.containsKey(ChunkColumns.CREATED_DATE) == false) {
            values.put(ChunkColumns.CREATED_DATE, now);
        }

        if (values.containsKey(ChunkColumns.MODIFIED_DATE) == false) {
            values.put(ChunkColumns.MODIFIED_DATE, now);
        }

        if (values.containsKey(ChunkColumns.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(ChunkColumns.TITLE, r.getString(android.R.string.untitled));
        }

      // initialize nullable fields here
        if (values.containsKey(ChunkColumns.TASKNOTES) == false) {
            values.put(ChunkColumns.TASKNOTES, "");
        }
        if (values.containsKey(ChunkColumns.AUTHOR) == false) {
            values.put(ChunkColumns.AUTHOR, "");
        }
        if (values.containsKey(ChunkColumns.CITATION) == false) {
            values.put(ChunkColumns.CITATION, "");
        }
        if (values.containsKey(ChunkColumns.CLASSIFICATION) == false) {
            values.put(ChunkColumns.CLASSIFICATION, "");
        }
        if (values.containsKey(ChunkColumns.CHUNKS) == false) {
            values.put(ChunkColumns.CHUNKS, "");
        }
        if (values.containsKey(ChunkColumns.LAST_LISTENED_TIME) == false) {
            values.put(ChunkColumns.LAST_LISTENED_TIME, "");
        }
        if (values.containsKey(ChunkColumns.FILENAME) == false) {
            values.put(ChunkColumns.FILENAME, "");
        }
        if (values.containsKey(ChunkColumns.FULL_FILEPATH_AND_FILENAME) == false) {
            values.put(ChunkColumns.FULL_FILEPATH_AND_FILENAME, "");
        }
        if (values.containsKey(ChunkColumns.THUMBNAIL) == false) {
            values.put(ChunkColumns.THUMBNAIL, "");
        }
        if (values.containsKey(ChunkColumns.STARRED) == false) {
            values.put(ChunkColumns.STARRED, "");
        }
        if (values.containsKey(ChunkColumns.PUBLICATION_DATE) == false) {
            values.put(ChunkColumns.PUBLICATION_DATE, "");
        }
        
        
        
        
        
        
        
        

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // it seems suspicious to only be the content of TASKNOTES, ah its the nullcolumnhack
        long rowId = db.insert(CHUNKS_TABLE_NAME, ChunkColumns.TASKNOTES, values);
        if (rowId > 0) {
            Uri chunkUri = ContentUris.withAppendedId(ChunkColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(chunkUri, null);
            return chunkUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case CHUNKS:
            count = db.delete(CHUNKS_TABLE_NAME, where, whereArgs);
            break;

        case CHUNK_ID:
            String chunkId = uri.getPathSegments().get(1);
            count = db.delete(CHUNKS_TABLE_NAME, ChunkColumns._ID + "=" + chunkId
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
        case CHUNKS:
            count = db.update(CHUNKS_TABLE_NAME, values, where, whereArgs);
            break;

        case CHUNK_ID:
            String chunkId = uri.getPathSegments().get(1);
            //update teh row using the values provided
            //this takes updates from the title editor
            //this takes updates from the chunks editor
            count = db.update(CHUNKS_TABLE_NAME, values, ChunkColumns._ID + "=" + chunkId
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
        sUriMatcher.addURI(ChunkDatabase.AUTHORITY, "chunks", CHUNKS);
        sUriMatcher.addURI(ChunkDatabase.AUTHORITY, "chunks/#", CHUNK_ID);
        sUriMatcher.addURI(ChunkDatabase.AUTHORITY, "live_folders/chunks", LIVE_FOLDER_CHUNKS);

        sChunksProjectionMap = new HashMap<String, String>();
        sChunksProjectionMap.put(ChunkColumns._ID, ChunkColumns._ID);
        
        sChunksProjectionMap.put(ChunkColumns.TITLE, ChunkColumns.TITLE);
        sChunksProjectionMap.put(ChunkColumns.AUTHOR, ChunkColumns.AUTHOR);
        sChunksProjectionMap.put(ChunkColumns.CITATION, ChunkColumns.CITATION);
        sChunksProjectionMap.put(ChunkColumns.CLASSIFICATION, ChunkColumns.CLASSIFICATION);
        sChunksProjectionMap.put(ChunkColumns.PUBLICATION_DATE, ChunkColumns.PUBLICATION_DATE);
        sChunksProjectionMap.put(ChunkColumns.LAST_LISTENED_TIME, ChunkColumns.LAST_LISTENED_TIME);
        sChunksProjectionMap.put(ChunkColumns.CHUNKS, ChunkColumns.CHUNKS);
        sChunksProjectionMap.put(ChunkColumns.FILENAME, ChunkColumns.FILENAME);
        sChunksProjectionMap.put(ChunkColumns.FULL_FILEPATH_AND_FILENAME, ChunkColumns.FULL_FILEPATH_AND_FILENAME);
        sChunksProjectionMap.put(ChunkColumns.THUMBNAIL, ChunkColumns.THUMBNAIL);
        sChunksProjectionMap.put(ChunkColumns.STARRED, ChunkColumns.STARRED);

        
        sChunksProjectionMap.put(ChunkColumns.TASKNOTES, ChunkColumns.TASKNOTES);
        sChunksProjectionMap.put(ChunkColumns.CREATED_DATE, ChunkColumns.CREATED_DATE);
        sChunksProjectionMap.put(ChunkColumns.MODIFIED_DATE, ChunkColumns.MODIFIED_DATE);

        // Support for Live Folders.
        sLiveFolderProjectionMap = new HashMap<String, String>();
        sLiveFolderProjectionMap.put(LiveFolders._ID, ChunkColumns._ID + " AS " +
                LiveFolders._ID);
        sLiveFolderProjectionMap.put(LiveFolders.NAME, ChunkColumns.TITLE + " AS " +
                LiveFolders.NAME);
        // Add more columns here for more robust Live Folders.
    }



}
