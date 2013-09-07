package org.section9.pocketwatch;

import java.lang.reflect.Field;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;

public class MainActivity extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        // Force action bar overflow menu to display when hardware menu key is present.
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {}
        
        // Initialize preference defaults.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        

		findViewById(R.id.PanicButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onPanicButtonClicked(v);
			}
		});
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings:
				// Display preferences menu.
				FragmentManager fragmentManager = getFragmentManager();
	            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	            PreferencesFragment pf = new PreferencesFragment();
	            fragmentTransaction.replace(android.R.id.content, pf);
	            fragmentTransaction.hide(fragmentManager.findFragmentById(R.id.fragment_control_panel));
	            fragmentTransaction.addToBackStack(null);
	            fragmentTransaction.commit();
				return true;
				
			case R.id.menu_about:
				// Show about screen.
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void onPanicButtonClicked(View view) {
		// Toggle message transmission service state. 
		if (MessageTransmissionService.isRunning()) {
			stopService(new Intent(MainActivity.this, MessageTransmissionService.class));
			view.setSelected(false);
    	} else {
    		startService(new Intent(MainActivity.this, MessageTransmissionService.class));
    		view.setSelected(true);
    	}
	}
    
	@Override
	protected void onResume() {
    	super.onResume();
    	refreshPanicButtonState();
    }
    
    private void refreshPanicButtonState() {
    	findViewById(R.id.PanicButton).setSelected(MessageTransmissionService.isRunning());
    }
}
