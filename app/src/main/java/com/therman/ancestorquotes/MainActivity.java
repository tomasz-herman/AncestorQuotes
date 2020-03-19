package com.therman.ancestorquotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements CategoryAdapter.ItemClicked {

    private static final int NUM_PAGES = 2;
    private static final String TAG = "MainActivity";

    private Fragment categoriesFragment, quotesFragment;
    private ViewPager2 viewPager;
    private FragmentStateAdapter  pagerAdapter;

    //RecyclerView rvQuotes;
    FragmentManager fragmentManager;
    MenuItem iSearch, iFavorites, iAbout;
    String lastCategory, searchQuery;
    boolean shownQuotesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        restoreState(savedInstanceState);
        viewPager = findViewById(R.id.vpPager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            categoriesFragment = getSupportFragmentManager().getFragment(savedInstanceState, "categoriesFragment");
            quotesFragment = getSupportFragmentManager().getFragment(savedInstanceState, "quotesFragment");
            lastCategory = savedInstanceState.getString("lastCategory");
            searchQuery = savedInstanceState.getString("searchQuery");
            shownQuotesFragment = savedInstanceState.getBoolean("shownQuotesFragment");
            setTitle(lastCategory);
            filterQuotes(lastCategory);
        } else {
            categoriesFragment = new CategoriesFragment();
            quotesFragment = new QuotesFragment();
            lastCategory = "All";
            searchQuery = "";
            shownQuotesFragment = false;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "categoriesFragment", categoriesFragment);
        getSupportFragmentManager().putFragment(outState, "quotesFragment", quotesFragment);
        outState.putString("lastCategory", lastCategory);
        outState.putString("searchQuery", searchQuery);
        outState.putBoolean("shownQuotesFragment", shownQuotesFragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(iSearch.isActionViewExpanded()){
            SearchView searchView = (SearchView) iSearch.getActionView();
            if(searchView.getQuery().length() != 0){
                searchQuery = "" + searchView.getQuery();
            }
        } else searchQuery = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private void filterQuotes(String category){
        ArrayList<Quote> quotes = AncestorQuotes.database.getCategorizedQuotes(category);
        if(((QuotesFragment)quotesFragment).getRecyclerView()!=null)
        ((QuoteAdapter) Objects.requireNonNull(((QuotesFragment)quotesFragment).getRecyclerView().getAdapter())).replaceData(quotes);
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
                searchQuery = query;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ((Filterable) Objects.requireNonNull(((QuotesFragment)quotesFragment).getRecyclerView().getAdapter())).getFilter().filter(newText);
                return false;
            }
        });
        if(!searchQuery.isEmpty()){
            iSearch.expandActionView();
            searchView.setQuery(searchQuery, false);
            searchView.clearFocus();
        }
        return super.onCreateOptionsMenu(menu);
    }

//    private boolean isPortraitMode(){
//        return findViewById(R.id.layout_portrait) != null;
//    }
//
//    private boolean isLandscapeMode(){
//        return findViewById(R.id.layout_landscape) != null;
//    }

    @Override
    public void onItemClicked(String category) {
        filterQuotes(lastCategory = category);
        shownQuotesFragment = true;
        viewPager.setCurrentItem(1, true);
        ((Filterable) Objects.requireNonNull(((QuotesFragment)quotesFragment).getRecyclerView().getAdapter())).getFilter().filter(((SearchView)iSearch.getActionView()).getQuery());
        setTitle(category);
    }

    public void setTitle(String title){
        Log.d(TAG, "setTitle: " + (getSupportActionBar() == null));
        Objects.requireNonNull(getSupportActionBar()).setTitle("Ancestor Quotes " + title);
    }


    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if(position == 0)return categoriesFragment;
            else return quotesFragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }

    }

}
