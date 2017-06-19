package com.shellcore.android.happybirthayapp;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final int READ_CONTACTS_PERMISSION_REQUEST = 1;
    private static final String DEBUG = "MainActivity";
    private static final int CONTACT_LOADER_ID = 90;
    private static final int LOOKUP_KEY_INDEX = 1;
    private static final int CONTACT_ID_INDEX = 0;

    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setupCursorAdapter();
        ListView listView = (ListView) findViewById(R.id.rec_list);
        listView.setAdapter(adapter);

        // Adding the onClickEvent to each element of the list
        listView.setOnItemClickListener(this);

        getPermissionToReadUserContacts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about_me) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_CONTACTS_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadingContacts();
                } else {
                    Log.d(DEBUG, "Permission Denied");
                }
                break;
        }

    }

    /**
     * Click event for each element for the list of contacts
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = ((SimpleCursorAdapter) parent.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        String contactName = cursor.getString(LOOKUP_KEY_INDEX);

        Uri mContact = ContactsContract.Contacts.getLookupUri(
                cursor.getLong(CONTACT_ID_INDEX),
                contactName
        );
        String email = getEmail(mContact);

        if (!email.equals("")) {
            sendEmail(email, contactName);
        }
    }

    private void sendEmail(String email, String contactName) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto",
                email,
                null
        ));

        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.main_email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.main_email_body, contactName));
        startActivity(Intent.createChooser(intent, getString(R.string.main_email_choose)));
    }

    private String getEmail(Uri mContact) {
        String email = "";
        String id = mContact.getLastPathSegment();

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[] {id},
                null
        );

        int emailId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

        if (cursor.moveToFirst()) {
            email = cursor.getString(emailId);
        }

        return email;
    }

    /**
     * This function is to ask to the user if he granted the permission to read the contacts from his gadget.
     * From version < MArshmallow, the permission won't be asked
     */
    private void getPermissionToReadUserContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[] {Manifest.permission.READ_CONTACTS},
                        READ_CONTACTS_PERMISSION_REQUEST);
            } else {
                loadingContacts();
            }
        } else {
            loadingContacts();
        }
    }

    /**
     * Function to initialize the elements of the adapter
     */
    private void setupCursorAdapter() {
        String[] uiBindFrom = {
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
        };

        int[] uiBindTo = {
                R.id.txt_contact_name,
                R.id.img_contact_photo
        };

        adapter = new SimpleCursorAdapter(this, R.layout.item_contact, null, uiBindFrom, uiBindTo, 0);
    }

    private void loadingContacts() {
        Log.d(DEBUG, "We have permission to load the contacts");
        getLoaderManager().initLoader(CONTACT_LOADER_ID, new Bundle(), contactsLoader);
    }

    private LoaderManager.LoaderCallbacks<Cursor> contactsLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            String[] projectionFields = new String[] {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_URI

            };

            CursorLoader cursorLoader = new CursorLoader(
                    MainActivity.this,
                    ContactsContract.Contacts.CONTENT_URI,
                    projectionFields,
                    null,
                    null,
                    null
            );

            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            adapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.swapCursor(null);
        }
    };

    public void loadAboutMe(MenuItem item) {
        Intent intent = new Intent(this, AboutMeActivity.class);
        startActivity(intent);
    }
}
