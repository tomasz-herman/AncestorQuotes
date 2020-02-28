package com.therman.ancestorquotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements CategoryAdapter.ItemClicked {

    RecyclerView rvQuotes;
    FragmentManager fragmentManager;
    MenuItem iSearch, iFavorites, iAbout;
    static String lastCategory = "All";
    static boolean shownQuotesFragment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        rvQuotes = findViewById(R.id.rvQuotes);
        adjustFragments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(rvQuotes.getAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        filterQuotes(lastCategory);
    }

    @Override
    public void onBackPressed() {
        if(isLandscapeMode() || !shownQuotesFragment){
            lastCategory = "All";
            shownQuotesFragment = false;
            super.onBackPressed();
        } else {
            shownQuotesFragment = false;
            showCategoriesHideQuotes();
        }
    }

    private void filterQuotes(String category){
        ArrayList<Quote> quotes = AncestorQuotes.database.getCategorizedQuotes(category);
        ((QuoteAdapter) Objects.requireNonNull(rvQuotes.getAdapter())).replaceData(quotes);
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

    private boolean isPortraitMode(){
        return findViewById(R.id.layout_portrait) != null;
    }

    private boolean isLandscapeMode(){
        return findViewById(R.id.layout_landscape) != null;
    }

    private void adjustFragments(){
        if(isPortraitMode())
            if (shownQuotesFragment) hideCategoriesShowQuotes();
            else showCategoriesHideQuotes();
        if(isLandscapeMode()) showCategoriesAndQuotes();
    }

    private void hideCategoriesShowQuotes(){
        fragmentManager.beginTransaction()
                .hide(Objects.requireNonNull(fragmentManager.findFragmentById(R.id.fragCategories)))
                .show(Objects.requireNonNull(fragmentManager.findFragmentById(R.id.fragQuotes)))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        if(iSearch != null) iSearch.setVisible(true);

    }

    private void showCategoriesHideQuotes(){
        fragmentManager.beginTransaction()
                .show(Objects.requireNonNull(fragmentManager.findFragmentById(R.id.fragCategories)))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .hide(Objects.requireNonNull(fragmentManager.findFragmentById(R.id.fragQuotes)))
                .commit();
        if(iSearch != null) iSearch.setVisible(false);

    }

    private void showCategoriesAndQuotes(){
        fragmentManager.beginTransaction()
                .show(Objects.requireNonNull(fragmentManager.findFragmentById(R.id.fragCategories)))
                .show(Objects.requireNonNull(fragmentManager.findFragmentById(R.id.fragQuotes)))
                .commit();
        if(iSearch != null) iSearch.setVisible(true);
    }

    @Override
    public void onItemClicked(String category) {
        filterQuotes(lastCategory = category);
        shownQuotesFragment = true;
        if(findViewById(R.id.layout_portrait) != null) hideCategoriesShowQuotes();
        if(isLandscapeMode()){
            ((Filterable) Objects.requireNonNull(rvQuotes.getAdapter())).getFilter().filter(((SearchView)iSearch.getActionView()).getQuery());
        }
    }
}
