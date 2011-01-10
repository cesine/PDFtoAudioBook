package ca.openlanguage.pdftoaudiobook.ui;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ca.openlanguage.pdftoaudiobook.R;
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryContract.Documents;

import com.google.android.apps.iosched.util.NotifyingAsyncQueryHandler;
import com.google.android.apps.iosched.util.NotifyingAsyncQueryHandler.AsyncQueryListener;

public class DocumentsEditDetailActivity extends Activity implements AsyncQueryListener{

//private static final String TAG = "RegisterPDFActivity";
	
	private String fullPathAndFileName;
	private String fileName;
	private EditText mFilePathEditText;
	private EditText mFileNameEditText;
	private EditText mDocTitleEditText;
	private EditText mDocAuthorEditText;
	private EditText mDocCitationsEditText;
	private EditText mDocPubDateEditText;
	private EditText mDocChunksText;
	
	private Button mSaveButton;
	
	private NotifyingAsyncQueryHandler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_documents_editdetail);
		
        /*
        //the save button functionality is implemented by the element 
        // android:onClick="onSaveClick" in the layout, and teh function below which matches its name
        mSaveButton = (Button)findViewById(R.id.button_saveDocumentToDatabase);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v){
        		//saveToDatabase();
        	}
        });
        */
		mDocTitleEditText = (EditText)findViewById(R.id.documentTitle);
        
		/* 
		 * Do new pdf registration specific actions
		 */
		//getPDFFileNameAndPath();
        //fillDocumentDetailsIntoForm();
        
        
        /*The NotifyingAsyncQueryHandler is a class from ioschedule utils which is pending API council review
         * Slightly more abstract {@link AsyncQueryHandler} that helps keep a
         * {@link WeakReference} back to a listener. Will properly close any
         * {@link Cursor} if the listener ceases to exist.
         * 
         * the asyncqueryhandler takes a contextresolver in its constructor, which in turn connects to the provider
         * 
         * <p>
         * This pattern can be used to perform background queries without leaking
         * {@link Context} objects.
         */
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
        
        /*
         * prepare and execute instructions from the intents/uri
         */
        final String action = getIntent().getAction();
        if (Intent.ACTION_EDIT.equals(action) && savedInstanceState == null) {
            // Start background query to load current state
            final Uri documentUri = getIntent().getData();
            mHandler.startQuery(documentUri, DocumentsQuery.PROJECTION);
        }
        

        
	}//end onCreate

	/**
     * Return file name and path.
     * @return string
     */
    private void getPDFFileNameAndPath() {
        final Intent intent = getIntent();
		Uri uri = intent.getData();    	
		fullPathAndFileName = uri.getPath().toString();
		
		int lastPosition = uri.getPathSegments().size() - 1 ;
		fileName = uri.getPathSegments().get(lastPosition);
		
		if (uri.getScheme().equals("file")) {
			return ;//fullPathAndFileName;
    		//return new PDF(new File(fullPathAndFileName));
    	} else if (uri.getScheme().equals("content")) {
    		ContentResolver cr = this.getContentResolver();
    		FileDescriptor fileDescriptor;
			try {
				fileDescriptor = cr.openFileDescriptor(uri, "r").getFileDescriptor();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e); // TODO: handle errors
			}
			fileName = "Unknown - 2010 - Unknown.pdf";
			fullPathAndFileName = "Unknown - 2010 - Unknown.pdf";//fileDescriptor.toString();
			return ;//fileDescriptor.toString();
    		//return new PDF(fileDescriptor);
    	} else {
    		throw new RuntimeException("don't know how to get filename from " + uri);
    	}
    }
    private void fillDocumentDetailsIntoForm(){
    	
        mFilePathEditText = (EditText)findViewById(R.id.filePathText);
        mFilePathEditText.setText(fullPathAndFileName, TextView.BufferType.EDITABLE);
        mFileNameEditText = (EditText)findViewById(R.id.fileNameText);
        mFileNameEditText.setText(fileName, TextView.BufferType.EDITABLE);
        
        //divide filename on hyphens -, replace underscores with spaces
        StringTokenizer fileNameSections = new StringTokenizer(fileName.replaceAll("_", " "), "-");
        
        //assume the filename is in format author - date - title
        //later use metadata and the actual text to extract information
        String author = fileNameSections.nextToken().replaceAll(",", " and");
        String date = fileNameSections.nextToken();
        String title = fileNameSections.nextToken().replace(".pdf", "");
        String citations = ""+author+" "+date;
        
        mDocTitleEditText = (EditText)findViewById(R.id.documentTitle);
        mDocAuthorEditText = (EditText)findViewById(R.id.documentAuthor);
        mDocPubDateEditText = (EditText)findViewById(R.id.documentPublicationDate);
        mDocCitationsEditText = (EditText)findViewById(R.id.documentCitations);
        
        mDocTitleEditText.setText(title);
        mDocAuthorEditText.setText(author);
        mDocPubDateEditText.setText(date);
        mDocCitationsEditText.setText(citations);
        
        Toast tellUserInfoSource = Toast.makeText(this, 
        		"Document info was auto-filled based on the file name. \n\n You can make any corrections needed.", Toast.LENGTH_LONG);
        tellUserInfoSource.show();
        
    }
    
    /*
     * functions which are needed to connect to the provider and other aspects of the app
     * 
     */
   
    /* to pop a menu for confirming discards
     * requires a layout or dialog somewhere called "dialog_discard_confirm"
    @Override
    protected Dialog onCreateDialog(int id){
    	return null;
    }
    */
    public void onSaveClick(View v){
    	saveContent();
    }
    public void saveContent(){
    	final String documentContent = mDocTitleEditText.getText().toString();
    	
    	//TODO: consider passing it off to a startSerivice() to prevent the process from being killed
    	//eg by an incoming call before actually perserving the document
    	
    	//when empty title treat it as a discard, add code here
    	
    	final ContentValues values = new ContentValues();
    	values.put(Documents.DOCUMENT_TITLE,"test title");
    	values.put(Documents.DOCUMENT_AUTHOR, "test author");
    	//etc
    	
    	final String action = getIntent().getAction();
    	//if its an insert, set the document added time, otherwise just save it using update
    	if (Intent.ACTION_INSERT.equals(action)){
    		values.put(Documents.DOCUMENT_ADDED_TIME, System.currentTimeMillis());
    	
    		final Uri documentsDirUri = getIntent().getData();
    		//here is the connection to the database, via a handler, via a content resolver, via a provider using the Contract about data constants set out in AudioBookLibraryContract, formerly the metadata from hashimi book
    		/*
    		 * more info about the startInsert and startUpdate are wrapped in the NotifyingAsyncQueryHandler in the utils package, and documneted in the asyncqueryhandler in the android documenation
    		 * an asyncqueryhandler takes a conentresolver in its constructor
    		 */
    		mHandler.startInsert(documentsDirUri, values);
    	}else if (Intent.ACTION_EDIT.equals(action)){
    		final Uri documentUri = getIntent().getData();
    		mHandler.startUpdate(documentUri, values);
    	}
    	finish();
    }
    
    /**
     * onQueryComplete is required by the AsyrconousQueryListener
     */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        try {
            if (!cursor.moveToFirst()) return;

            // Load current note content for editing
           mDocTitleEditText.setText(cursor.getString(DocumentsQuery.DOCUMENT_TITLE));

        } finally {
            cursor.close();
        }
    }
    /** {@link Notes} query parameters. */
    private interface DocumentsQuery {
        String[] PROJECTION = {
                Documents.DOCUMENT_ADDED_TIME,
                Documents.DOCUMENT_TITLE,
        };

        int DOCUMENT_ADDED_TIME = 0;
        int DOCUMENT_TITLE = 1;
    }
}
