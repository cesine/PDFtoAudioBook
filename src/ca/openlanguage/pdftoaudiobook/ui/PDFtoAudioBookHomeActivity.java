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

import java.net.URI;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import ca.openlanguage.pdftoaudiobook.R;

public class PDFtoAudioBookHomeActivity extends Activity implements TextToSpeech.OnInitListener {
	private static final String TAG = "PDFtoAudioBookHomeActivity";
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
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        

        mTts = new TextToSpeech(this, this);
        
        loadTipsAndTricks();

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		//Case where user clicked on menu add
		if (requestCode ==1){
			if(resultCode == RESULT_OK){
		        String actionToRegisterPdf="android.intent.action.VIEW";
		    	
				Uri fullpath = data.getData();

		    	Toast tellUser = Toast.makeText(this, 
		        		"OI File manager came back with \n"+fullpath.toString(), Toast.LENGTH_LONG);
		        tellUser.show();
		        Intent intentRegisterPDF= new Intent(actionToRegisterPdf,fullpath);//Intent.ACTION_VIEW);//new Intent(this, ChunksActivity.class);
		    	/*
		    	 * Fix bug: No Activity found to handle Intent { act=android.intent.action.VIEW dat=file:///sdcard/My%20Documents/filename.pdf }
		    	 */
		    	//startActivity(intentRegisterPDF);
			}
		}

	}
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add:
            /*
             * use OI File Manager to find a file, depends on OI File Manager being installed from the Market
             * 
             */
        	
        	Intent intent = new Intent("org.openintents.action.PICK_FILE");
        	Uri mDefaultPDFPath = Uri.parse("file://"+"/sdcard/My%20Documents/");
        	
            intent.setData(mDefaultPDFPath);
            startActivityForResult(intent,1);
            //page107
//        	String actionName = " ca.openlanguchunkaudiobook.action.EDIT_DOCUMENT_DETAILS ";
//            Intent intent = new Intent(actionName);
//            startActivity(intent);
        	return true;
        case R.id.menu_about:
        	Toast tellUser = Toast.makeText(this, 
            		"PDFtoAudioBook is a free open source app which allows users to listen to PDFs.\n\n For complex PDFs with formulas, figures, code blocks tables or similarly difficult text the application guesses a good way to read the information to the user.\n\n The application aims to be fully customizable but still user friendly.", Toast.LENGTH_LONG);
            tellUser.show();
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	public void loadTipsAndTricks(){
		String[] tipsTricks = {
				"Click the menu button to find a PDF to import.",
				"Click the audio books list to edit details or generate the audio book.",
				"You can generate the audiobook in chunks of audio such as section, page, chapter.",
				"Click the brown button to view the audiobook  chunked by section, page, chapter etc.",
				"Audiobooks can be scheduled for generation by date or depending on when your phone is charging and not in use.",
				"This program is free and open source!",
				"Using a music playlist you can listen to your audiobooks in the background when you're reading a pdf."
		};
		Random random = new Random();
		int tipsCount = tipsTricks.length;
		String selectedTip = tipsTricks[random.nextInt(tipsCount)];
		TextView tipbox = (TextView) findViewById(R.id.tipstricks);
		tipbox.setText("Tip: "+selectedTip);
	}
	
	public void onSearchClick(View v){
		loadTipsAndTricks();
    	startActivity(new Intent(this, DocumentsActivity.class));
    }   
    public void onStarredClick(View v){
    	loadTipsAndTricks();
    	startActivity(new Intent(this, ChunksActivity.class));
    }
    public void onAudioBookClick(View v){
    	loadTipsAndTricks();
    	startActivity(new Intent(this, DocumentsActivity.class));
    }
    public void onListenClick(View v){
    	loadTipsAndTricks();
    	startActivity(new Intent(this, ChunksActivity.class));
    }
    /** Handle "mind map" action. */
    public void onMindMapClick(View v) {
    	loadTipsAndTricks();
    	startActivity(new Intent(this, DocumentsActivity.class));
//    	mTts.speak("This would open the relevant mind map.",
//    	        TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
//    	        null);
    }
}
