package com.dogfishsoftware.shanegianelli.spreadsheetview.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.dogfishsoftware.shanegianelli.spreadsheetview.R;
import com.dogfishsoftware.shanegianelli.spreadsheetview.adapters.SpreadsheetAdapter;
import com.dogfishsoftware.shanegianelli.spreadsheetview.adapters.SpreadsheetAdapter.Coordinates;
import com.dogfishsoftware.shanegianelli.spreadsheetview.adapters.SpreadsheetHeaderAdapter;

public class SpreadsheetView extends LinearLayout {

    /**
     * This gets called when a user taps a cell or header view
     */
    public static abstract class OnSelectListener {
        public abstract boolean onSelect(Coordinates coordinates);
        public abstract boolean onRowSelect(int position);
        public abstract boolean onColumnSelect(int position);
    }

    /**
     * This gets called when the user performs a long press gesture on a cell
     */
    public static abstract class OnLongSelectListener {
        public abstract void onLongSelect(Coordinates coordinates, View view);
    }

    /**
     * Callback handler for click interactions with cells and headers
     */
    private OnSelectListener onSelectListener;

    /**
     * Applies this listener to both body and headers
     */
    private OnLongSelectListener onLongSelectListener;

    /**
     * The width for the main content cells (in dp)
     */
    private int columnWidth;

    /**
     * The height for the main content cells (in dp)
     */
    private int rowHeight;

    /**
     * The width for the row header cells (in dp)
     */
    private int rowHeaderWidth;

    /**
     * Main content adapter for the spreadsheet
     */
    private SpreadsheetAdapter adapter;

    // Internal properties

    private SpreadsheetHeaderAdapter mColumnHeaderAdapter;
    private SpreadsheetHeaderAdapter mRowHeaderAdapter;

    private LinearLayout mSpreadsheetHorizontalLayout;
    private LinearLayout mSpreadsheetColumnHeaderLayout;

    private HorizontalScrollView mMainHorizontalScrollView;

    private Coordinates mTempSelectedPosition = null;
    private Coordinates mSelectedPosition = null;

    private SpreadsheetGridView mGridView;
    private GridView mRowHeaderGridView;
    private GridView mColumnHeaderGridView;

    private View mVerticalSpacerTop;
    private View mVerticalSpacerBottom;

    private LinearLayout mSpreadsheetContainer;

    private float mStartX;
    private float mStartY;
    private boolean mScrolling = false;

