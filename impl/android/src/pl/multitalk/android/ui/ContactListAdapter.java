package pl.multitalk.android.ui;

import java.util.List;

import pl.multitalk.android.R;
import pl.multitalk.android.datatypes.UserInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Adapter listy kontaktów
 * @author Michał Kołodziejski
 */
public class ContactListAdapter extends ArrayAdapter<UserInfo> {

    private int itemResId;
    
    
    /**
     * Tworzy adapter listy kontaktów
     * @param context kontekst
     * @param itemResId identyfikator kontrolki elementu
     * @param objects lista kontaktów
     */
    public ContactListAdapter(Context context, int itemResId,
            List<UserInfo> objects) {
        
        super(context, itemResId, objects);
        this.itemResId = itemResId;
    }

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout contactListLayout;
        
        UserInfo item = getItem(position);
        
        if(convertView == null){
            contactListLayout = new LinearLayout(getContext());
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(itemResId, contactListLayout, true);
            
        } else {
            contactListLayout = (LinearLayout) convertView;
        }
        
        TextView username = (TextView) contactListLayout.findViewById(R.id.contactListItem_username);
        username.setText(item.getUsername());
        
        return contactListLayout;
    }
}
