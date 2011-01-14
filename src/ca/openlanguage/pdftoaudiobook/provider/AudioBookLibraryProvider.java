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
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryDatabase.AudiobookColumns;


public class AudioBookLibraryProvider extends ContentProvider {

    private static final String TAG = "AudiobookProvider";

    private static final String DATABASE_NAME = "audiobook.db";
    private static final int DATABASE_VERSION = 2;
    private static final String AUDIOBOOKS_TABLE_NAME = "audiobooks";

    private static HashMap<String, String> sAudiobooksProjectionMap;
    private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int AUDIOBOOKS = 1;
    private static final int AUDIOBOOK_ID = 2;
    private static final int LIVE_FOLDER_AUDIOBOOKS = 3;

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
            db.execSQL("CREATE TABLE " + AUDIOBOOKS_TABLE_NAME + " ("
                    + AudiobookColumns._ID + " INTEGER PRIMARY KEY,"
                    + AudiobookColumns.TITLE + " TEXT,"
                    + AudiobookColumns.AUTHOR + " TEXT,"
                    + AudiobookColumns.CITATION + " TEXT,"
                    + AudiobookColumns.CLASSIFICATION + " TEXT,"
                    + AudiobookColumns.CHUNKS+ " TEXT,"
                    + AudiobookColumns.LAST_LISTENED_TIME + " TEXT,"
                    + AudiobookColumns.FILENAME + " TEXT,"
                    + AudiobookColumns.FULL_FILEPATH_AND_FILENAME + " TEXT,"
                    + AudiobookColumns.PUBLICATION_DATE + " TEXT,"
                    + AudiobookColumns.THUMBNAIL + " TEXT,"
                    + AudiobookColumns.STARRED + " TEXT,"
                    
                    + AudiobookColumns.TASKNOTES + " TEXT,"
                    + AudiobookColumns.CREATED_DATE + " INTEGER,"
                    + AudiobookColumns.MODIFIED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+ AUDIOBOOKS_TABLE_NAME);
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
        qb.setTables(AUDIOBOOKS_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case AUDIOBOOKS:
        	//gets a cursor of all rows, with all columns (all should be entered into the projectionmap)
            qb.setProjectionMap(sAudiobooksProjectionMap);
            break;

        case AUDIOBOOK_ID:
            qb.setProjectionMap(sAudiobooksProjectionMap);
            //get the row (of selected columns in projetion, it should be all of them) for that ID
            //gets a cursor of the row which matches the id from the uri
            qb.appendWhere(AudiobookColumns._ID + "=" + uri.getPathSegments().get(1));
            break;

        case LIVE_FOLDER_AUDIOBOOKS:
        	//gets a cursor of all rows, but with just a few columns
            qb.setProjectionMap(sLiveFolderProjectionMap);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = AudiobookColumns.DEFAULT_SORT_ORDER;
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
        case AUDIOBOOKS:
        case LIVE_FOLDER_AUDIOBOOKS:
            return AudiobookColumns.CONTENT_TYPE;

        case AUDIOBOOK_ID:
            return AudiobookColumns.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
    	//initial values are null
    	
    	
        // Validate the requested uri
        if (sUriMatcher.match(uri) != AUDIOBOOKS) {
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
        
        if (values.containsKey(AudiobookColumns.CREATED_DATE) == false) {
            values.put(AudiobookColumns.CREATED_DATE, now);
        }

        if (values.containsKey(AudiobookColumns.MODIFIED_DATE) == false) {
            values.put(AudiobookColumns.MODIFIED_DATE, now);
        }

        if (values.containsKey(AudiobookColumns.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(AudiobookColumns.TITLE, r.getString(android.R.string.untitled));
        }

      // initialize nullable fields here
        if (values.containsKey(AudiobookColumns.TASKNOTES) == false) {
            values.put(AudiobookColumns.TASKNOTES, "");
        }
        if (values.containsKey(AudiobookColumns.AUTHOR) == false) {
            values.put(AudiobookColumns.AUTHOR, "");
        }
        if (values.containsKey(AudiobookColumns.CITATION) == false) {
            values.put(AudiobookColumns.CITATION, "");
        }
        if (values.containsKey(AudiobookColumns.CLASSIFICATION) == false) {
            values.put(AudiobookColumns.CLASSIFICATION, "");
        }
        if (values.containsKey(AudiobookColumns.CHUNKS) == false) {
            values.put(AudiobookColumns.CHUNKS, "Section");
        }
        if (values.containsKey(AudiobookColumns.LAST_LISTENED_TIME) == false) {
            values.put(AudiobookColumns.LAST_LISTENED_TIME, "");
        }
        if (values.containsKey(AudiobookColumns.FILENAME) == false) {
            values.put(AudiobookColumns.FILENAME, "");
        }
        if (values.containsKey(AudiobookColumns.FULL_FILEPATH_AND_FILENAME) == false) {
            values.put(AudiobookColumns.FULL_FILEPATH_AND_FILENAME, "");
        }
        if (values.containsKey(AudiobookColumns.THUMBNAIL) == false) {
            values.put(AudiobookColumns.THUMBNAIL, "");
        }
        if (values.containsKey(AudiobookColumns.STARRED) == false) {
            values.put(AudiobookColumns.STARRED, "");
        }
        if (values.containsKey(AudiobookColumns.PUBLICATION_DATE) == false) {
            values.put(AudiobookColumns.PUBLICATION_DATE, "");
        }
        
        
        
        
        
        
        
        

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // it seems suspicious to only be the content of TASKNOTES, ah its the nullcolumnhack
        
        //values contains title=<untitled> created=1294758635956 audiobook= modified=1294758635956
        long rowId = db.insert(AUDIOBOOKS_TABLE_NAME, AudiobookColumns.TASKNOTES, values);
        if (rowId > 0) {
            Uri audiobookUri = ContentUris.withAppendedId(AudiobookColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(audiobookUri, null);
            return audiobookUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case AUDIOBOOKS:
            count = db.delete(AUDIOBOOKS_TABLE_NAME, where, whereArgs);
            break;

        case AUDIOBOOK_ID:
            String audiobookId = uri.getPathSegments().get(1);
            count = db.delete(AUDIOBOOKS_TABLE_NAME, AudiobookColumns._ID + "=" + audiobookId
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
        case AUDIOBOOKS:
            count = db.update(AUDIOBOOKS_TABLE_NAME, values, where, whereArgs);
            break;

        case AUDIOBOOK_ID:
            String audiobookId = uri.getPathSegments().get(1);
            //update teh row using the values provided
            //this takes updates from the title editor
            //this takes updates from the audiobooks editor
            count = db.update(AUDIOBOOKS_TABLE_NAME, values, AudiobookColumns._ID + "=" + audiobookId
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
        sUriMatcher.addURI(AudioBookLibraryDatabase.AUTHORITY, "audiobooks", AUDIOBOOKS);
        sUriMatcher.addURI(AudioBookLibraryDatabase.AUTHORITY, "audiobooks/#", AUDIOBOOK_ID);
        sUriMatcher.addURI(AudioBookLibraryDatabase.AUTHORITY, "live_folders/audiobooks", LIVE_FOLDER_AUDIOBOOKS);

        sAudiobooksProjectionMap = new HashMap<String, String>();
        sAudiobooksProjectionMap.put(AudiobookColumns._ID, AudiobookColumns._ID);
        
        sAudiobooksProjectionMap.put(AudiobookColumns.TITLE, AudiobookColumns.TITLE);
        sAudiobooksProjectionMap.put(AudiobookColumns.AUTHOR, AudiobookColumns.AUTHOR);
        sAudiobooksProjectionMap.put(AudiobookColumns.CITATION, AudiobookColumns.CITATION);
        sAudiobooksProjectionMap.put(AudiobookColumns.CLASSIFICATION, AudiobookColumns.CLASSIFICATION);
        sAudiobooksProjectionMap.put(AudiobookColumns.PUBLICATION_DATE, AudiobookColumns.PUBLICATION_DATE);
        sAudiobooksProjectionMap.put(AudiobookColumns.LAST_LISTENED_TIME, AudiobookColumns.LAST_LISTENED_TIME);
        sAudiobooksProjectionMap.put(AudiobookColumns.CHUNKS, AudiobookColumns.CHUNKS);
        sAudiobooksProjectionMap.put(AudiobookColumns.FILENAME, AudiobookColumns.FILENAME);
        sAudiobooksProjectionMap.put(AudiobookColumns.FULL_FILEPATH_AND_FILENAME, AudiobookColumns.FULL_FILEPATH_AND_FILENAME);
        sAudiobooksProjectionMap.put(AudiobookColumns.THUMBNAIL, AudiobookColumns.THUMBNAIL);
        sAudiobooksProjectionMap.put(AudiobookColumns.STARRED, AudiobookColumns.STARRED);

        
        sAudiobooksProjectionMap.put(AudiobookColumns.TASKNOTES, AudiobookColumns.TASKNOTES);
        sAudiobooksProjectionMap.put(AudiobookColumns.CREATED_DATE, AudiobookColumns.CREATED_DATE);
        sAudiobooksProjectionMap.put(AudiobookColumns.MODIFIED_DATE, AudiobookColumns.MODIFIED_DATE);

        // Support for Live Folders.
        sLiveFolderProjectionMap = new HashMap<String, String>();
        sLiveFolderProjectionMap.put(LiveFolders._ID, AudiobookColumns._ID + " AS " +
                LiveFolders._ID);
        sLiveFolderProjectionMap.put(LiveFolders.NAME, AudiobookColumns.TITLE + " AS " +
                LiveFolders.NAME);
        // Add more columns here for more robust Live Folders.
    }

}
