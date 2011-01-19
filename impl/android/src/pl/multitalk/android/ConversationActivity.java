package pl.multitalk.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.messages.MsgMessage;
import pl.multitalk.android.managers.messages.internal.SendMessageToClient;
import pl.multitalk.android.model.MultitalkApplication;
import pl.multitalk.android.ui.ConversationListAdapter;
import pl.multitalk.android.ui.ConversationListItem;
import pl.multitalk.android.ui.MenuUtil;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Ekran rozmowy między użytkownikami
 * @author Michał Kołodziejski
 */
public class ConversationActivity extends Activity {

    private MultitalkApplication app;
    private UserInfo clientUser;
    private List<ConversationListItem> conversationListItems;
    private ConversationListAdapter conversationAdapter;
    private EditText messageInput;
    private Handler handler = new Handler();
    private Timer updateConversationTimer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        app = (MultitalkApplication) getApplication();
        clientUser = new UserInfo();
        clientUser.setUid(getIntent().getStringExtra("UID"));
        
        setContentView(R.layout.conversation_activity);
        
        // lista wiadomości
        conversationListItems = new ArrayList<ConversationListItem>();
        conversationAdapter = new ConversationListAdapter(this, R.layout.conversation_item,
                conversationListItems);
        ListView conversationListView = (ListView) findViewById(R.id.conversation_listView);
        conversationListView.setAdapter(conversationAdapter);
        
        messageInput = (EditText) findViewById(R.id.conversation_bottomBar_messageInput);
        
        Button sendButton = (Button) findViewById(R.id.conversation_bottomBar_sendButton);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageInput.getText().toString();
                if("".equals(messageText)){
                    return;
                }
                
                SendMessageToClient sendMessageMsg = new SendMessageToClient();
                sendMessageMsg.setContent(messageText);
                sendMessageMsg.setRecipientInfo(clientUser);
                app.getMultitalkNetworkManager().putMessage(sendMessageMsg);
                
                messageInput.setText("");
            }
        });
        
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        updateConversationTimer = new Timer();
        updateConversationTimer.schedule(new UpdateConversationTimerTask(), 0, 2000);
    }
    
    
    @Override
    protected void onPause() {
        super.onPause();
        updateConversationTimer.cancel();
        updateConversationTimer = null;
    }
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuUtil.createMenu(menu);
        return true;
    }
    
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        startActivity(item.getIntent());
        return true;
    }
    
    
    
    /**
     * Zadanie odświeżania rozmowy
     */
    class UpdateConversationTimerTask extends TimerTask{

        private List<MsgMessage> conversation;
        
        @Override
        public void run() {
            conversation = app.getMultitalkNetworkManager()
                    .getConversation(clientUser);
            
            handler.post(new Runnable() {
                @Override
                public void run() {
                    conversationListItems.clear();
                    for(MsgMessage msg : conversation){
                      ConversationListItem item = new ConversationListItem();
                      item.setUsername(msg.getMsgSender().getUsername());
                      item.setMessage(msg.getContent());
                      conversationListItems.add(item);
                    }
                  conversationAdapter.notifyDataSetChanged();
                }
            });
            
        }
        
    }
}
