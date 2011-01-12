package pl.multitalk.android;

import java.util.ArrayList;
import java.util.List;

import pl.multitalk.android.ui.ContactListAdapter;
import pl.multitalk.android.ui.ContactListItem;
import pl.multitalk.android.util.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Ekran listy kontaktów
 * @author Michał Kołodziejski
 */
public class ContactListActivity extends Activity {

    private List<ContactListItem> contactListItems;
    private ContactListAdapter contactListAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.contact_list_activity);
        
        /* lista kontaktów */
        contactListItems = new ArrayList<ContactListItem>();
        contactListAdapter = new ContactListAdapter(this,
                R.layout.contact_list_item, contactListItems);
        ListView contactListView = (ListView) findViewById(R.id.contactList_listView);
        contactListView.setAdapter(contactListAdapter);
        contactListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactListItem item = (ContactListItem) parent.getItemAtPosition(position);
                Log.d(Constants.DEBUG_TAG, "Kliknięto kontakt: "+item.getUsername());
                
                Intent intent = new Intent(Constants.ACTION_CONVERSATION_ACTIVITY);
                startActivity(intent);
            }
            
        });
        
        
        // FIXME
        for(int i=0; i<20; ++i){
            ContactListItem item = new ContactListItem();
            item.setUsername("Firstname Lastname "+i);
            contactListAdapter.add(item);
            contactListAdapter.notifyDataSetChanged();
        }
        
    }
}
