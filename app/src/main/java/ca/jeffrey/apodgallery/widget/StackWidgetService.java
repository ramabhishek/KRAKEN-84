package ca.jeffrey.apodgallery.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import ca.jeffrey.apodgallery.R;

public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private int mCount = 10;
    private List<WidgetItem> mWidgetItems = new ArrayList<>();
    private Context mContext;
    private int mAppWidgetId;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public int countFiles() {
        // Replace with preferences value
        File file=new File(Environment.getExternalStorageDirectory().getPath() + "APOD");
        File[] list = file.listFiles();

        mCount = list.length;
        for (File f: list){
            String name = f.getName();
            if (name.endsWith(".jpg"));
        }
        return 0;

    }
    public void onCreate() {
        // TODO When instantiating ConfigActivity, check that Storage permissions are enabled.
        Log.i("ONCREATE", "created");
        Calendar cal = GregorianCalendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM dd yyyy");
        Log.i("PATH", Environment.getExternalStorageDirectory().getPath());
        // Replace with preferences value
        File file=new File(Environment.getExternalStorageDirectory().getPath() + "/APOD/");
        File[] list = file.listFiles();

        if (list == null) {
            mCount = 0;
        }
        else {
            mCount = list.length;

            for (File f : list) {
                String name = f.getName();
                if (name.endsWith(".jpg")) {
                    name = name.replaceAll(".jpg", "");
                    mWidgetItems.add(new WidgetItem(name));
                }
            }
        }

        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        // for (int i = 0; i < mCount; i++) {
        //     String day = shortDateFormat.format(date);
        //     Log.i("DATE", day);
        //     mWidgetItems.add(new WidgetItem(day));
        //     cal.add(Calendar.DAY_OF_YEAR, -1);
        //     date = cal.getTime();
        // }
        // We sleep for 3 seconds here to show how the empty view appears in the interim.
        // The empty view is set in the StackWidgetProvider and should be a sibling of the
        // collection view.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear();
    }
    public int getCount() {
        return mCount;
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.
        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.layout_item);
        rv.setTextViewText(R.id.widget_text, mWidgetItems.get(position).getFormattedDate());

        final String IMAGE_DIRECTORY = "/sdcard/APOD/";
        final String EXT = ".jpg";

        try {
            File f = new File(IMAGE_DIRECTORY + mWidgetItems.get(position).getDate() + EXT);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            rv.setImageViewBitmap(R.id.widget_image, b);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(WidgetProvider.EXTRA_ITEM, position);
        extras.putString(WidgetProvider.EXTRA_DATE, mWidgetItems.get(position).getDate());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.stackWidgetItem, fillInIntent);
        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
        try {
            System.out.println("Loading view " + position);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Return the remote views object.
        return rv;
    }
    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }
    public int getViewTypeCount() {
        return 1;
    }
    public long getItemId(int position) {
        return position;
    }
    public boolean hasStableIds() {
        return true;
    }
    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }
}