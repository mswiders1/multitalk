package pl.multitalk.android;

import android.app.Activity;
import android.os.Bundle;

/**
 * Ekran startowy aplikacji
 * @author Michał Kołodziejski
 */
public class StartActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
    }
}