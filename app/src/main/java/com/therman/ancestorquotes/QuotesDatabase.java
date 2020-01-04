package com.therman.ancestorquotes;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class QuotesDatabase {

    private ArrayList<Quote> quotes;

    public QuotesDatabase(Context context) {
        quotes = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(loadQuotes(context));
            JSONArray jsonArray = obj.getJSONArray("quotes");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String audio = jsonObject.getString("audio");
                String text = jsonObject.getString("text");
                quotes.add(new Quote(audio, text));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (Quote quote : quotes) Log.d("Quote", quote.getText());
    }

    public String loadQuotes(Context context) {
        String json;
        try(InputStream is = context.getAssets().open("quotes.json")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            json = new String(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public ArrayList<Quote> getQuotes() {
        return quotes;
    }
}
