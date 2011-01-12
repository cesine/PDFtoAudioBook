package ca.openlanguage.pdftoaudiobook.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import ca.openlanguage.pdftoaudiobook.R;
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryDatabase.AudiobookColumns;

public class DocumentsEditDetailActivity extends Activity{
    private static final String TAG = "AudiobookEditor";

    /**
     * Standard projection for the interesting columns of a normal audiobook.
     * 
     * Originally just title and content, no extra meta data or details
     */
    private static final String[] PROJECTION = new String[] {
        AudiobookColumns._ID, // 0
        AudiobookColumns.AUDIOBOOK, // 1
        AudiobookColumns.TITLE, // 2
        AudiobookColumns.AUTHOR, //3
        AudiobookColumns.CITATION, //4
        AudiobookColumns.CLASSIFICATION, //5
        AudiobookColumns.CHUNKS, //6
        AudiobookColumns.LAST_LISTENED_TIME, //7
        AudiobookColumns.FILENAME, //8
        AudiobookColumns.FULL_FILEPATH_AND_FILENAME, //9
        AudiobookColumns.PUBLICATION_DATE, //10
        AudiobookColumns.THUMBNAIL, //11
        AudiobookColumns.STARRED, //12
        AudiobookColumns.AUDIOBOOK, //13
    };

    /** 
     * The index of the columns in teh PROJECTION (above) and will be used to 
     * pull the right strings out of the right positions in the cursor
     */
    private static final int COLUMN_INDEX_AUDIOBOOK = 1;
    private static final int COLUMN_INDEX_TITLE = 2;
    private static final int COLUMN_INDEX_AUTHOR = 3;
    private static final int COLUMN_INDEX_CITATION = 4;
    private static final int COLUMN_INDEX_CLASSIFICATION = 5;
    private static final int COLUMN_INDEX_CHUNKS = 6;
    private static final int COLUMN_INDEX_LAST_LISTENED_TIME = 7;
    private static final int COLUMN_INDEX_FILENAME = 8;
    private static final int COLUMN_INDEX_FULL_FILEPATH_AND_FILENAME = 9;
    private static final int COLUMN_INDEX_PUBLICATION_DATE = 10;
    private static final int COLUMN_INDEX_THUMBNAIL = 11;
    private static final int COLUMN_INDEX_STARRED = 12;
    
    //not needed
    //private static final int COLUMN_INDEX_CREATED_DATE = 14;
    //private static final int COLUMN_INDEX_MODIFIED_DATE = 15;
    
    /*
     * These are the constants which are put into a state bundle to identify the string contents
     * which are preserved eg, origTitle is th key for the the value "THeory of pumpkins"
     */
    private static final String ORIGINAL_CONTENT = "origContent";
    private static final String ORIGINAL_TITLE = "origTitle";
    private static final String ORIGINAL_AUTHOR = "origAuthor";
    private static final String ORIGINAL_CITATION = "origCitation";
    private static final String ORIGINAL_CLASSIFICATION = "origClassification";
    private static final String ORIGINAL_PUBDATE = "origPubDate";
    private static final String ORIGINAL_LASTLISTENEDTIME = "origLastListenedTime";
    private static final String ORIGINAL_CHUNKS = "origChunks";
    private static final String ORIGINAL_FILENAME = "origFilename";
    private static final String ORIGINAL_FULLFILEPATHANDNAME = "origFullPathAndFilename";
    private static final String ORIGINAL_THUMBNAIL = "origThumbnail";
    private static final String ORIGINAL_STARRED = "origStarred";

    
    
    // The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;//the audiobook field
    private EditText mTitleEditText;
    private EditText mAuthorEditText;
    private EditText mCitationEditText;
    private EditText mClassificationEditText;
    private EditText mPubDateEditText;
    private EditText mLastListenedTimeEditText;//note used
    private EditText mChunksEditText;
    private EditText mFileNameEditText;
    private EditText mFullFilePathAndFileNameEditText;
    private EditText mThumbnailEditText;//probably won't be displayed
    private EditText mStarredEditText;//should be checkbox