    public SpreadsheetView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public SpreadsheetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.spreadsheet_view, this);

        /*
          Grab references to all the various layout components needed to make this work.
         */
        mGridView = (SpreadsheetGridView) findViewById(R.id.spreadsheet_grid_view);

        mSpreadsheetHorizontalLayout = (LinearLayout) findViewById(R.id.linearLayout_gridtableLayout);
        mSpreadsheetColumnHeaderLayout = (LinearLayout) findViewById(R.id.linearLayout_gridtableColumnHeaderLayout);

        mColumnHeaderGridView = (GridView) findViewById(R.id.spreadsheet_column_header_view);

        mRowHeaderGridView = (GridView) findViewById(R.id.spreadsheet_row_header_view);

        mMainHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.spreadsheet_mainHorizontalScrollView);

        HorizontalScrollView mColumnHeaderScrollView = (HorizontalScrollView) findViewById(R.id.spreadsheet_column_header_scroll_view);

        mVerticalSpacerTop = findViewById(R.id.spreadsheet_view_vertical_spacer_top);
        mVerticalSpacerBottom = findViewById(R.id.spreadsheet_view_vertical_spacer_bottom);

        mSpreadsheetContainer = (LinearLayout) findViewById(R.id.spreadsheet_container);

        mGridView.setBackground(this.getBackground());
        mRowHeaderGridView.setBackground(this.getBackground());
        mColumnHeaderGridView.setBackground(this.getBackground());
        mVerticalSpacerTop.setBackground(this.getBackground());
        mVerticalSpacerBottom.setBackground(this.getBackground());
        mSpreadsheetContainer.setBackground(this.getBackground());

        /**
         * Setup synchronized scrolling between header grids and the main body grid. This was a
         * complete nightmare to get working right because of: (1) scrollTo not working properly
         * in certain instance (documented bug in android), (2) touch events not being able to be
         * passed to HorizontalScrollViews in the same way as they can to other grid views.
         */

        // Horizontal scroll handler (scrollTo works in this instance)
        mMainHorizontalScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                mColumnHeaderGridView.scrollTo(mMainHorizontalScrollView.getScrollX(), 0);
            }
        });

        mGridView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                mRowHeaderGridView.scrollTo(0, mGridView.getScrollYFunctional());
            }
        });

        mRowHeaderGridView.setEnabled(false);
        mColumnHeaderScrollView.setEnabled(true);

        /**
         * This whole construct deals with the issue of cells becoming selected during scrolling
         * and observes the first moments of a touch event to see if it starts moving after touch-
         * down has happened. If the movement exceeds the threshold defined below, then the view is
         * scrolled, otherwise the `onSelect` event is passed to the OnSelectListener.
         */
        mGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mScrolling = false;
                        mStartX = event.getX();
                        mStartY = event.getY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (!mScrolling) {
                            double distance = Math.sqrt(Math.pow(mStartX - event.getX(), 2) + Math.pow(mStartY - event.getY(), 2));

                            mScrolling = distance > 2;
                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (!mScrolling && mTempSelectedPosition != null) {
                            double distance = Math.sqrt(Math.pow(mStartX - event.getX(), 2) + Math.pow(mStartY - event.getY(), 2));

                            if (distance > 1) {
                                break;
                            }

                            mSelectedPosition = mTempSelectedPosition;

                            if (onSelectListener != null) {
                                onSelectListener.onSelect(mSelectedPosition);
                            }

                            mColumnHeaderAdapter.notifyDataSetChanged();
                            mRowHeaderAdapter.notifyDataSetChanged();
                        }

                        break;

                    default:
                        break;
                }

                return false;
            }
        });
    }

    /**
     * @return The current SpreadsheetAdapter instance
     */
    public SpreadsheetAdapter getAdapter() {
        return adapter;
    }

    /**
     * Assigns a new adapter to the SpreadsheetView and initializes all of the child GridViews that
     * make up the column header, row header, and body.
     *
     * @param adapter The new adapter
     */
    public void setAdapter(SpreadsheetAdapter adapter) {
        this.adapter = adapter;

        mGridView.setAdapter(adapter);

        mColumnHeaderAdapter = new SpreadsheetHeaderAdapter(this.adapter, true);
        mRowHeaderAdapter = new SpreadsheetHeaderAdapter(this.adapter, false);

        mColumnHeaderGridView.setAdapter(mColumnHeaderAdapter);
        mRowHeaderGridView.setAdapter(mRowHeaderAdapter);

        /*
          Now the useful part... setting up selection listeners on the main grid view.
         */
        this.adapter.setOnSelectListener(new OnSelectListener() {
            @Override
            public boolean onSelect(Coordinates coordinates) {
                mTempSelectedPosition = coordinates;

                return true;
            }

            @Override
            public boolean onColumnSelect(int position) {
                return onSelectListener != null && onSelectListener.onColumnSelect(position);
            }

            @Override
            public boolean onRowSelect(int position) {
                return onSelectListener != null && onSelectListener.onRowSelect(position);
            }
        });

        AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (getOnLongSelectListener() != null) {
                    getOnLongSelectListener().onLongSelect(getAdapter().getCoordinates(position), view);
                }

                return false;
            }
        };

        mGridView.setOnItemLongClickListener(onItemLongClickListener);

        this.reload();
    }

    /**
     * Adjusts the current viewing frame so that the specified coordinates will be fully visible
     *
     * @param coordinates Position of view to display
     */
    public void moveTo(Coordinates coordinates) {
        float density = getResources().getDisplayMetrics().density;

        int visibleWidth = (int) ((mSpreadsheetContainer.getWidth() - mRowHeaderGridView.getWidth()) / density);
        int offsetX = mMainHorizontalScrollView.getScrollX();

        int visibleHeight = (int) (mGridView.getHeight() / density);
        int offsetY = mGridView.getScrollY();

        int selectedX = this.getAdapter().getSelectedCoordinates().column * this.getColumnWidth();
        int selectedY = this.getAdapter().getSelectedCoordinates().row * this.getRowHeight();

        if (selectedX + getColumnWidth() > offsetX + visibleWidth || selectedX < offsetX) {
            int scrollX = (int) ((offsetX + (selectedX + getColumnWidth()) - (offsetX + visibleWidth) + 10) * density);
            scrollX = Math.max(Math.min(scrollX, mMainHorizontalScrollView.getWidth() - getColumnWidth()), 0);

            mMainHorizontalScrollView.scrollTo(scrollX, 0);
        }

        if (selectedY + getRowHeight() > offsetY + visibleHeight || selectedY < offsetY) {
            int scrollY = (int) ((offsetY + (selectedY + getRowHeight()) - (offsetY + visibleHeight) + 10) * density);
            scrollY = Math.max(Math.min(scrollY, mGridView.getScrollYFunctional(coordinates.row) - getRowHeight()), 0);

            mGridView.smoothScrollBy(scrollY - mGridView.getScrollYFunctional(), 0);
        }
    }

    /**
     * Adjusts the current viewing frame so the currently selected position will be fully visible
     */
    public void moveToSelectedCoordinates() {
        moveTo(this.adapter.getSelectedCoordinates());
    }

    /**
     * Notifies all of the child GridViews to reload their data and then reloads their layout to
     * accommodate for any changes in number of rows and columns.
     */
    public void reload() {
        if (this.adapter == null) {
            return;
        }

        this.adapter.notifyDataSetChanged();

        mColumnHeaderAdapter.notifyDataSetChanged();
        mRowHeaderAdapter.notifyDataSetChanged();

        mGridView.setNumColumns(this.adapter.getNumColumns());
        mColumnHeaderGridView.setNumColumns(this.adapter.getNumColumns());

        final int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getRowHeight(), getResources().getDisplayMetrics());
        int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getColumnWidth(), getResources().getDisplayMetrics());

        mSpreadsheetHorizontalLayout.getLayoutParams().width = widthPx * this.adapter.getNumColumns();
        mSpreadsheetColumnHeaderLayout.getLayoutParams().width = widthPx * this.adapter.getNumColumns();

        mRowHeaderGridView.getLayoutParams().height = heightPx * (this.adapter.getNumRows() + 1);
    }

    /**
     * @return Returns `onSelectListener`
     */
    @SuppressWarnings("unused")
    public OnSelectListener getOnSelectListener() {
        return onSelectListener;
    }

    /**
     * @param onSelectListener Sets new instance of `onSelectListener`
     */
    public void setOnSelectListener(OnSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
    }

    public OnLongSelectListener getOnLongSelectListener() {
        return onLongSelectListener;
    }

    public void setOnLongSelectListener(OnLongSelectListener onLongSelectListener) {
        this.onLongSelectListener = onLongSelectListener;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
        this.reload();
    }

    private int getRowHeight() {
        if (this.rowHeight == 0 && mGridView  .getChildCount() > 0) {
            return (int) (mGridView.getChildAt(0).getHeight() / getContext().getResources().getDisplayMetrics().density);
        }

        return this.rowHeight;
    }

    public void setRowHeightHint(int rowHeight) {
        this.rowHeight = rowHeight;

        this.reload();
    }

    public int getRowHeaderWidth() {
        return rowHeaderWidth;
    }

    public void setRowHeaderWidth(int rowHeaderWidth) {
        this.rowHeaderWidth = (int) ((float)rowHeaderWidth * getContext().getResources().getDisplayMetrics().density);

        View corner = findViewById(R.id.spreadsheet_corner_spacer);
        corner.getLayoutParams().width = this.rowHeaderWidth;

        mRowHeaderGridView.getLayoutParams().width = this.rowHeaderWidth;
        mRowHeaderGridView.setColumnWidth(this.rowHeaderWidth);

        this.reload();
    }

    /**
     * There are a handful of views that constitute the "background" so calls to set the background
     * color are aggregated here and passed to the appropriate views.
     */

    @Override
    public void setBackground(Drawable background) {
        if (mGridView != null) {
            mGridView.setBackground(background);
            mRowHeaderGridView.setBackground(background);
            mColumnHeaderGridView.setBackground(background);
            mVerticalSpacerTop.setBackground(this.getBackground());
            mVerticalSpacerBottom.setBackground(this.getBackground());
            mSpreadsheetContainer.setBackground(this.getBackground());
        }

        super.setBackground(background);
    }

    @Override
    public void setBackgroundColor(int color) {
        if (mGridView != null) {
            mGridView.setBackgroundColor(color);
            mRowHeaderGridView.setBackgroundColor(color);
            mColumnHeaderGridView.setBackgroundColor(color);
            mVerticalSpacerTop.setBackgroundColor(color);
            mVerticalSpacerBottom.setBackgroundColor(color);
            mSpreadsheetContainer.setBackgroundColor(color);
        }

        super.setBackgroundColor(color);
    }
}
