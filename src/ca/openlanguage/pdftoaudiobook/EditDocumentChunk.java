package ca.openlanguage.pdftoaudiobook;

import java.util.StringTokenizer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditDocumentChunk extends Activity {
	
	private EditText mChunkTitle;
	private EditText mChunkText;
	private EditText mChunkHistory;
	private EditText mChunkCorrections;
	

	
	private Button mSaveButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        setContentView(R.layout.edit_chunk);

        fillDocumentDetailsIntoForm();

        mSaveButton = (Button)findViewById(R.id.button_saveDocumentToDatabase);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v){
        		saveToDatabase();
        	}
        });
 }
    
    private void saveToDatabase(){
    	
    	//TODO: insert a record in to the database
    	
    	Toast tellUserSavedDetails = Toast.makeText(this, 
        		"The chunk was saved into the database, next time you open it it will display these details.", Toast.LENGTH_LONG);
        tellUserSavedDetails.show();
    }
    private void fillDocumentDetailsIntoForm(){
    	
    	mChunkTitle = (EditText)findViewById(R.id.ChunkTitle);
    	mChunkText = (EditText)findViewById(R.id.ChunkText);
    	mChunkHistory = (EditText)findViewById(R.id.ChunkHistory);
    	mChunkCorrections = (EditText)findViewById(R.id.ChunkCorrections);
        
        Toast tellUserInfoSource = Toast.makeText(this, 
        		"Document chunk was auto generated \n\n You can make any corrections needed.", Toast.LENGTH_LONG);
        tellUserInfoSource.show();
        
    }

}
