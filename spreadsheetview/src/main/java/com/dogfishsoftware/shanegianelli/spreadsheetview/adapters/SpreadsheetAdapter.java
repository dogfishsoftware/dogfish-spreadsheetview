package com.dogfishsoftware.shanegianelli.spreadsheetview.adapters;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.dogfishsoftware.shanegianelli.spreadsheetview.views.SpreadsheetView;

public abstract class SpreadsheetAdapter extends BaseAdapter {

    /**
     * Abstraction from the normal `getCount` method of an adapter to better handle the abstract
     * notion of a two-dimensional data source.
     */
    public static class Coordinates {
        public int row;
        public int column;

        /**
         * Convenient constructor for Coordinates
         *
         * @param row Row
         * @param column Column
         */
        public Coordinates(int row, int column) {
            this.row = row;
            this.column = column;
        }

        /**
         * @return Returns an instance of an unselected coordinate position
         */
        public static Coordinates Unselected() {
            return new Coordinates(-1, -1);
        }

        @Override
        public String toString() {
            return String.format("%s Row=%d Column=%d", super.toString(), this.row, this.column);
        }

        public boolean equals(Coordinates coordinates) {
            return this.column == coordinates.column && this.row == coordinates.row;
        }
    }

    /**
     * Selection listener callback instance
     */
    private SpreadsheetView.OnSelectListener onSelectListener;

    /**
     * The position of the currently selected cell, otherwise null
     */
    private Coordinates selectedCoordinates = Coordinates.Unselected();

    /**
     * Default constructor.
     */
    public SpreadsheetAdapter() {}

    /**
     * Transforms a given position into a 2-dimensional representation
     *
     * @param position Linear position
     * @return `position` as Coordinates
     */
    public Coordinates getCoordinates(int position) {
        return new Coordinates(position / this.nColumns, position % this.nColumns);
    }

    /**
     * @return Returns `onSelectListener`
     */
    public SpreadsheetView.OnSelectListener getOnSelectListener() {
        return onSelectListener;
    }

    /**
     * @param onSelectListener Sets a new `onSelectListener`
     */
    public void setOnSelectListener(SpreadsheetView.OnSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
    }

    /**
     * @return The currently selected item. Always returns an instance of Coordinates. If nothing
     * is selected, an instance of Coordinates.Unselected() is returned.
     */
    public Coordinates getSelectedCoordinates() {
        return selectedCoordinates;
    }

    /**
     * Changes the selected cell to the new coordinates
     *
     * @param selectedCoordinates The new position to be selected
     */
    public void setSelectedCoordinates(Coordinates selectedCoordinates) {
        if (selectedCoordinates == null) {
            this.selectedCoordinates = Coordinates.Unselected();
        } else {
            this.selectedCoordinates = selectedCoordinates;
        }
    }

    /**
     * @return Whether or not a cell is currently selected
     */
    @SuppressWarnings("unused")
    public boolean isItemSelected() {
        return !this.selectedCoordinates.equals(Coordinates.Unselected());
    }

    /**
     * Number of rows in the spreadsheet
     */
    private int nRows = 0;

    /**
     * Number of columns in the spreadsheet
     */
    private int nColumns = 0;

    /**
     * @return Number of rows to present in the spreadsheet
     */
    public abstract int getNumRows();

    /**
     * @return Number of columns to present in the spreadsheet
     */
    public abstract int getNumColumns();

    /**
     * Convenient way of loading results into the grid without the silly `getCount` wrap around
     *
     * @param coordinates The 2-dimensional position of the item
     * @return The data item at the given coordinates
     */
    public abstract Object getItem(Coordinates coordinates);

    /**
     * Convenient mapping of BaseAdapter abstract method to use the coordinate system instead of position
     *
     * @param coordinates The 2-dimensional position of the item
     * @return The id of the item at the given coordinates
     */
    public abstract long getItemId(Coordinates coordinates);

    /**
     * Convenient mapping of BaseAdapter getView() method to use coordinate system
     *
     * @param coordinates Position of the item
     * @param convertView Returned content view of the cell
     * @param parent Containing super view of the `convertView`
     * @return The view to place at the given position
     */
    public abstract View getView(Coordinates coordinates, View convertView, ViewGroup parent);

    /**
     * Requests a view to place in the header of the spreadsheet view
     *
     * @param position Index of the header item as an int from 0...getNumRows() - 1
     * @param convertView View to contain header item view
     * @param parent Containing super view of the `convertView`
     * @return The view to place in the header at the given position
     */
    public abstract View getRowHeaderView(int position, View convertView, ViewGroup parent);

    /**
     * Requests a view to place in the header of the spreadsheet view
     *
     * @param position Index of the header item as an int from 0...getNumColumns() - 1
     * @param convertView View to contain header item view
     * @param parent Containing super view of the `convertView`
     * @return The view to place in the header at the given position
     */
    public abstract View getColumnHeaderView(int position, View convertView, ViewGroup parent);

    @Override
    public int getCount() {
        return nRows * nColumns;
    }

    @Override
    public Object getItem(int position) {
        return getItem(getCoordinates(position));
    }

    @Override
    public long getItemId(int position) {
        return getItemId(getCoordinates(position));
    }

    @Override
    public void notifyDataSetChanged() {
        this.nRows = getNumRows();
        this.nColumns = getNumColumns();

        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Coordinates coordinates = getCoordinates(position);
        convertView = getView(coordinates, convertView, parent);

        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (onSelectListener != null) {
                    onSelectListener.onSelect(coordinates);
                }

                return false;
            }
        });

        return convertView;
    }

    /**
     * Routes the `getView()` method from the row header GridView to the SpreadsheetAdapter subclass
     * and adds in a touch handler to the returned view.
     */
    public View _getRowHeaderView(final int position, View convertView, final ViewGroup parent) {
        convertView = getRowHeaderView(position, convertView, parent);
        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (onSelectListener != null && event.getAction() == MotionEvent.ACTION_DOWN) &&
                        onSelectListener.onRowSelect(position);
            }
        });

        return convertView;
    }

    /**
     * Routes the `getView()` method from the column header GridView to the SpreadsheetAdapter
     * subclass and adds in a touch handler to the returned view.
     */
    public View _getColumnHeaderView(final int position, View convertView, final ViewGroup parent) {
        convertView = getColumnHeaderView(position, convertView, parent);
        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (onSelectListener != null && event.getAction() == MotionEvent.ACTION_DOWN) &&
                        onSelectListener.onColumnSelect(position);
            }
        });

        return convertView;
    }
}