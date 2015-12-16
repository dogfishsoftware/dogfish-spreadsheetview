package com.dogfishsoftware.shanegianelli.spreadsheet;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dogfishsoftware.shanegianelli.spreadsheetview.adapters.SpreadsheetAdapter;
import com.dogfishsoftware.shanegianelli.spreadsheetview.views.SpreadsheetView;

public class MainActivity extends Activity {

    private SpreadsheetView mSpreadsheetView;
    private SampleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new SampleAdapter(this);

        mSpreadsheetView = (SpreadsheetView) findViewById(R.id.spreadsheet_view);

        // sets the width of the row header on the left-most side of the spreadsheet
        mSpreadsheetView.setRowHeaderWidth(40);

        // sets a column width with which to resize the containing view of the spreadsheet grid
        mSpreadsheetView.setColumnWidth(100);

        // because the height of the cells is determined by the view returned by the SpreadsheetAdapter
        // subclass, this just provides extra information for the SpreadsheetView on how to handle
        // resizing the content view
        mSpreadsheetView.setRowHeightHint(60);

        mSpreadsheetView.setAdapter(mAdapter);

        // this listener is used here to set the selection state of the spreadsheet view and allows
        // the user of the library flexibility on what and how to highlight selected cells and regions
        mSpreadsheetView.setOnSelectListener(new SpreadsheetView.OnSelectListener() {
            @Override
            public boolean onSelect(SpreadsheetAdapter.Coordinates coordinates) {
                if (mAdapter.getSelectedCoordinates().equals(coordinates)) {
                    mAdapter.setSelectedCoordinates(null);
                } else {
                    mAdapter.setSelectedCoordinates(coordinates);
                }

                mSpreadsheetView.reload();

                return true;
            }

            @Override
            public boolean onRowSelect(int position) {
                return false;
            }

            @Override
            public boolean onColumnSelect(int position) {
                return false;
            }
        });

        // creates a listener for long press gestures on cells
        mSpreadsheetView.setOnLongSelectListener(new SpreadsheetView.OnLongSelectListener() {
            @Override
            public void onLongSelect(SpreadsheetAdapter.Coordinates coordinates, View view) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
