package acc.aviato;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    protected SectionsPageAdapter mSectionsPagerAdapter;
    protected ViewPager mViewPager;

    SwipeRefreshLayout mSwipeRefreshLayout;
    FloatingActionButton mFab;

    int mCurrentTabPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragmented);

        mSectionsPagerAdapter = new SectionsPageAdapter(this, getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(0, false);    // Sets the default tab

        mViewPager.addOnPageChangeListener (new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // This makes the refresh less sensitive. If the page is scrolling already, the refresh layout is disabled.
                mSwipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });

        mFab = (FloatingActionButton) findViewById(R.id.fab_feed);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser() == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(getString(R.string.error_sign_in))
                            .setTitle(getString(R.string.error_title_sign_in))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Intent intent = new Intent(MainActivity.this, CreateEventActivity.class);
                    startActivity(intent);
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurrentTabPosition = tab.getPosition();
                mViewPager.setCurrentItem(tab.getPosition());
                animateFab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_blue_light,
                android.R.color.holo_green_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setDistanceToTriggerSync(150);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mCurrentTabPosition == 0) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    FeedFragment feedFragment = (FeedFragment) getSupportFragmentManager().getFragments().get(mCurrentTabPosition);
                    feedFragment.refreshEvents();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.title_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        /*if(id == R.id.filter_events){
            createFilterDialog();
        }*/

        return super.onOptionsItemSelected(item);
    }

    protected void animateFab(final int position) {
        // Creating a custom animation will allow more controlled animations
        if (position != 0) {
            mFab.hide();
        } else {
            mFab.show();
        }
    }
}
