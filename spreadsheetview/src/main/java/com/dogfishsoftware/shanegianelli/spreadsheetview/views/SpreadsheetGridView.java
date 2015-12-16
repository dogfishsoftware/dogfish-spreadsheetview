package com.dogfishsoftware.shanegianelli.spreadsheetview.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

/**
 * Created by shanegianelli on 9/3/15.
 */
public class SpreadsheetGridView extends GridView {
    public SpreadsheetGridView(Context context) {
        super(context);
    }

    public SpreadsheetGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpreadsheetGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        onTouchEvent(event);
        return false;
    }

    public int getScrollYFunctional() {
        int first = getFirstVisiblePosition();
        final View firstView = getChildAt(0);

        if (firstView == null) {
            return 0;
        }

        int nRow = (first / getNumColumns());
        int firstY = -firstView.getTop();

        return firstY + firstView.getHeight() * nRow;
    }

    public int getScrollYFunctional(int row) {
        final View firstView = getChildAt(0);

        return firstView == null ? 0 : firstView.getHeight() * row;
    }
}
