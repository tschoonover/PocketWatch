package org.section9.pocketwatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

public class IrregularShapeButton extends Button {

	public IrregularShapeButton(Context context) {
		super(context);
	}

	public IrregularShapeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IrregularShapeButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int iX = (int) event.getX();
        int iY = (int) event.getY();
        
        // If touch location is transparent, do not handle.
        if (getBackgroundBitmap().getPixel(iX, iY) == 0)
        	return false;
        
		return super.dispatchTouchEvent(event);
	}
	
	private Bitmap getBackgroundBitmap () {
		Drawable d = this.getBackground().getCurrent();
		
	    if (d instanceof BitmapDrawable) {
	        return ((BitmapDrawable)d).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    d.draw(canvas);

	    return bitmap;
	}
}
