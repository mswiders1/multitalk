package pl.multitalk.android;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.model.MultitalkApplication;
import pl.multitalk.android.ui.ContactListAdapter;
import pl.multitalk.android.ui.ContactListItem;
import pl.multitalk.android.util.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
    
    private MultitalkApplication app;
    private List<UserInfo> contactListItems;
    private ContactListAdapter contactListAdapter;
    private Handler handler = new Handler();
    private Timer contactListRefresherTimer;
    private final UserComparator userComparator = new UserComparator();
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        app = (MultitalkApplication) getApplication();
        
        setContentView(R.layout.contact_list_activity);
        
        /* lista kontaktów */
        contactListItems = new ArrayList<UserInfo>();
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
        
        
        // tymczasowe
//        for(int i=0; i<20; ++i){
//            ContactListItem item = new ContactListItem();
//            item.setUsername("Firstname Lastname "+i);
//            contactListAdapter.add(item);
//            contactListAdapter.notifyDataSetChanged();
//        }
        
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        contactListRefresherTimer = new Timer();
        contactListRefresherTimer.schedule(new ContactListRefresherTimerTask(), 
                3000, 3000);
    }
    
    
    @Override
    protected void onPause() {
        contactListRefresherTimer.cancel();
        contactListRefresherTimer = null;
        super.onPause();
    }
    
    
    /**
     * Odświeża listę kontaktów
     */
    private void refreshContactList(){
        List<UserInfo> users = app.getMultitalkNetworkManager().getUsers();
        contactListAdapter.clear();
        for(UserInfo user : users){
            contactListAdapter.add(user);
        }
        contactListAdapter.sort(userComparator);
        contactListAdapter.notifyDataSetChanged();
    }
    
    
    
    
    /**
     * Komparator użytkowników
     */
    class UserComparator implements Comparator<UserInfo> {

        @Override
        public int compare(UserInfo user1, UserInfo user2) {
            return user1.getUsername().compareTo(user2.getUsername());
        }
        
    }
    
    
    /**
     * Zadanie odświeżania listy kontaktów
     */
    class ContactListRefresherTimerTask extends TimerTask {

        @Override
        public void run() {
            ContactListActivity.this.handler.post(new Runnable() {
                @Override
                public void run() {
                    refreshContactList();
                }
            });
        }
        
    }
}
