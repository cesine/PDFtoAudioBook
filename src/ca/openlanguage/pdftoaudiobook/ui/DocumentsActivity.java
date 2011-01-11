package ca.openlanguage.pdftoaudiobook.ui;





import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryDatabase.DocumentColumns;
import ca.openlanguage.pdftoaudiobook.R;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class DocumentsActivity extends ListActivity {
	 private static final String TAG ="Document Activity";
    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
        DocumentColumns._ID, // 0
        DocumentColumns.TITLE, // 1
    };
    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_documents);
		
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		
		// If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
        	
        	//example  Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] dat=content://ca.openlanguage.pdftoaudiobook.AudiobookLibraryProvider/documents flg=0x10000000 cmp=ca.openlanguage.pdftoaudiobook/.ui.DocumentsActivity }
            intent.setData(DocumentColumns.CONTENT_URI);
        }
     // Inform the list we provide context menus for items
        //getListView().setOnCreateContextMenuListener(this);
        
        //getting an a null reference. try to make the db first?
        //ContentValues values = new ContentValues();
        //values.put("title","testing from documents activity.");
        //sending null values, the provider will check it anyway.
       //notworkign Uri uri = getContentResolver().insert(DocumentColumns.CONTENT_URI, values);
        //String temp = uri.toString();
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null, DocumentColumns.DEFAULT_SORT_ORDER);
        //Cursor cursor = managedQuery(DocumentColumns.CONTENT_URI,null,null,null,null);
        //Cursor cursor = getContentResolver().query(DocumentColumns.CONTENT_URI, null, null, null, null);
        
        String cursorworked = cursor.toString();
        cursorworked ="this is to debug "+cursorworked;
        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.noteslist_item, cursor,
                new String[] { DocumentColumns.TITLE }, new int[] { android.R.id.text1 });
        setListAdapter(adapter);
	}

}
