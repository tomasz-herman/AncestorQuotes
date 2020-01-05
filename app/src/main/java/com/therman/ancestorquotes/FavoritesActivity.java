package com.therman.ancestorquotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Objects;

public class FavoritesActivity extends AppCompatActivity {

    RecyclerView rvQuotes;
    FragmentManager fragmentManager;
    MenuItem iSearch, iFavorites, iAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        fragmentManager = getSupportFragmentManager();
        rvQuotes = findViewById(R.id.rvQuotes);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.favorites));
    }

    @Override
    protected void onStart() {
        super.onStart();
        ArrayList<Quote> quotes = AncestorQuotes.database.getQuotes();
        ArrayList<Quote> favorites = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        for (Quote quote : quotes) if (prefs.contains(quote.getSource())) favorites.add(quote);
        ((QuoteAdapter) Objects.requireNonNull(rvQuotes.getAdapter())).replaceData(favorites);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        iSearch = menu.findItem(R.id.iSearch);
        iFavorites = menu.findItem(R.id.iFavorites);
        iFavorites.setVisible(false);
        iAbout = menu.findItem(R.id.iAbout);
        iAbout.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(FavoritesActivity.this, AboutActivity.class);
            startActivity(intent);
            return false;
        });
        SearchView searchView = (SearchView) iSearch.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ((Filterable) Objects.requireNonNull(rvQuotes.getAdapter())).getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
