package ca.openlanguage.pdftoaudiobook.provider;



import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryContract.Documents;

public class AudioBookLibraryDatabase extends SQLiteOpenHelper {
	private static final String TAG = "AudioBookLibraryDatabase";
	private static final String DATABASE_NAME = "audiobooklibrary.db";
	
	private SQLiteDatabase mDatabase;
	private Context mContext;
	
    // NOTE: carefully update onUpgrade() when bumping database versions to make
    // sure user data is saved.

    private static final int VER_LAUNCH = 1;
    private static final int VER_SESSION_HASHTAG = 2;
    private static final int DATABASE_VERSION = VER_SESSION_HASHTAG;
    
    interface Tables {
    	String DOCUMENTS = "documents";
    	String DOCUMENTS_SEARCH ="documents_search";
    	//TODO list base tables as well as the joined tables used for query reasons
    	
    	
    	String SEARCH_SUGGEST = "sessions_search";
    	
    }
    /**
     * Names of triggers on the database
     * At the moment it contains triggers which are used to register new rows into their respective search tables/indexes. 
     * IE, adda  new document a new entry will be created in the 
     * search table which contains the documents id, as well as a 
     * body containing {title, author, classification}, 
     * as specified by the subquery interface below
     * 
     * @author gina
     *
     */
    private interface Triggers {
    	String DOCUMENTS_SEARCH_INSERT ="documents_search_insert";
    	String DOCUMENTS_SEARCH_DELETE = "documents_search_delete";
    }
    //TODO check if this is the right var choices, might want to change it later, 
    //note the Qualified interface depends on this 
    interface DocumentsSearchColumns{
    	String DOCUMENT_ID = "documentId";
    	String BODY = "body";
    }
    /**
     * TODO what does the qualified mean here
     * Fully-qualified field names
     */
    private interface Qualified{
    	//construction is used when there is both an _ID and a MODULENAME_ID... might not be needed for documents as it just uses _ID
    	String DOCUMENTS_SEARCH_DOCUMENT_ID = Tables.DOCUMENTS_SEARCH + "."
    		+ DocumentsSearchColumns.DOCUMENT_ID;
    	
    	String DOCUMENTS_SEARCH = Tables.DOCUMENTS_SEARCH 
    	+ "(" + DocumentsSearchColumns.DOCUMENT_ID
    	+ "," + DocumentsSearchColumns.BODY 
    	+ ")";
    }
    /** {@code REFERENCES} clauses. 
     *  Notice, classes which use the base columns will use the _ID, 
     *  whereas classes which declare their own primary key will use the full key eg, PERSON_ID
     */
    private interface References{
    	String DOCUMENT_ID = "REFERENCES " 
    		+ Tables.DOCUMENTS 
    		+ "(" + Documents._ID 
    		+ "(";
    }
    private interface BuildSearchBodys {
    	/** 
    	 * Subquery used to build the {@link DocumentsSearchColumns#BODY} string
    	 * used for indexing {@link Documents} content.
    	 */
    	String DOCUMENTS_BODY = "(new."
    		+ Documents.DOCUMENT_TITLE 
    		+ "||'; '||new."
    		+ Documents.DOCUMENT_AUTHOR 
    		+ "||'; '||new." 
    		+ Documents.DOCUMENT_CLASSIFICATION + ")";
    }
    private static void createDocumentsSearch(SQLiteDatabase db){
    	//using the porter stemmer 
    	//so that queries like "frustration" also matches "frustrated"
    	//TODO there might be a problem here with the ids, the examples in the sessions and vendor's modules show two ids, one that is the integer one that is the string for the uri.
    	//the documents contract sets it up with just the integer util a reasonable string can be thought of. 
    	String createStatementDocumentsSearch =("CREATE VIRTUAL TABLE " + Tables.DOCUMENTS_SEARCH 
    			+ " USING fts3("
    			+ Documents._ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
    			+ DocumentsSearchColumns.BODY + " TEXT NOT NULL,"
    			+ "tokenize=porter)"
     			);
    	db.execSQL(createStatementDocumentsSearch);
    	/*
    	 * when a new document is added, create an entry in the search table for Documents_Search 
    	 * which contains some info about the document
    	 */
    	String createStatementTriggerNewDocAddToSearch =("CREATE TRIGGER " + Triggers.DOCUMENTS_SEARCH_INSERT 
    			+ " AFTER INSERT ON " + Tables.DOCUMENTS 
    			+ " BEGIN INSERT INTO " + Qualified.DOCUMENTS_SEARCH + " "
    			+ " VALUES(new." +Documents._ID + ", " + BuildSearchBodys.DOCUMENTS_BODY + ");"
    			+ " END;"
    			);
    	db.execSQL(createStatementTriggerNewDocAddToSearch);
    	//TODO implement the update trigger, otherwise if user enters document first tiem 
    	//with lots of missing details the search wont pick up the document (eg, classification etc)
    	
    	/* 
    	 * delete rows from the documents search table which have "=old" in their documents id string? (based on the vendors module from best practices of ioschedule)
    	 */
    	String createTriggerDeleteDocFromSearch=("CREATE TRIGGER "+ Triggers.DOCUMENTS_SEARCH_DELETE + " AFTER DELETE ON "
    			+ Tables.DOCUMENTS + " BEGIN DELETE FROM " + Tables.DOCUMENTS_SEARCH + " "
    			+ " WHERE " + Qualified.DOCUMENTS_SEARCH_DOCUMENT_ID + "=old." + Documents._ID
    			+ " END;"
    			);
    	db.execSQL(createTriggerDeleteDocFromSearch);
    }
    