    //a holder for the original text (prior to user edits) for the main content of the audiobook, 
    //make more Strings like this one for the other columns
    private String mOriginalContent;
    private String mOriginalTitle;
    private String mOriginalAuthor;
    private String mOriginalCitation;
    private String mOriginalClassification;
    private String mOriginalPubDate;
    private String mOriginalLastListenedTime;
    private String mOriginalChunks;
    private String mOriginalFileName;
    private String mOriginalFullFilePathAndFileName;
    private String mOriginalThumbnail;
    private String mOriginalStarred;

    /**
     * A custom EditText that draws lines between each line of text that is displayed.
     */
    public static class LinedEditText extends EditText {
        private Rect mRect;
        private Paint mPaint;

        // we need this constructor for LayoutInflater
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            int count = getLineCount();
            Rect r = mRect;
            Paint paint = mPaint;

            for (int i = 0; i < count; i++) {
                int baseline = getLineBounds(i, r);

                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }

            super.onDraw(canvas);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        // Do some setup based on the action being performed.
        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            // Requested to insert: set that state, and create a new entry
            // in the container.
            mState = STATE_INSERT;
            //this is run when add audiobook is called, prior to an data being entered.
            mUri = getContentResolver().insert(intent.getData(), null);

            // If we were unable to create a new audiobook, then just finish
            // this activity.  A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (mUri == null) {
                Log.e(TAG, "Failed to insert new audiobook into " + getIntent().getData());
                finish();
                return;
            }

