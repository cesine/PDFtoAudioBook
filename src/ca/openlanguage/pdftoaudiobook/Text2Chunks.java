package ca.openlanguage.pdftoaudiobook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;


//later extend the AsyncTask to run longer non-UI processes
public class Text2Chunks extends Activity{
	String mFileName="";
	String mOriginalFileNameAndPath="";
	String mSplitOn;
	String mResults;
	Context mParentsContext;
	
	File mOutputFilePath;
	BufferedReader mOriginalFile;
	
	LinkedHashMap<String,String> chunks = new LinkedHashMap();

	
	final Handler mHandler= new Handler();
	final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateResultsInUi();
        }
    };
	
    
    
	public Text2Chunks(Context context, String fileName, String fullPathAndFileName){
		mParentsContext =context;
		mFileName = fileName;
		//will open the text file if it is present. 
		mOriginalFileNameAndPath=fullPathAndFileName.replace(".pdf", ".txt");
	}

	private void updateResultsInUi() {
        // Back in the UI thread -- update our UI elements based on the data in mResults
		//ProgressBar progressBar;
		//int progressStatus = 0;
    }
	public void chunkIt(){
		Thread t = new Thread() {
            public void run() {
                mResults = chunkItCompletely(mSplitOn);
                mHandler.post(mUpdateResults);
            }
        };
        t.start();
	}
	public void setSplitOn(String splitOn){
		mSplitOn=splitOn;
	}
	public String getChunkResults(){
		return mResults;
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// txt.setText("Processing Done");

		}
	};

	
	public String chunkItCompletely(String splitOn){
		if (true){
			return "chunking turned off to save time";
		}
		 /*
		 * Here is a brief summary of the recommended approach for handling expensive operations:

				Create a Handler object in your UI thread
				Spawn off worker threads to perform any required expensive operations
				Post results from a worker thread back to the UI thread's handler either through a Runnable or a Message
				Update the views on the UI thread as needed
		 */
		
		/*
		 * ProgressDialog progressBar;
		progressBar = ProgressDialog.show(mParentsContext,
				"Dividing the file into sections", "please wait....", true);
		 */
		
		/*
		new Thread() {
			public void run() {
				try {
					// just doing some long operation
					sleep(5000);
				} catch (Exception e) {
				}
				handler.sendEmptyMessage(0);
			}
		}.start();
		
		
		progressBar.dismiss();
		*/
		
		String chunkString = "";
		
		String lineBreak ="\n";
		String chunkName = "00preface";
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
				
				if (line.trim().matches("\\d+\\.\\d+") ) {
					chunks.put(chunkName, chunkString);
					//Toast tellUser = Toast.makeText(mParentsContext, 
		            //		"The Chunk Name: "+chunkName+":\n\n"+chunkString, Toast.LENGTH_LONG);
		            //tellUser.show();
		            
					chunkString = "";
					chunkName = line.trim().replaceAll(" ", "_");
					message = message + chunkName + lineBreak;
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
		/*write the chunks out to file for examination
		try {
			FileWriter out = new FileWriter(mOutputFilePath+"/test.txt");
			out.write(chunks.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
	  	// Tell the media scanner about the new file so that it is
        // immediately available to the user.
    	/*
		MediaScannerConnection.scanFile(this,
                new String[] { mOutputFilePath+"/text.txt"}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.i("ExternalStorage", "Scanned " + path + ":");
                Log.i("ExternalStorage", "-> uri=" + uri);
            }
        });
        */
			
		//progressBar.dismiss();
		
        return "Chunked on "+splitOn+" the result is: \n\n"+chunks.size()+" chunks.\n  "+message;
	}
	
	public String askUserForSplitRegEx(){
		return "/Chapter [digits]/";
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

        	Toast tellUserSDCARDproblem = Toast.makeText(mParentsContext, 
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
            mOutputFilePath.mkdirs();     
            message+="\nFile path for output is okay (it's in the Music directory). \n";
        }
       
		return message;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		

        //setContentView(R.layout.blank);
	}
}
