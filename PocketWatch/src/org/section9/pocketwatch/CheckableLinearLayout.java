package org.section9.pocketwatch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {

	private Checkable checkable;

    public CheckableLinearLayout (final Context context, final AttributeSet attrs) {
    	super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
    	super.onFinishInflate();
    	final int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);

            if (view instanceof Checkable) {
                checkable = (Checkable)view;
                break;
            }
        }
    }

    @Override
    public boolean isChecked() {
        return (checkable != null) ? checkable.isChecked() : false;
    }

    @Override
    public void setChecked (final boolean checked) {
        if (checkable != null) {
	        checkable.setChecked(checked);
	    }
    }

    @Override
    public void toggle() {
        if (checkable != null) {
            checkable.toggle();
        }
    }
}
