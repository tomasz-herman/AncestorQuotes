package com.therman.ancestorquotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class QuotesDatabase {

    private ArrayList<Quote> quotes;
    private ArrayList<Quote> favorites;
    private ArrayList<String> categoriesList;
    private HashMap<String, ArrayList<Quote>> categorizedQuotes;

    public QuotesDatabase(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        quotes = new ArrayList<>();
        favorites = new ArrayList<>();
        categorizedQuotes = new HashMap<>();
        categoriesList = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(loadQuotes(context));
            JSONArray jsonArray = obj.getJSONArray("quotes");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String audio = jsonObject.getString("audio");
                String altAudio = jsonObject.getString("altAudio");
                String text = jsonObject.getString("text");
                Quote quote = new Quote(audio, altAudio, text);
                quotes.add(quote);
                if (prefs.contains(audio)) favorites.add(quote);
                String categories = jsonObject.getString("categories");
                String[] cats = categories.split("\\|");
                for (String category : cats) {
                    if(categorizedQuotes.containsKey(category)){
                        ArrayList<Quote> quoteList = categorizedQuotes.get(category);
                        Objects.requireNonNull(quoteList).add(quote);
                    }
                    else {
                        categoriesList.add(category);
                        ArrayList<Quote> quoteList = new ArrayList<>();
                        quoteList.add(quote);
                        categorizedQuotes.put(category, quoteList);
                    }
                }
            }
            categorizedQuotes.put("All", quotes);
            categorizedQuotes.put("Favorites", favorites);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sort(categoriesList);
        categoriesList.add(0, "Favorites");
        categoriesList.add(0, "All");
    }

    /*Function to sort array using insertion sort*/
    private void sort(ArrayList<String> categoriesList)
    {
        int n = categoriesList.size();
        for (int i = 1; i < n; ++i) {
            String key = categoriesList.get(i);
            int j = i - 1;

            /* Move elements of arr[0..i-1], that are
               greater than key, to one position ahead
               of their current position */
            while (j >= 0 && categoriesList.get(j).compareToIgnoreCase(key) > 0) {
                categoriesList.set(j + 1, categoriesList.get(j));
                j = j - 1;
            }
            categoriesList.set(j + 1, key);
        }
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

    public ArrayList<Quote> getFavorites() {
        return favorites;
    }

    public ArrayList<String> getCategories() {
        return categoriesList;
    }

    public ArrayList<Quote> getCategorizedQuotes(String category) {
        return categorizedQuotes.get(category);
    }
}
