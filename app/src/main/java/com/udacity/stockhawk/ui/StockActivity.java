package com.udacity.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private Uri stockUri;
    @BindView(R.id.stock_chart)
    LineChart lineChart;
    private Context context;
    private static final int LOADER_ID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        stockUri = getIntent().getData();
        setTitle(getIntent().getStringExtra(Contract.Quote.COLUMN_SYMBOL));

        ButterKnife.bind(this);
        context = this;
        configureChart();

        LoaderManager manager = getLoaderManager();
        if (manager.getLoader(LOADER_ID) == null)
            manager.initLoader(LOADER_ID, null, this);
        else {
            manager.restartLoader(LOADER_ID, null, this);
        }
    }

    private void configureChart() {
        lineChart.setPinchZoom(false);
        lineChart.getXAxis().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateX(1500);
    }

    private LineData configureDataSet(LineDataSet lineDataSet) {
        lineDataSet.setLineWidth(2);
        lineDataSet.setHighlightEnabled(false);
        return new LineData(lineDataSet);
    }

    private void setData(String dataHQ) {
        List<Entry> entries = new ArrayList<>();
        int i = 0;
        for (String hq : dataHQ.split("\n")) {
            String[] values = hq.split(",");
            entries.add(new Entry(i++, Float.parseFloat(values[1])));
        }
        // create a data object with the datasets
        LineData dataSet = configureDataSet(new LineDataSet(entries, "values"));

        // set data
        lineChart.setData(dataSet);
        lineChart.invalidate(); // refresh
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (stockUri != null) {
            return new CursorLoader(
                    context,
                    stockUri,
                    new String[] {Contract.Quote.COLUMN_HISTORY},
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        String history =  data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
        setData(history);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
