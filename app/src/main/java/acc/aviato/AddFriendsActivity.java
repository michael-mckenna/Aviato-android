package acc.aviato;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

public class AddFriendsActivity extends AppCompatActivity {

    public static final String TAG = "AddFriendsActivity";
    ParseRelation friendsRelation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        friendsRelation = ParseUser.getCurrentUser().getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        final EditText usernameInput = (EditText) findViewById(R.id.usernameField);
        Button addButton = (Button) findViewById(R.id.buttonAddFriend);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String requested_username = usernameInput.getText().toString();
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo(ParseConstants.KEY_USERNAME, requested_username);
                query.setLimit(1);
                Log.i(TAG, "Searching for user: " + requested_username);
                /*FIXME: put a loader in here*/
                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> list, ParseException e) {
                        if (e == null && list.size() > 0) {
                            Log.i(TAG, "Found User: " + requested_username);
                            Log.i(TAG, "Found User: " + list.get(0).getUsername());
                            friendsRelation.add(list.get(0));
                            ParseUser.getCurrentUser().saveEventually();
                            AddFriendsActivity.this.finish();
                            try {
                                ParseUser.getCurrentUser().save();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            Log.i(TAG, "Did not find user: " + requested_username);
                        }
                    }
                });

            }
        });

    }

}
