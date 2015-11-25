package acc.aviato;

import android.app.AlertDialog;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class CreateEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, CreateEventFragment.newInstance(getIntent().getExtras()))
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_createactivity, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        EditText eventInput;
        EditText tagsInput;
        ViewGroup container;

        switch (item.getItemId()) {
            case R.id.submit_event:
                // TODO: Have a way of handling a null return from getApplicationContext

                eventInput = (EditText) findViewById(R.id.eventInput);
                tagsInput = (EditText) findViewById(R.id.tagsInput);

                String event = eventInput.getText().toString().trim();
                String tag = tagsInput.getText().toString().trim();
                int vote = 0;

                event = event.trim();
                tag = tag.trim();
                // TODO: Task#35 - Disallow an event to be created if a user is not signed in
                // Example: if (ParseUser.getCurrentUser() == null) { A user is not signed in! }
                if (event.isEmpty() || tag.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("You must at least include a name and a tag!")
                            .setTitle("Error")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    System.out.println(event);
                    ParseObject eventObject = new ParseObject(ParseConstants.CLASS_EVENTS);
                    eventObject.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
                    eventObject.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
                    eventObject.put(ParseConstants.KEY_EVENT_NAME, event);
                    eventObject.put(ParseConstants.KEY_EVENT_TAG, tag);
                    eventObject.put(ParseConstants.KEY_EVENT_VOTES, vote);
                    eventObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(CreateEventActivity.this, "Event Uploaded", Toast.LENGTH_SHORT).show();
                                CreateEventActivity.this.finish();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
                                builder.setMessage(e.getMessage() + "")
                                        .setTitle("Error")
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });

                }
                return true;
            case R.id.upload_photo:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class CreateEventFragment extends Fragment {

        // static final int PICK_IMAGE_REQUEST = 1;

        public CreateEventFragment() {
            setHasOptionsMenu(true);
        }

        public static CreateEventFragment newInstance(Bundle bundle) {
            CreateEventFragment fragment = new CreateEventFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View eventView = inflater.inflate(R.layout.fragment_create_event, container, false);
            return eventView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.clear();
            inflater.inflate(R.menu.menu_createactivity, menu);
        }

    }
}
