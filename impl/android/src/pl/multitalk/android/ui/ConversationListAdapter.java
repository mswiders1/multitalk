package pl.multitalk.android.ui;

import java.util.List;

import pl.multitalk.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Adapter listy wiadomości
 * @author Michał Kołodziejski
 */
public class ConversationListAdapter extends ArrayAdapter<ConversationListItem> {

    private int itemResId;
    
    
    /**
     * Tworzy adapter listy wiadomości
     * @param context kontekst
     * @param itemResId identyfikator kontrolki elementu
     * @param objects lista wiadomości
     */
    public ConversationListAdapter(Context context, int itemResId,
            List<ConversationListItem> objects) {
        
        super(context, itemResId, objects);
        this.itemResId = itemResId;
    }

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout conversationListLayout;
        
        ConversationListItem item = getItem(position);
        
        if(convertView == null){
            conversationListLayout = new LinearLayout(getContext());
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(itemResId, conversationListLayout, true);
            
        } else {
            conversationListLayout = (LinearLayout) convertView;
        }
        
        // username
        TextView username = (TextView) conversationListLayout.findViewById(R.id.conversationItem_username);
        username.setText(item.getUsername());
        
        // wiadomość
        TextView message = (TextView) conversationListLayout.findViewById(R.id.conversationItem_message);
        message.setText(item.getMessage());
        
        // data
        TextView date = (TextView) conversationListLayout.findViewById(R.id.conversationItem_date);
        date.setText(item.getDate());
        
        return conversationListLayout;
    }
}
