package ca.openlanguage.pdftoaudiobook;





import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DocumentsList extends Activity {

	private TextView mTextView;
    private ListView mListView;



	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.documentList);
        

        
        Intent intent = getIntent();

        Toast tellUser = Toast.makeText(this, 
        		"The intent was "+intent.toString(), Toast.LENGTH_LONG);
        tellUser.show();
        
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion; launches activity to show word
        	tellUser = Toast.makeText(this, 
            		"The intent was an ActionView", Toast.LENGTH_LONG);
            tellUser.show();
            displayResults();

        } 
        mTextView.setText("Opened the list activity.");

        
	}
	private String displayResults(){
		
		
		return "displayed results";
	}

}
