package com.dogfishsoftware.shanegianelli.spreadsheetview.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * The overall purpose of this class is to aggregate the adapters for the header GridViews into one
 * main adapter. You should never need to subclass this adapter.
 */
public class SpreadsheetHeaderAdapter extends BaseAdapter {

    /**
     * Aggregate adapter to forward messages to
     */
    private SpreadsheetAdapter mParentAdapter;

    /**
     * Tells this adapter whether or not to act as a column header adapter or a row a header adapter
     */
    private boolean mColumnAdapter = false;

    /**
     * Default constructor
     *
     * @param parentAdapter Aggregate adapter to forward messages to
     * @param columnAdapter Flag telling this adapter whether or not it is a column header adapter
     */
    public SpreadsheetHeaderAdapter(SpreadsheetAdapter parentAdapter, boolean columnAdapter) {
        mParentAdapter = parentAdapter;
        mColumnAdapter = columnAdapter;
    }

    /**
     * Returns the number of items based on the size and shape of the spreadsheet.
     *
     * @return Number of items in the adapter
     */
    @Override
    public int getCount() {
        return mColumnAdapter ? mParentAdapter.getNumColumns() : mParentAdapter.getNumRows();
    }

    @Override
    public long getItemId(int item) {
        return item;
    }

    @Override
    public Object getItem(int item) {
        return "" + item;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (mColumnAdapter) {
            return mParentAdapter._getColumnHeaderView(position, convertView, parent);
        } else {
            return mParentAdapter._getRowHeaderView(position, convertView, parent);
        }
    }
}