            // The new entry was created, so assume all will end well and
            // set the result to be returned.
            //The result can then be filled in by the application into a full audiobook
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

        } else {
            // Whoops, unknown action!  Bail.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        // Set the layout for this activity.  You can find it in res/layout/audiobook_editor.xml
        setContentView(R.layout.audiobook_editor);
        
        // The text view for our audiobook, identified by its ID in the XML file.
        mText = (EditText) findViewById(R.id.audiobook);
        
        
        mTitleEditText = (EditText) findViewById(R.id.audiobookTitle);
        mAuthorEditText = (EditText) findViewById(R.id.audiobookAuthor);
        mCitationEditText = (EditText) findViewById(R.id.audiobookCitations);
        mClassificationEditText = (EditText) findViewById(R.id.audiobookClassification);
        mPubDateEditText = (EditText) findViewById(R.id.audiobookPublicationDate);
        //mLastListenedTimeEditText;
        mChunksEditText = (EditText) findViewById(R.id.audiobookChunks);
        mFileNameEditText = (EditText) findViewById(R.id.audiobookFileName);
        mFullFilePathAndFileNameEditText = (EditText) findViewById(R.id.audiobookFullFilePathandFileName);
        //mThumbnailEditText;
        //mStarredEditText;


        
        
        
        /*
         * Get the audiobook details using the id which is in mUri
         * 
         * get the columns listed in PROJECTION, they need to match the order
         *  given in COLUMN_INDEX
         */
        mCursor = managedQuery(mUri, PROJECTION, null, null, null);

        // If an instance of this activity had previously stopped, we can still
        // get the original text it started with before the user pushed back or te activity was paused. i
        // ie can still discard/cancel edits that the user doesnt think were made. 
        /*
         * asks the savedInstantState for the key at the entry of the constant defined
         * in ORIGINAL_*****columname*** ?
         */
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
            mOriginalTitle = savedInstanceState.getString(ORIGINAL_TITLE);
            mOriginalAuthor = savedInstanceState.getString(ORIGINAL_AUTHOR);
            mOriginalCitation = savedInstanceState.getString(ORIGINAL_CITATION);
            mOriginalClassification = savedInstanceState.getString(ORIGINAL_CLASSIFICATION);
            mOriginalPubDate = savedInstanceState.getString(ORIGINAL_PUBDATE);
            mOriginalLastListenedTime = savedInstanceState.getString(ORIGINAL_LASTLISTENEDTIME);
            mOriginalChunks = savedInstanceState.getString(ORIGINAL_CHUNKS);
            mOriginalFileName = savedInstanceState.getString(ORIGINAL_FILENAME);
            mOriginalFullFilePathAndFileName = savedInstanceState.getString(ORIGINAL_FULLFILEPATHANDNAME);
            mOriginalThumbnail = savedInstanceState.getString(ORIGINAL_THUMBNAIL);
            mOriginalStarred = savedInstanceState.getString(ORIGINAL_STARRED);

        }
    }

    /**
     * this method is called very often, each time the details are displayed.
     * 
     * It first calls the super's onResume (from activity)
     * 
     * Then it provides a user friendly title change
     * 
     * Then it preserves the all edit text fields in case the user cancels their edits
     * 
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        
        /*
         * Expect the mCursor to contain one row with the audiobook details
         * 
         * Frequent:
         */
        if (mCursor != null) {
            // Requery in case something changed while paused (such as the title)
            mCursor.requery();
            // Make sure we are at the one and only row in the cursor.
            mCursor.moveToFirst();

            /*
             * Modify our activity's title depending on the mode we are running in.
             * STATE_EDIT: "Editing The theory of pumpkins"
             * STATE_INSERT: "Creating a new Audiobook"
             */
            if (mState == STATE_EDIT) {
                String title = mCursor.getString(COLUMN_INDEX_TITLE);
                Resources res = getResources();
                setTitle("Editing "+title);//title of activity
            } else if (mState == STATE_INSERT) {
                setTitle("In the Insert state"); //title of activity
            }

            // This is a little tricky: we may be resumed after previously being
            // paused/stopped.  We want to put the new text in the text view,
            // but leave the user where they were (retain the cursor position
            // etc).  This version of setText does that for us.
            
            /*
             * The resume function redisplays the edit details after the app has been 
             * paused, so if the user wants to cancel their edits the original content
             * should be saved here first.
             * 
             * This gets the string for each column, sets the TextKeepState on each edittext
             * and also puts the text into a member variable of the object called mOriginalContent
             * 
             * Should only do this if previous OriginalContent doesnt exist, so and an if...
             * 
             */
            String audiobook = mCursor.getString(COLUMN_INDEX_AUDIOBOOK);
            mText.setTextKeepState(audiobook);
            mOriginalContent = audiobook;
            
            String title = mCursor.getString(COLUMN_INDEX_TITLE);
            mTitleEditText.setTextKeepState(title);
            mOriginalTitle = title;
            
            
            String author = mCursor.getString(COLUMN_INDEX_AUTHOR);
            mAuthorEditText.setTextKeepState(author);
            mOriginalAuthor = author;
            
            String citations = mCursor.getString(COLUMN_INDEX_CITATION);
            mCitationEditText.setTextKeepState(citations);
            mOriginalCitation = citations;
            
            String classifications = mCursor.getString(COLUMN_INDEX_CLASSIFICATION);
            mClassificationEditText.setTextKeepState(classifications);
            mOriginalClassification = classifications;
                        
            String pubdate = mCursor.getString(COLUMN_INDEX_PUBLICATION_DATE);
            mPubDateEditText.setTextKeepState(pubdate);
            mOriginalPubDate = pubdate;
            
            String lastlistenedtime = mCursor.getString(COLUMN_INDEX_LAST_LISTENED_TIME);
            //mLastListenedTimeEditText.setTextKeepState(lastlistenedtime);
            mOriginalLastListenedTime = lastlistenedtime;
            
            String chunks = mCursor.getString(COLUMN_INDEX_CHUNKS);
            mChunksEditText.setTextKeepState(chunks);
            mOriginalChunks = chunks;
            
            String filename = mCursor.getString(COLUMN_INDEX_FILENAME);
            mFileNameEditText.setTextKeepState(filename);
            mOriginalFileName = filename;
            
            String fullpathandfilename = mCursor.getString(COLUMN_INDEX_FULL_FILEPATH_AND_FILENAME);
            mFullFilePathAndFileNameEditText.setTextKeepState(fullpathandfilename);
            mOriginalFullFilePathAndFileName = fullpathandfilename;
                        
            String thumbnail = mCursor.getString(COLUMN_INDEX_THUMBNAIL);
            //mThumbnail.setTextKeepState(fullpathandfilename);
            mOriginalThumbnail = thumbnail;
            
            String starred = mCursor.getString(COLUMN_INDEX_STARRED);
            //mStarred.setTextKeepState(fullpathandfilename);
            mOriginalStarred = starred;
            


        } else {
        	/*
        	 * if there is no content in the row supplied by mUri's id
        	 * 
        	 * Rare:
        	 */
            setTitle(getText(R.string.error_title));
            mText.setText(getText(R.string.error_message));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
        outState.putString(ORIGINAL_TITLE, mOriginalTitle);
        outState.putString(ORIGINAL_AUTHOR, mOriginalAuthor);
        outState.putString(ORIGINAL_CITATION, mOriginalCitation);
        outState.putString(ORIGINAL_CLASSIFICATION, mOriginalClassification);
        outState.putString(ORIGINAL_PUBDATE, mOriginalPubDate);
        outState.putString(ORIGINAL_LASTLISTENEDTIME, mOriginalLastListenedTime);
        outState.putString(ORIGINAL_CHUNKS, mOriginalChunks);
        outState.putString(ORIGINAL_FILENAME, mOriginalFileName);
        outState.putString(ORIGINAL_FULLFILEPATHANDNAME, mOriginalFullFilePathAndFileName);
        outState.putString(ORIGINAL_THUMBNAIL, mOriginalThumbnail);
        outState.putString(ORIGINAL_STARRED, mOriginalStarred);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // The user is going somewhere, so make sure changes are saved

            saveAudiobook();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_options_menu, menu);

        // Append to the
        // menu items for any other activities that can do stuff with it
        // as well.  This does a query on the system for any activities that
        // implement the ALTERNATIVE_ACTION for our data, adding a menu item
        // for each one that is found.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, DocumentsEditDetailActivity.class), null, intent, 0, null);

        return super.onCreateOptionsMenu(menu);
    }
    
    

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mState == STATE_EDIT) {
            menu.setGroupVisible(R.id.menu_group_edit, true);
            menu.setGroupVisible(R.id.menu_group_insert, false);
            
            // Check if audiobook has changed and enable/disable the revert option
            //TODO change the logic to make revert act like an undo action on one field.
            String savedAudiobook = mCursor.getString(COLUMN_INDEX_AUDIOBOOK);
            String currentAudiobook = mText.getText().toString();
            if (savedAudiobook.equals(currentAudiobook)) {
                menu.findItem(R.id.menu_revert).setEnabled(false);
            } else {
                menu.findItem(R.id.menu_revert).setEnabled(true);
            }
        } else {
            menu.setGroupVisible(R.id.menu_group_edit, false);
            menu.setGroupVisible(R.id.menu_group_insert, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
        case R.id.menu_save:
            saveAudiobook();
            finish();
            break;
        case R.id.menu_delete:
            deleteAudiobook();
            finish();
            break;
        case R.id.menu_revert:
        case R.id.menu_discard:
            cancelAudiobook();
            break;
        }
        return super.onOptionsItemSelected(item);
        
    }
    
    /*
     * handles the save button as defined in its properties
     * android:onClick="onSaveClick"
     * 
     * the save button is pretty unneseary but the users like it.
     */
    public void onSaveClick(View v) {
        saveAudiobook();//saveContent();
    }

    /*
     * handles the discard button as defined in its properties
     * android:onClick="onDiscardClick"
     */
    public void onDiscardClick(View v) {
    	//cancel audiobook undo's the user edits
    	cancelAudiobook();
    }
    
    private final void saveAudiobook() {
        // Make sure their current
        // changes are safely saved away in the provider.  We don't need
        // to do this if only editing. TODO what does that mean ?this is where i put the save logic,maybe by default it saves the right way..
        if (mCursor != null) {
            
            ContentValues values = new ContentValues();
            // Bump the modification time to now.
            values.put(AudiobookColumns.MODIFIED_DATE, System.currentTimeMillis());


            /*
             *  Write the contents of the edit texts back into the provider.
             */
            //TOD put the other edit fields here
            values.put(AudiobookColumns.AUDIOBOOK, mText.getText().toString());
            values.put(AudiobookColumns.TITLE, mTitleEditText.getText().toString());
            
            //put all the fields (except the metadata fields) into the values to update row in the database
            values.put(AudiobookColumns.AUTHOR, mAuthorEditText.getText().toString());
            values.put(AudiobookColumns.CITATION, mCitationEditText.getText().toString());
            values.put(AudiobookColumns.CLASSIFICATION, mClassificationEditText.getText().toString());
            values.put(AudiobookColumns.PUBLICATION_DATE, mPubDateEditText.getText().toString());
            //values.put(AudiobookColumns.LAST_LISTENED_TIME, mLastListenedTimeEditText.getText().toString());
            values.put(AudiobookColumns.CHUNKS, mChunksEditText.getText().toString());
            values.put(AudiobookColumns.FILENAME, mFileNameEditText.getText().toString());
            values.put(AudiobookColumns.FULL_FILEPATH_AND_FILENAME, mFullFilePathAndFileNameEditText.getText().toString());
            //values.put(AudiobookColumns.THUMBNAIL, mThumbnailEditText.getText().toString());
            //values.put(AudiobookColumns.STARRED, mStarredEditText.getText().toString());

            //what about the create tiem etc?, how does this relate to the index of the column?
            

            // Commit all of our changes to persistent storage. When the update completes
            // the content provider will notify the cursor of the change, which will
            // cause the UI to be updated.
            /*
             * the question is, what about the columns that are missing?
             */
          
            try {
                getContentResolver().update(mUri, values, null, null);
            } catch (NullPointerException e) {
                Log.e(TAG, e.getMessage());
            }
            
        }
    }

    /**
     * Take care of canceling work on a audiobook.  Deletes the audiobook if we
     * had created it, otherwise reverts to the original text.
     */
    private final void cancelAudiobook() {
        if (mCursor != null) {
            if (mState == STATE_EDIT) {
                // Put the original audiobook text back into the database
                mCursor.close();
                mCursor = null;
                ContentValues values = new ContentValues();
                /* 
                 * put other content columns here too
                 */
                values.put(AudiobookColumns.AUDIOBOOK, mOriginalContent);
                values.put(AudiobookColumns.TITLE, mOriginalTitle);
                values.put(AudiobookColumns.AUTHOR, mOriginalAuthor);
                values.put(AudiobookColumns.CLASSIFICATION, mOriginalClassification);
                values.put(AudiobookColumns.CITATION, mOriginalCitation);
                values.put(AudiobookColumns.CHUNKS, mOriginalChunks);
                values.put(AudiobookColumns.LAST_LISTENED_TIME, mOriginalLastListenedTime);
                values.put(AudiobookColumns.FILENAME, mOriginalFileName);
                values.put(AudiobookColumns.FULL_FILEPATH_AND_FILENAME, mOriginalFullFilePathAndFileName);
                values.put(AudiobookColumns.PUBLICATION_DATE, mOriginalPubDate);
                values.put(AudiobookColumns.THUMBNAIL, mOriginalThumbnail);
                values.put(AudiobookColumns.STARRED, mOriginalStarred);

                /*
                 * this originally contianed only the text, not the title (nor the metadata)
                 * what happens with the columns that are not modified? how does the contentresolver
                 * know which values in teh original PROJECTION were not modified?
                 * 
                 * the documenation says that teh values are a bundle maping from column names, to new values. 
                 * essentially resulting in an update command per each pair in the values bundle,
                 * so its not one big update command. it seems like one command because it matches the id in the URI so effectively
                 * only one row should get changed. 
                 * 
                 * This makes it robust to errors where a column is forgotten in the update, it simply wont get updated
                 * but it wont cause teh application to crash
                 */
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                // We inserted an empty audiobook, make sure to delete it
                deleteAudiobook();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Take care of deleting a audiobook.  Simply deletes the entry.
     */
    private final void deleteAudiobook() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mText.setText("");
        }
    }

}
