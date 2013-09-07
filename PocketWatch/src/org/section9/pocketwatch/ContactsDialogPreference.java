package org.section9.pocketwatch;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.preference.DialogPreference;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ContactsDialogPreference extends DialogPreference {
 	
	private ListView _list;
	
	public ContactsDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
		setDialogLayoutResource(R.layout.contacts_dialog_preference);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		
		// Set the adapter.
		_list = (ListView) view.findViewById(R.id.contacts_list);
		_list.setAdapter(getContactsAdapter());
		
		// Get the persisted string IDs, if any.
		Set<String> stringIDs = getSharedPreferences().getStringSet(getKey(), new HashSet<String>());
							
		// Convert to Long IDs.
		HashSet<Long> persistedIDs = new HashSet<Long>();
		for (String id : stringIDs)
			persistedIDs.add(Long.valueOf(id));
		
		// Update UI.
		for (int i=0; i < _list.getCount(); i++)
			if (persistedIDs.contains(_list.getItemIdAtPosition(i)))
				_list.setItemChecked(i, true);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			long[] long_ids = _list.getCheckedItemIds();
			HashSet<String> string_ids = new HashSet<String>();
			
			for (long id : long_ids)
				string_ids.add(String.valueOf(id));
			
			getEditor().putStringSet(getKey(), string_ids).commit();
		}
	}

	private CursorAdapter getContactsAdapter() {
		String fromCols[] = {
	 			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
	 			ContactsContract.CommonDataKinds.Phone.NUMBER
	 		};
	 	
	 	int toViews[] = {
		 		R.id.contact_name,
		 		R.id.contact_number
		 	};
	 	
	 	int flags = 0;
	 	
     	Cursor contacts = getContext().getContentResolver().query(
	     		ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	     		new String[] {
	     				ContactsContract.CommonDataKinds.Phone._ID,
	     				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
	     				ContactsContract.CommonDataKinds.Phone.NUMBER
	     			},
	     		ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + "=1",
	     		null,
	     		ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
	     	);
     	
     	return new SimpleCursorAdapter(
     			getContext(),
     			R.layout.contact_list_item,
     			contacts,
     			fromCols,
     			toViews,
     			flags
     		);
	}
}