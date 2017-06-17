package com.rincyan.smsdelete.utils;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Telephony;
import android.widget.Toast;

import com.rincyan.smsdelete.recyclerview.SMS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * Created by rin on 2017/6/15.
 */

public class SMSHandler {
    private Context context;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private static final Uri SMS_URI = Uri.parse("content://sms/inbox");
    private static final String[] ALL_THREADS_PROJECTION = {
            "_id", "address", "person", "body",
            "date", "type", "thread_id"};

    public SMSHandler(Context context) {
        this.context = context;
    }

    public ArrayList getSMS() {
        final ArrayList smsData = new ArrayList<>();
        try {
            ContentResolver resolver = context.getContentResolver();
            final Cursor cursor = resolver.query(SMS_URI, ALL_THREADS_PROJECTION,
                    null, null, "date desc");
            isCapture ic = new isCapture(context);
            while ((cursor.moveToNext())) {
                int indexBody = cursor.getColumnIndex("body");
                int indexAddress = cursor.getColumnIndex("address");
                int indexDate = cursor.getColumnIndex("date");
                int indexId = cursor.getColumnIndex("_id");
                Long id = cursor.getLong(indexId);
                String body = cursor.getString(indexBody);
                String address = cursor.getString(indexAddress);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd hh:mm");
                Date formatDate = new Date(Long.parseLong(cursor.getString(indexDate)));
                String date = dateFormat.format(formatDate);
                preferences = context.getSharedPreferences("setting", context.MODE_PRIVATE);
                Boolean checked = preferences.getBoolean("advance", false);
                if (!checked) {
                    if (ic.simpleDetect(body)) {
                        smsData.add(new SMS(address, body, date, id));
                    }
                } else {
                    if (ic.advanceDetect(body)) {
                        smsData.add(new SMS(address, body, date, id));
                    }
                }
            }
            if (smsData.isEmpty()) {
                smsData.add(new SMS("未检测到数据，请尝试下拉刷新", "", "", (long) -1));
            }
            cursor.close();

        } catch (Exception e) {
            smsData.add(new SMS("未得到短信读取权限", "", "", (long) -1));
        }
        return smsData;
    }

    public int deleteSms(String smsId) {
        if (!Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName()) | Objects.equals(smsId, "-1")) {
            return 0;
        }
        String uri = "content://sms/" + smsId;
        return context.getContentResolver().delete(Uri.parse(uri),
                null, null);
    }
}
