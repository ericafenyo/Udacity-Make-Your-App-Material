package com.example.xyzreader.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.utils.HelperMethods;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private Cursor mCursor;
    private long mStartId;
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private long mSelectedItemId;
    String[] textSizeItems;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private int mChechedItem;
    private static final String PREF_TEXT_SIZE_KEY = "pref_text_size";
    private static final String PREF_TEXT_SIZE = "PREF_TEXT_SIZE";
    private SharedPreferences preferences;
    private static final String PREF_PAGE_POS = "pref_page_pos";
    int itemId;

    @BindView(R.id.share_fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);

        preferences = getSharedPreferences(PREF_TEXT_SIZE, MODE_PRIVATE);

        // configures and add back arrow to toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        fab.setOnClickListener(this);

        getLoaderManager().initLoader(0, null, this);

        int mDefaultCheckedItem = 1;

        //initialize variables for configuration dialog box
        mChechedItem = HelperMethods.getTextPreferences(getApplicationContext(),
                PREF_TEXT_SIZE_KEY, mDefaultCheckedItem);
        textSizeItems = getResources().getStringArray(R.array.text_size_values);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);


        ViewCompat.setOnApplyWindowInsetsListener(mPager,
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        insets = ViewCompat.onApplyWindowInsets(v, insets);
                        if (insets.isConsumed()) {
                            return insets;
                        }

                        boolean consumed = false;
                        for (int i = 0, count = mPager.getChildCount(); i < count; i++) {
                            ViewCompat.dispatchApplyWindowInsets(mPager.getChildAt(i), insets);
                            if (insets.isConsumed()) {
                                consumed = true;
                            }
                        }
                        return consumed ? insets.consumeSystemWindowInsets() : insets;
                    }
                });

        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                checkPreferenceChange();
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);

                //saves view pager scroll position
                //this value is retrieved and used in configuration dialog box
                HelperMethods.storeTextPreferences(getApplicationContext(), PREF_PAGE_POS, position);
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //resets saved viewpager scroll position to zero
        HelperMethods.storeTextPreferences(getApplicationContext(), PREF_PAGE_POS, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                final AlertDialog.Builder builder = new AlertDialog.Builder(ArticleDetailActivity.this)
                        .setTitle(R.string.text_size)
                        .setSingleChoiceItems(textSizeItems, mChechedItem, new DialogInterface
                                .OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                checkPreferenceChange();
                                //saves the position of radio buttons in configuration dialog box
                                HelperMethods.storeTextPreferences(getApplicationContext(),
                                        PREF_TEXT_SIZE_KEY, position);

                                //this refreshes the page(fragment) after the user selects a text
                                // size
                                mPager.setAdapter(mPagerAdapter);
                                // "itemId" = viewpager current position(mPager.getCurrentItem())
                                // position is maintained after the page refreshes
                                mPager.setCurrentItem(itemId);
                            }
                        }).setPositiveButton(R.string.positive_text, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    /**
     * gets stored shared preference values without restarting the activity
     */
    private void checkPreferenceChange() {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences sharedPreferences, String key) {
                int mDefaultCheckedItem = 1;
                if (Objects.equals(key, PREF_TEXT_SIZE_KEY)) {
                    mChechedItem = sharedPreferences.getInt(key, mDefaultCheckedItem);
                } else if (key == PREF_PAGE_POS) {
                    itemId = sharedPreferences.getInt(PREF_PAGE_POS,
                            0);
                }
            }
        };

        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from
                (ArticleDetailActivity.this)
                .setType("text/plain")
                .setText("Some sample text")
                .getIntent(), getString(R.string.action_share)));
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}