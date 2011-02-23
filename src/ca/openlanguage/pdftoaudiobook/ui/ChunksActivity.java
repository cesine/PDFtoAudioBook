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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import ca.openlanguage.pdftoaudiobook.R;
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryDatabase.AudiobookColumns;
import ca.openlanguage.pdftoaudiobook.provider.ChunkDatabase.ChunkColumns;


public class ChunksActivity extends ListActivity implements TextToSpeech.OnInitListener{
    private static final String TAG = "chunksList";
    private Boolean mRegisterChunks = false;
    private String mFileName;
    private String mOriginalFileNameAndPath;

	String mSplitOn;
	String mResults;
	Context mParentContext;
	
	
	File mOutputFilePath;
	BufferedReader mOriginalFile;
	
	LinkedHashMap<String,String> chunks = new LinkedHashMap();

	/** Talk to the user */
    private TextToSpeech mTts;

    
    
  //implement on Init for the text to speech
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			int result = mTts.setLanguage(Locale.US);
			// Try this someday for some interesting results.
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Language data is missing or the language is not supported.
				Log.e(TAG, "Language is not available.");
			} else {

				// mSpeakButton.setEnabled(true);
				// mPauseButton.setEnabled(true);
				// Greet the user.
				// sayHello();
			}
		} else {
			// Initialization failed.
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

    
    
    
    
    /**
     * Subset of columns from the database for use by this activity
     */
    private static final String[] PROJECTION = new String[] {
        ChunkColumns._ID, // 0
        ChunkColumns.TITLE, // 1
        ChunkColumns.CHUNKS, //2
        ChunkColumns.FULL_FILEPATH_AND_FILENAME, //3
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    private static final int COLUMN_INDEX_CHUNKTEXT = 2 ;
    private static final int COLUMN_INDEX_FULLPATH_AND_FILENAME = 3 ;
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTts = new TextToSpeech(this, this);
        setTitle("Registered Chunks in this AudioBook");
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        

        Intent intent = getIntent();
       
       
       // If no data was given in the intent (because we were started
       // as a MAIN activity), then use our default content provider.
        if (intent.getData() == null) {
            intent.setData(ChunkColumns.CONTENT_URI);
        }

        String action = intent.getAction();
        
        /*
         * If the activity recieved an intent to generate chunks, open the file, extract chunks and insert them into the database
         * using a bundles of values which can be set immediately, other values are entered as defaults in the content provider
         */
        String actionToGenerateChunks="ca.openlanguage.pdftoaudiobook.action.GENERATE_CHUNKS";
    	if (actionToGenerateChunks.equals(action)){
    		Bundle extras = intent.getExtras();
    		//if txt exists, use that so that the user can edit the txt if the want)
    		mOriginalFileNameAndPath =  extras.getString(AudiobookColumns.FULL_FILEPATH_AND_FILENAME).replace(".pdf",".txt");
    		mFileName = extras.getString(AudiobookColumns.FILENAME);
    		mRegisterChunks = true;
    		
            
            
            String sucessMessage = openFileStreams();
            Toast tellUser = Toast.makeText(this, 
            		sucessMessage, Toast.LENGTH_LONG);
            //tellUser.show();
            mParentContext= this;
            
            
            
            mSplitOn = extras.getString(AudiobookColumns.CHUNKS);
            String digitSections = "\\d+\\.\\d+"; //3.4, 12.22 etc
            sucessMessage=chunkItCompletely(mSplitOn);
            tellUser = Toast.makeText(mParentContext, 
              		sucessMessage, Toast.LENGTH_LONG);
            tellUser.show();

            if (chunks.size() >0){
            	String chunkTitle;
            	String chunkText;
            	//for each chunk in the chunk hashmap
            	Iterator iteratorForChunks = chunks.keySet().iterator();
            	while (iteratorForChunks.hasNext()){
            		chunkTitle = (String)iteratorForChunks.next();
            		chunkText = (String)chunks.get(chunkTitle);
            		
            		/*
            		 * Resegment and clean text chunk
            		 * 
            		 */
            		String cleanedChunk=cleanText(chunkText);
            		
            		
            		ContentValues values = new ContentValues();
            		values.put(ChunkColumns.CHUNKS, "Section");
                	values.put(ChunkColumns.TITLE, chunkTitle);
                	values.put(ChunkColumns.CHUNKS, cleanedChunk);
                	//values.put((ChunkColumns.FILENAME, "audiobooksfilename");
                	values.put(ChunkColumns.FULL_FILEPATH_AND_FILENAME, mOutputFilePath+"/"+chunkTitle.replaceAll(" ","_")+".wav");
                	Uri uriUnusedJustToInsert = getContentResolver().insert(ChunkColumns.CONTENT_URI, values);
            	}
            	
            }//end if there are chunks
    	}//end if to generate chunks
       
        
        
        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                                        ChunkColumns.DEFAULT_SORT_ORDER);

        // Used to map chunks entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_item_chunk, cursor,
                new String[] { ChunkColumns.TITLE }, new int[] { android.R.id.text1 });
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
//        	String actionName = " ca.openlanguchunkaudiobook.action.EDIT_DOCUMENT_DETAILS ";
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
        inflater.inflate(R.menu.list_context_chunk_menu, menu);
        
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
        
        Uri chunkUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

        switch (item.getItemId()) {
        case R.id.context_open:
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, chunkUri));
            return true;
        case R.id.context_delete:
            // Delete the chunk that the context menu is for
            getContentResolver().delete(chunkUri, null, null);
            return true;
        case R.id.context_play:
            // Launch activity to view/edit the currently selected item
            //startActivity(new Intent(Intent.ACTION_EDIT, chunkUri));
        	Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        	String chunk =  cursor.getString(COLUMN_INDEX_CHUNKTEXT);
        	if (chunk.length()>351){
        		chunk = chunk.substring(0,350);
        	}
        	mTts.speak(chunk,
          	        TextToSpeech.QUEUE_ADD,  
          	        null);
            return true;
        case R.id.context_generateaudio:
        	cursor = (Cursor) getListAdapter().getItem(info.position);
        	String chunksFileName= cursor.getString(COLUMN_INDEX_TITLE).replaceAll(" ", "_");
        	chunksFileName =chunksFileName+".wav";
        	chunk =  cursor.getString(COLUMN_INDEX_CHUNKTEXT);
        	if (chunk.length()>3001){
        		chunk = chunk.substring(0,3001);
        	}

        	//File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC+"/Life_of_Pi/");
            //path.mkdirs();
        	/*
        	 * TODO: Using the "audiobook'sfilename" from that chunk, 
        	 * 	look up the current title of the audiobook in the application
        	 *  use that current title to create a directory
        	 *  generate the output to that directory
        	 */
        	
        	Toast tellUser = Toast.makeText(this, 
            		"Generating audio: "+cursor.getString(COLUMN_INDEX_FULLPATH_AND_FILENAME)+" into folder: ", Toast.LENGTH_LONG);
            tellUser.show();
            	
            	mTts.synthesizeToFile(chunk,
            			null,  
            			cursor.getString(COLUMN_INDEX_FULLPATH_AND_FILENAME));//"/sdcard/Music/Life_of_Pi/Chapter_13.wav"); //tried changing to path variable but didnt work.
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri chunkUri = ContentUris.withAppendedId(getIntent().getData(), id);
        
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a chunk selected by
            // the user.  The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(chunkUri));
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, chunkUri));
        }
    }

    public void onGenerateClick(View v){
    	  
    	
          mTts.speak("I will make this audio file for you, please wait.",
      	        TextToSpeech.QUEUE_ADD, 
      	        null);
    }
    public void onPlayClick(View v){
    	 
          mTts.speak("I will play this chunk of the Audiobook.",
      	        TextToSpeech.QUEUE_ADD,  
      	        null);
    }
    public String cleanText(String stringIn){
    	stringIn= stringIn.replaceAll("Fig.", "figure ");
    	String stringOut = "";
    	StringTokenizer tokens = new StringTokenizer(stringIn,".",true);
		while (tokens.hasMoreTokens()){
			stringOut = stringOut + "\n" + tokens.nextToken().replaceAll("\n"," ").replaceAll("- ", "") ;
		}
    	return stringOut;
    }
    
    public String openFileStreams(){
		String message="";
		/*
		 * Accessing the SDCARD
		 */
		boolean externalStorageAvailable = false;
		boolean externalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            externalStorageAvailable = externalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            externalStorageAvailable = true;
            externalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            externalStorageAvailable = externalStorageWriteable = false;

        	Toast tellUserSDCARDproblem = Toast.makeText(this, 
            		"The SDCARD is unavailible, please try again later.\n\n Is the phone attached to a computer?", Toast.LENGTH_LONG);
            tellUserSDCARDproblem.show();
            message ="The SDCARD is unavailible, please try again later.\n\n Is the phone attached to a computer?";
        }

        /*
         * Open and set the file stream for the original text file
         */
        if (externalStorageAvailable == true ){
        	//text =" The External Storage is Readable. ";
        	try {
    			mOriginalFile = new BufferedReader(new FileReader(mOriginalFileNameAndPath));
    		} catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		message+="\nFile path for original is okay. \n";
        }
        
		/*
		 * Open and create the file path for the output directory in the music folder
		 * 
		 * Location: Music folder
		 * Convention: remove .pdf or .txt from the filename, and replaces spaces" " by underscores"_"
		 * 
		 * usage of the mOutputFilePath:
		 * //File file = new File (mOutputFilePath, "Chapter_13.wav");
		 */
        if (externalStorageWriteable == true ){
        	//text =" The External Storage is Writable. ";
        	String directoryName = mFileName.replace(".pdf", "");
        	directoryName = directoryName.replace(".txt", "");
        	directoryName = directoryName.replaceAll(" ", "_");
        	mOutputFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC+"/"+directoryName+"/");
            
        	/*
        	 * TODO: fix bug, directory is not being made
        	 */
        	mOutputFilePath.mkdirs();     
            message+="\nFile path for output is okay (it's in the Music directory). \n";
        }
       
		return message;
	}
    public String chunkItCompletely(String splitOn){
		
		String chunkString = "";
		String lineBreak ="\n";
		String chunkName = "Section00"+mFileName.replace(".pdf", ".txt");
		//chunks.put(chunkName, chunkString);
		
		String message="";
		
		
		String line;
		try {
			while ((line = mOriginalFile.readLine()) != null) {
				/*
				 * If the line matches the split regex: 
				 * 	1 Put the chunk into the HashMap
				 *  2 Reset chunk contents, either 
				 *  	-make a new file out or
				 * 		-clear the string
				 */
				//String digitSections = "\\d+\\.\\d+"; //3.4, 12.22 etc
				if (line.trim().startsWith(mSplitOn) && mSplitOn.length()>3 ) {
					chunks.put(chunkName, chunkString);
					//Toast tellUser = Toast.makeText(mParentsContext, 
		            //		"The Chunk Name: "+chunkName+":\n\n"+chunkString, Toast.LENGTH_LONG);
		            //tellUser.show();
		            
					chunkString = "";
					chunkName = line.trim().replaceAll(" ", "_");
					if(chunkName.length()>21){
						message = message +chunkName.substring(0,20) + lineBreak;
					}else{
						message = message + chunkName + lineBreak;
					}
				}
				/*
				 * Add the line to the chunk, 
				 * 	either by writing out to the file or // out.append(line);
				 * 	adding it to the string
				 */
				
				chunkString = chunkString + line + lineBreak;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		chunks.put(chunkName, chunkString);
		
        return "Chunked on "+splitOn+" the result is: \n\n"+chunks.size()+" chunks.\n"+message;
	}

}
