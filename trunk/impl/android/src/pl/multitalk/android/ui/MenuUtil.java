package pl.multitalk.android.ui;

import pl.multitalk.android.R;
import pl.multitalk.android.util.Constants;
import android.content.Intent;
import android.view.Menu;

/**
 * Klasa użytkowa do zarządzania menu
 * @author Michał Kołodziejski
 */
public class MenuUtil {

    public static final int MENU_ITEM_CONTACT_LIST_ID = Menu.FIRST;
    public static final int MENU_ITEM_LOGOUT_ID = Menu.FIRST+1;

    /**
     * Tworzy menu
     * @param menu menu
     * @return utworzone menu
     */
    public static Menu createMenu(Menu menu){
        Intent contactListIntent = new Intent(Constants.ACTION_CONTACT_LIST_ACTIVITY);
        contactListIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        Intent logoutIntent = new Intent(Constants.ACTION_START_ACTIVITY);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        logoutIntent.putExtra("logout", true);
        
        menu.add(0, MENU_ITEM_CONTACT_LIST_ID, Menu.NONE, R.string.menu_contact_list_item)
            .setIntent(contactListIntent);
        menu.add(0, MENU_ITEM_LOGOUT_ID, Menu.NONE, R.string.menu_logout_item)
            .setIntent(logoutIntent);
        return menu;
    }
}
