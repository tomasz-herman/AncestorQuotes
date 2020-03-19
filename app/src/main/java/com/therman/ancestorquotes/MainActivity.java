package com.therman.ancestorquotes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewPager.setPageTransformer(new DepthPageTransformer());
        } else viewPager.setPageTransformer(new ZoomOutPageTransformer());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position == 0){
                    setTitle(getString(R.string.app_name));
                } else {
                    setTitle(lastCategory);
                }
            }
        });
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
    }

    public void setTitle(String title){
        Log.d(TAG, "setTitle: " + (getSupportActionBar() == null));
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
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

    @RequiresApi(21)
    private static class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setTranslationZ(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Move it behind the left page
                view.setTranslationZ(-1f);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }

    private static class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }


}
