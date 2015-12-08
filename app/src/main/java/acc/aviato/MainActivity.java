package acc.aviato;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;

public class MainActivity extends AppCompatActivity {

    protected SectionsPageAdapter mSectionsPagerAdapter;
    protected ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragmented);

        mSectionsPagerAdapter = new SectionsPageAdapter(this, getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(0, false);    // Sets the default tab

        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(mViewPager);
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

    /*public void createFilterDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton(R.string.filter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Set other dialog properties
        builder.setTitle("Filter Events");
        builder.setView(R.layout.content_filter_dialog);

        final AlertDialog dialog = builder.create();


        ParseQuery<ParseObject> comp = new ParseQuery<ParseObject>(ParseConstants.CLASS_TAGS);
        comp.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    //If I weren't lazy I would make a sorting algorithm here to sort this list by tag popularity, but I'm not even sure that would make a difference so I won't do it for now
                    String[] tagFilters = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        tagFilters[i]=list.get(i).get(ParseConstants.KEY_TAG_NAME).toString();
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_dropdown_item_1line,tagFilters);
                    AutoCompleteTextView textView = (AutoCompleteTextView)dialog.findViewById(R.id.filter_autocomplete);
                    textView.setAdapter(adapter);

                }
            }
        });

        // Create the AlertDialog

        dialog.show();
    }*/

}
