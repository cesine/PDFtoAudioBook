/**
 * 
 */
package ca.openlanguage.provider;

import android.net.Uri;

/**
 * @author gina
 *
 */
public class DocumentProviderMetadata {

	public static final String AUTHORITY = "ca.openlanguage.provider.Document";
	

	
	public static final String DATABASE_NAME = "document.db";
	public static final int DATABASE_VERSION = 1;
	public static final String DOCUMENTS_TABLE_NAME = "documents";
	
	//null constructor
	private DocumentProviderMetadata(){}

	
	//inner class describing documentTable
	public static final class DocumentTableMetadata{
		private DocumentTableMetadata(){}
		public static final String TABLE_NAME = "documents";
		
		//uri and MIME type definitions
		public static final Uri CONTENT_URI =
			Uri.parse("content://"+AUTHORITY+ "/documents");
		public static final String CONTENT_TYPE =
			"vnd.android.cursor.dir/vnd.openlanguage.document";
		public static final String CONTENT_ITEM_TYPE =
			"vnd.android.cursor.item/vnd.openlanguage.document";
		public static final String DEFAULT_SORT_ORDER = "modified DESC";
		
		/*
		 * Column names
		 * 	To add a new colum, add it here and in the Document Provider
		 */
		//Additional columns here, expected datatypes are in comments
		//string type "Intensional Semantics"
		public static final String DOCUMENT_TITLE = "title";
		
		//string type "Kai von Fintel, Irene Heim"
		public static final String DOCUMENT_AUTHOR = "author";
		
		//string type, convert to list of strings later, "(von Fintel 2004), (von Fintel and Heim)"
		public static final String DOCUMENT_CITATION = "citations";
		
		//integer type from System.currentTimeMillis()
		public static final String DOCUMENT_ADDED_DATE = "addeddate";

		//added because it wasn't present, creating errors in the DocumentProvider, might remove it later if its auto included
		public static final String _ID = "documentid";
		
		
		
	}
		
}
