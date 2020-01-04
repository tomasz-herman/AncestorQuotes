package com.therman.ancestorquotes;

import android.app.Application;

public class AncestorQuotes extends Application {

    public static QuotesDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = new QuotesDatabase(this);
    }
}
