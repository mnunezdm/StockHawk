package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewFactory();
    }

    private class ListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        private Cursor cursor = null;
        private final DecimalFormat dollarFormatWithPlus;
        private final DecimalFormat dollarFormat;
        private final DecimalFormat percentageFormat;

        ListRemoteViewFactory() {
            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
        }

        @Override
        public void onCreate() {
            cursor = getContentResolver().query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);
        }

        @Override
        public void onDataSetChanged() {
            if (cursor != null) cursor.close();
            final long identityToken = Binder.clearCallingIdentity();
            cursor = getContentResolver().query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (cursor != null) cursor.close();
        }

        @Override
        public int getCount() {
            return cursor == null ? 0 : cursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || cursor == null
                    || !cursor.moveToPosition(position)) {
                return null;
            }

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.stock_item_widget);

            String stockSymbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
            Float stockPrice = cursor.getFloat(Contract.Quote.POSITION_PRICE);
            Float absoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

            int backgroundDrawable = absoluteChange > 0 ? R.drawable.percent_change_pill_green :
                    R.drawable.percent_change_pill_red;

            remoteViews.setTextViewText(R.id.widget_symbol, stockSymbol);
            remoteViews.setTextViewText(R.id.widget_price, dollarFormat.format(stockPrice));
            remoteViews.setTextViewText(R.id.widget_change, dollarFormatWithPlus.format(absoluteChange));
            remoteViews.setInt(R.id.change, "setBackgroundResource", backgroundDrawable);

            final Intent fillInIntent = new Intent();
            Uri stockUri = Contract.Quote.makeUriForStock(stockSymbol);
            fillInIntent.setData(stockUri);
            remoteViews.setOnClickFillInIntent(R.id.ll_item_widget, fillInIntent);
            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return cursor.moveToPosition(position) ? cursor.getLong(Contract.Quote.POSITION_ID) : position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

}
