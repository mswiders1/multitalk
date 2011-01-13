package pl.multitalk.android;

import java.util.ArrayList;
import java.util.List;

import pl.multitalk.android.ui.ConversationListAdapter;
import pl.multitalk.android.ui.ConversationListItem;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

/**
 * Ekran rozmowy między użytkownikami
 * @author Michał Kołodziejski
 */
public class ConversationActivity extends Activity {

    
    private List<ConversationListItem> conversationListItems;
    private ConversationListAdapter conversationAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.conversation_activity);
        
        // lista wiadomości
        conversationListItems = new ArrayList<ConversationListItem>();
        conversationAdapter = new ConversationListAdapter(this, R.layout.conversation_item,
                conversationListItems);
        ListView conversationListView = (ListView) findViewById(R.id.conversation_listView);
        conversationListView.setAdapter(conversationAdapter);
        
        
        // FIXME
        for(int i=0; i<20; ++i){
            ConversationListItem item = new ConversationListItem();
            item.setDate("Wysłano: 13-01-2011 13:20."+i);
            if(i % 2 == 0){
                item.setUsername("Firstname Lastname");
                item.setMessage("Wiadomość od pierwszego użytkownika");
            } else {
                item.setUsername("User 2");
                item.setMessage("Bardzo długa wiadomość od drugiego użytkownika, która nie dotyczy" +
                                " niczego istotnego, po prostu jest długa i tyle, " +
                                "jedynie o to chodzi w tej wiadomości, żeby zobaczyć" +
                                " jak się zachowa kontrolka");
            }
            conversationListItems.add(item);
            conversationAdapter.notifyDataSetChanged();
        }
    }
}