    /**
     * Create table statements for all domain tables, as well as join tables, and the
     * search suggest table, and also calls a function to create a DocumentsSearch table and triggers. 
     */
	@Override
	public void onCreate(SQLiteDatabase db) {
		mDatabase = db;
		
		String workingCreateStatement ="CREATE TABLE documents (documentsid INTEGER PRIMARY KEY ,title TEXT NOT NULL,author TEXT)";
		//db.execSQL(
		String createStatement =("CREATE TABLE " + Tables.DOCUMENTS + " (" 
				+ Documents._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Documents.DOCUMENT_TITLE + " TEXT NOT NULL,"
				+ Documents.DOCUMENT_AUTHOR + " TEXT,"
				//TODO ADD THE REST HERE
				+"UNIQUE (" + Documents._ID + ") ON CONFLICT REPLACE)"
				);
		workingCreateStatement=createStatement;
		mDatabase.execSQL(workingCreateStatement);
		

		/*
		 * create additonal table and triggers to keep it up to date, table consists of 
		 * Document id ( an integer to look up the document in the database)
		 * body (a text field which has been porter stemmed and includes info such as title, author and classification of the document. 
		 * For more info see the method implemetnation and the 
		 * DocumentSearchColumns defined in subquery to add more 
		 * information in the indexed information, or to change the 
		 * columns taht are indexed (Eg add an abstrct), or add a 
		 * user made summary, or an autosummary 
		 */
		//createDocumentsSearch(db);
		
//		db.execSQL("CREATE TABLE "+ Tables.SEARCH_SUGGEST + " ("
//				+BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//				+SearchManager.SUGGEST_COLUMN_TEXT_1 + " TEXT NOT NULL)"
//				);
	}
	public String testInsertRow(){
		mDatabase = getWritableDatabase();
		mDatabase.execSQL("DROP TABLE IF EXISTS testdocuments");
		String workingCreateStatement ="CREATE TABLE testdocuments (documentsid INTEGER PRIMARY KEY ,title TEXT NOT NULL,author TEXT)";
		mDatabase.execSQL(workingCreateStatement);
		
		ContentValues initialValues = new ContentValues();
        initialValues.put("title","Test title");
        initialValues.put("author","test author");

        Long insertId= mDatabase.insert("testdocuments", null, initialValues);
		return "asked for a writable database, which will ask the sqliteopenhelper to call oncreate, droped the test table,created it and insert this is teh result: "+ insertId.toString();
	}

	/**
	 * Destroys all data and calls onCreate again, reinitalizing teh database to its original state
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade() from " + oldVersion + " to "+ newVersion);

		
		/*
		 * some left over logic from an upgrade which required adding a column to a table. 
		 * could use it to do the same, or maybe not...
		 */
		int version = oldVersion;
		switch (version){
		case VER_LAUNCH:
			db.execSQL("ALTER TABLE " + Tables.DOCUMENTS + " ADD COLUMN "
				+ "somenewcolumnname" + " TEXT");
			version = VER_SESSION_HASHTAG;
		}
		
		Log.d(TAG, "after upgrade logic, at version "+ version);
		if (version != DATABASE_VERSION){
			Log.w(TAG, "Destroying old data during upgrade");
			
			db.execSQL("DROP TABLE IF EXISTS "+ Tables.DOCUMENTS);
			db.execSQL("DROP TABLE IF EXISTS "+ Tables.DOCUMENTS_SEARCH);
			
			db.execSQL("DROP TABLE IF EXISTS "+ Tables.SEARCH_SUGGEST);
			
			onCreate(db);
		}
	}
	public AudioBookLibraryDatabase(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
		
		mContext = context;
		
	}

	
}
