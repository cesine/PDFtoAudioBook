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
package ca.openlanguage.pdftoaudiobook.ui;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import ca.openlanguage.pdftoaudiobook.R;
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryDatabase.AudiobookColumns;


public class DocumentsActivity extends ListActivity {
    private static final String TAG = "AudiobooksList";

    /**
     * The columns we are interested in from the database, Only displaying titles and keeping track
     * of their IDs, but other useful info such as their starred values could be added later
     */
    private static final String[] PROJECTION = new String[] {
        AudiobookColumns._ID, // 0
        AudiobookColumns.TITLE, // 1
        AudiobookColumns.FULL_FILEPATH_AND_FILENAME,//2
        AudiobookColumns.FILENAME,//3
        AudiobookColumns.CHUNKS,//4
        
        
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    private static final int COLUMN_INDEX_FULLFILEPATH_AND_NAME = 2;
    private static final int COLUMN_INDEX_FILENAME = 3;
    private static final int COLUMN_INDEX_CHUNK_SPLITON = 4;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Registered AudioBooks");
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(AudiobookColumns.CONTENT_URI);
        }

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                                        AudiobookColumns.DEFAULT_SORT_ORDER);

        // Used to map Audiobooks entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_item_document, cursor,
                new String[] { AudiobookColumns.TITLE }, new int[] { android.R.id.text1 });
        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);
        
        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, DocumentsEditDetailActivity.class), null, intent, 0, null);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add:
            // Launch activity to insert a new item
        	
        	Intent intent = new Intent(Intent.ACTION_INSERT, getIntent().getData());
            startActivity(intent);
            //page107
//        	String actionName = " ca.openlanguage.pdftoaudiobook.action.EDIT_DOCUMENT_DETAILS ";
//            Intent intent = new Intent(actionName);
//            startActivity(intent);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_audiobook_menu, menu);
        
        // Set the context menu header
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));
        
        // Append to the
        // menu items for any other activities that can do stuff with it
        // as well.  This does a query on the system for any activities that
        // implement the ALTERNATIVE_ACTION for our data, adding a menu item
        // for each one that is found.
        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(), 
                                        Integer.toString((int) info.id) ));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, DocumentsEditDetailActivity.class), null, intent, 0, null);
    }
        
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
        
        Uri audiobookUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

        switch (item.getItemId()) {
        case R.id.context_open:
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, audiobookUri));
            return true;
        case R.id.context_delete:
            // Delete the audiobook that the context menu is for
            getContentResolver().delete(audiobookUri, null, null);
            return true;
        case R.id.context_play:
            // Launch activity to view/edit the currently selected item
            //startActivity(new Intent(Intent.ACTION_EDIT, chunkUri));
//        	mTts.speak("I will play this chunk of the Audio Book.",
//          	        TextToSpeech.QUEUE_ADD,  
//          	        null);
            return true;
        case R.id.context_generateaudio:
            // Launch activity to view/edit the currently selected item
            //startActivity(new Intent(Intent.ACTION_EDIT, chunkUri));
//        	mTts.speak("I will make this audio file for you, please wait.",
//          	        TextToSpeech.QUEUE_ADD, 
//          	        null);
        	Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        	Toast tellUser = Toast.makeText(this, 
            		"Scheduling "+cursor.getString(COLUMN_INDEX_FILENAME)+"audio book for generation.", Toast.LENGTH_LONG);
            tellUser.show();
            return true;
        case R.id.context_generate:
            // Launch the chunks activity but tell it that it should use this Audiobook's id
        	String actionToGenerateChunks="ca.openlanguage.pdftoaudiobook.action.GENERATE_CHUNKS";
        	Intent tempIntent= new Intent(actionToGenerateChunks);//new Intent(this, ChunksActivity.class);
        	cursor = (Cursor) getListAdapter().getItem(info.position);
        	tempIntent.putExtra(AudiobookColumns.FULL_FILEPATH_AND_FILENAME, cursor.getString(COLUMN_INDEX_FULLFILEPATH_AND_NAME));
        	tempIntent.putExtra(AudiobookColumns.FILENAME, cursor.getString(COLUMN_INDEX_FILENAME));
        	tempIntent.putExtra(AudiobookColumns.CHUNKS, cursor.getString(COLUMN_INDEX_CHUNK_SPLITON));
        	//tempIntent.putExtra(AudiobookColumns.TITLE, cursor.getString(COLUMN_INDEX_TITLE));
        	//Uri uriForThisAudioBook = AudiobookColumns.CONTENT_URI;
        	//tempIntent.setData(uriForThisAudioBook);
        	tellUser = Toast.makeText(this, 
            		"Generating chunks for: "+ cursor.getString(COLUMN_INDEX_FILENAME)+"\n\n This may take a while depending on the pdf", Toast.LENGTH_LONG);
            tellUser.show();
        	startActivity(tempIntent);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri audiobookUri = ContentUris.withAppendedId(getIntent().getData(), id);
        
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a audiobook selected by
            // the user.  The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(audiobookUri));
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, audiobookUri));
        }
    }
}
