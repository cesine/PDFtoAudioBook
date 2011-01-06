package ca.openlanguage.pdftoaudiobook;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ca.openlanguage.pdftoaudiobook.Text2Chunks;

public class RegisterPDFActivity extends Activity {
	
	//private static final String TAG = "RegisterPDFActivity";
	
	private String fullPathAndFileName;
	private String fileName;
	private EditText mFilePathEditText;
	private EditText mFileNameEditText;
	private EditText mDocTitleEditText;
	private EditText mDocAuthorEditText;
	private EditText mDocCitationsEditText;
	private EditText mDocPubDateEditText;
	
	private Button mSaveButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        setContentView(R.layout.register_pdf);
        getPDFFileNameAndPath();
        fillDocumentDetailsIntoForm();

        mSaveButton = (Button)findViewById(R.id.button_saveDocumentToDatabase);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v){
        		saveToDatabase();
        	}
        });
        
        //test Text2Chunks
        //Depends on: txt file being next to the pdf
        Text2Chunks text2ChunksInstance = new Text2Chunks(this, fileName, fullPathAndFileName);
        String sucessMessage = text2ChunksInstance.openFileStreams();
        Toast tellUserFileStreamResults = Toast.makeText(this, 
        		sucessMessage, Toast.LENGTH_LONG);
        tellUserFileStreamResults.show();
        
        
        //if doing chunkit in a thread to use a progress bar
        //sucessMessage = "depreciated, use mResults instead";
        //text2ChunksInstance.setSplitOn("CHAPTEr");
        //text2ChunksInstance.chunkIt();
        //sucessMessage =text2ChunksInstance.getChunkResults();
        
        //if chunkitcompletely is public to use a progress dialog
        sucessMessage=text2ChunksInstance.chunkItCompletely("CHAPTEr");
        
        
        tellUserFileStreamResults = Toast.makeText(this, 
        		sucessMessage, Toast.LENGTH_LONG);
        tellUserFileStreamResults.show();
    }
    
    private void saveToDatabase(){
    	
    	//TODO: insert a record in to the database
    	
    	Toast tellUserSavedDetails = Toast.makeText(this, 
        		"The Document was saved into the database, next time you open it it will display these details.", Toast.LENGTH_LONG);
        tellUserSavedDetails.show();
    }
    
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
}
