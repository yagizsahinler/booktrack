package com.example.booktrack;

import android.app.Application;

import android.text.format.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
    }

    //timestamp datasını tarihe .eviren static metod
    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", cal).toString();

        return date;
    }

}
