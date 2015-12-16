package com.dogfishsoftware.shanegianelli.spreadsheet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dogfishsoftware.shanegianelli.spreadsheetview.adapters.SpreadsheetAdapter;

/**
 * Created by Shane Gianelli on 9/18/15.
 *
 * An example implementation of the SpreadsheetAdapter, this functions nearly identically to any
 * typical ListAdapter with the added caveat that all positions are in two dimensions.
 */
public class SampleAdapter extends SpreadsheetAdapter {

    public class ViewHolder {
        public TextView textView;
    }

    private LayoutInflater mInflater;

    public SampleAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getNumRows() {
        return 50;
    }

    @Override
    public int getNumColumns() {
        return 10;
    }

    @Override
    public Object getItem(Coordinates coordinates) {
        return "" + (coordinates.row + 1) * (coordinates.column + 1);
    }

    @Override
    public long getItemId(Coordinates coordinates) {
        return coordinates.row * coordinates.column;
    }

    @Override
    public View getView(Coordinates coordinates, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spreadsheet_item_view, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.spreadsheet_item_text_view);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textView.setText((String) getItem(coordinates));

        convertView.setBackgroundColor(coordinates.row % 2 == 0 ? 0xffffaaaa : 0xffffffff);

        if (this.getSelectedCoordinates().equals(coordinates)) {
            convertView.setBackgroundColor(0xffdfdfff);
        }

        return convertView;
    }

    @Override
    public View getRowHeaderView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spreadsheet_item_view, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.spreadsheet_item_text_view);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        convertView.setBackgroundColor(position % 2 == 0 ? 0xffffaaaa : 0xffffffff);

        if (position == getSelectedCoordinates().row) {
            convertView.setBackgroundColor(0xffaaaaff);
        }

        viewHolder.textView.setText("" + (position + 1));

        return convertView;
    }

    @Override
    public View getColumnHeaderView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spreadsheet_item_view, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.spreadsheet_item_text_view);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position == getSelectedCoordinates().column) {
            convertView.setBackgroundColor(0xffaaaaff);
        } else {
            convertView.setBackgroundColor(0xffffffff);
        }

        viewHolder.textView.setText("" + (position + 1));

        return convertView;
    }
}
