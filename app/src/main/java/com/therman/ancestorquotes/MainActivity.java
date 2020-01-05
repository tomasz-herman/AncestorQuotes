package com.therman.ancestorquotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Filterable;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    RecyclerView rvQuotes;
    FragmentManager fragmentManager;
    MenuItem iSearch, iFavorites, iAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        rvQuotes = findViewById(R.id.rvQuotes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(rvQuotes.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        iSearch = menu.findItem(R.id.iSearch);
        iFavorites = menu.findItem(R.id.iFavorites);
        iFavorites.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
            return false;
        });
        iAbout = menu.findItem(R.id.iAbout);
        iAbout.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
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
