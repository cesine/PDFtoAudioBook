package ca.openlanguage.pdftoaudiobook;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterPDFActivity extends Activity {
	
	private String filePath;
	private String fileName;
	private EditText mFilePathEditText;
	private EditText mFileNameEditText;
	private EditText mDocTitleEditText;
	private EditText mDocAuthorEditText;
	private EditText mDocCitationsEditText;
	private EditText mDocPubDateEditText;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        setContentView(R.layout.register_pdf);
        filePath = getPDFFilePath();
        
        mFilePathEditText = (EditText)findViewById(R.id.filePathText);
        mFilePathEditText.setText(filePath, TextView.BufferType.EDITABLE);
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
        
        
        //TextView message = (TextView)findViewById(R.id.message);
        //message.setText("hi");
    }
    /**
     * Return file name and path.
     * @return string
     */
    private String getPDFFilePath() {
        final Intent intent = getIntent();
		Uri uri = intent.getData();    	
		filePath = uri.getPath().toString();
		int lastPosition = uri.getPathSegments().size() - 1 ;
		fileName = uri.getPathSegments().get(lastPosition);
		if (uri.getScheme().equals("file")) {
			return filePath;
    		//return new PDF(new File(filePath));
    	} else if (uri.getScheme().equals("content")) {
    		ContentResolver cr = this.getContentResolver();
    		FileDescriptor fileDescriptor;
			try {
				fileDescriptor = cr.openFileDescriptor(uri, "r").getFileDescriptor();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e); // TODO: handle errors
			}
			return fileDescriptor.toString();
    		//return new PDF(fileDescriptor);
    	} else {
    		throw new RuntimeException("don't know how to get filename from " + uri);
    	}
    }
}
