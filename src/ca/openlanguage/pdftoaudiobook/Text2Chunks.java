package ca.openlanguage.pdftoaudiobook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;



public class Text2Chunks extends Activity{
	String mFileName="";
	String mOriginalFileNameAndPath="";
	
	File mOutputFilePath;
	FileInputStream mOriginalFile;
	
	LinkedHashMap<String,String> chunks = new LinkedHashMap();

	public Text2Chunks(String fileName, String fullPathAndFileName){
		mFileName = fileName;
		mOriginalFileNameAndPath=fullPathAndFileName;
	}

	public String chunkIt(String splitOn){
		String temp = "";
		String lineBreak ="\n";
		temp = temp+"This is the text for Chapter 1."+lineBreak;
		temp = temp+"This is the next line in the chapter."+lineBreak;
		chunks.put("Chapter 1", temp);
		
		
		
		
		
		
		
        return "Chunked on "+splitOn+" the result is: \n\n"+chunks.get("Chapter 1");
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
    			mOriginalFile = new FileInputStream(mOriginalFileNameAndPath);
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
	}
}
