package acc.aviato;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

public class FavoritesFragment extends ListFragment {

    public static final String TAG = FavoritesFragment.class.getSimpleName();

    protected List<ParseObject> mEvents;
    protected ParseRelation<ParseObject> mFavoritesRelation;
    protected ParseUser currentUser;

    public FavoritesFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_favorites_activity, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.title_items, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
        if (ParseUser.getCurrentUser() != null) {
            currentUser = ParseUser.getCurrentUser();
            mFavoritesRelation = currentUser.getRelation(ParseConstants.KEY_FAVORITE_EVENTS_REALATION);

            ParseQuery<ParseObject> query = mFavoritesRelation.getQuery();
            query.orderByAscending(ParseConstants.KEY_USERNAME);
            query.setLimit(1000);
        /*FIXME: add a loader for this*/
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> events, ParseException e) {
                    if (e == null) {
                    /* success */
                        mEvents = events;
                        String[] favoriteEvents = new String[mEvents.size()];
                        int i = 0;
                        for (ParseObject object : mEvents) {
                            favoriteEvents[i] = object.getString(ParseConstants.KEY_EVENT_NAME);
                            i++;
                        }
                        // Occasional error below: Attempt to invoke virtual method 'java.lang.Object android.content.Context.getSystemService(java.lang.String)' on a null object reference
                        // I think getContext() is not available yet
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                                android.R.layout.simple_list_item_1, favoriteEvents);
                        setListAdapter(adapter);

                    } else {
                    /* error */
                        Log.e(TAG, e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(e.getMessage())
                                .setTitle(getString(R.string.error_title))
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        } else {
            ParseQuery<ParseObject> favoriteQuery = ParseQuery.getQuery(ParseConstants.CLASS_EVENTS)
                    .fromLocalDatastore();

            favoriteQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> events, ParseException e) {
                    if (e == null) {
                        mEvents = events;
                        String[] favoriteEvents = new String[mEvents.size()];
                        int i = 0;
                        for (ParseObject object : mEvents) {
                            favoriteEvents[i] = object.getString(ParseConstants.KEY_EVENT_NAME);
                            i++;
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                getContext(), android.R.layout.simple_list_item_1,
                                favoriteEvents);
                        setListAdapter(adapter);

                    } else {
                    /* error */
                        Log.e(TAG, e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(e.getMessage())
                                .setTitle(getString(R.string.error_title))
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        }
    }
}