package acc.aviato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

public class FriendsFragment extends ListFragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> friendsRelation;
    protected ParseUser currentUser;

    public FriendsFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_friends_activity, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_feed);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddFriendsActivity.class);
                startActivity(intent);
            }
        });

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
        int id = item.getItemId();
        if (id == R.id.add_event) {

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();


        if (ParseUser.getCurrentUser() != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
            currentUser = ParseUser.getCurrentUser();
            friendsRelation = currentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

            ParseQuery<ParseUser> query = friendsRelation.getQuery();
            query.orderByAscending(ParseConstants.KEY_USERNAME);
            query.setLimit(1000);
        /*FIXME: add a loader for this*/
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> users, ParseException e) {
                    if (e == null) {
                    /* success */
                        mUsers = users;
                        String[] usernames = new String[mUsers.size()];
                        int i = 0;
                        for (ParseUser user : mUsers) {
                            usernames[i] = user.getUsername();
                            i++;
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                getContext(), android.R.layout.simple_list_item_1,
                                usernames);
                        setListAdapter(adapter);

                    } else {
                    /* error */
                        Log.e(TAG, e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(e.getMessage())
                                .setTitle("error")
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    ;
                }

                ;
            });
        }
    }
}