package ca.openlanguage.pdftoaudiobook.ui;

import ca.openlanguage.pdftoaudiobook.provider.AudioBookLibraryDatabase;
import ca.openlanguage.pdftoaudiobook.R;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.LiveFolders;

public class AudioBooksLiveFolder extends Activity {

    /**
     * The URI for the Notes Live Folder content provider.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + AudioBookLibraryDatabase.AUTHORITY + "/live_folders/notes");

    public static final Uri NOTE_URI = Uri.parse("content://"
            + AudioBookLibraryDatabase.AUTHORITY + "/notes/#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
            // Build the live folder intent.
            final Intent liveFolderIntent = new Intent();

            liveFolderIntent.setData(CONTENT_URI);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME,
                    getString(R.string.live_folder_name));
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON,
                    Intent.ShortcutIconResource.fromContext(this,
                            R.drawable.live_folder_notes));
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                    LiveFolders.DISPLAY_MODE_LIST);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT,
                    new Intent(Intent.ACTION_EDIT, NOTE_URI));

            // The result of this activity should be a live folder intent.
            setResult(RESULT_OK, liveFolderIntent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }
}
