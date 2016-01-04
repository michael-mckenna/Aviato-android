package acc.aviato;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    TextView mEventNameView, mEventDescriptionView, mEventAddressView;
    ImageView mEventImageView;

    ParseObject mEvent;
    String eventId, eventName, eventDescription;
    Uri eventImageUri;

    Button mFavoriteButton;
    ParseRelation mFavoriteEventRelation;

    Intent mIntent;

    private static String TAG = EventDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        mIntent = getIntent();

        Uri imageUri = mIntent.getData();

        mEventNameView = (TextView) findViewById(R.id.event_name);
        mEventNameView.setText(mIntent.getStringExtra("EVENT_NAME"));

        mEventDescriptionView = (TextView) findViewById(R.id.event_description);
        mEventDescriptionView.setText(mIntent.getStringExtra("EVENT_DESCRIPTION"));

        mEventAddressView = (TextView) findViewById(R.id.address);
        mEventAddressView.setText("(" + mIntent.getDoubleExtra("EVENT_LATITUDE", 0) + ", " + mIntent.getDoubleExtra("EVENT_LONGITUDE", 0) + ")");

        eventId = mIntent.getStringExtra("EVENT_ID");

        mEventImageView = (ImageView) findViewById(R.id.event_image);
        if (imageUri != null) {
            Picasso.with(this).load(imageUri.toString()).into(mEventImageView);
        } else {
            Log.d(TAG, "Image was null");
        }

        // TODO: Remove uses of mIntent in favor of direct use of Parse queries
        // Look for an event object with the name of the event id that was passed
        ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery(ParseConstants.CLASS_EVENTS);
        eventQuery.getInBackground(eventId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                // Event found
                if (e == null) {
                    mEvent = parseObject;
                } else {
                    // Failure (cannot find requested event of FeedFragment's listView)
                }
            }
        });

        mFavoriteButton = (Button) findViewById(R.id.favorite_button);
        // Get the user's favoriteEvents, if the current event is in the relation, remove it
        // Otherwise, add it
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser() != null) {
                    mFavoriteEventRelation = ParseUser.getCurrentUser().getRelation(ParseConstants.KEY_FAVORITE_EVENTS_REALATION);
                    final ParseQuery<ParseObject> query = mFavoriteEventRelation.getQuery();  // Returns objects in relation
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            // If the list of favorite events has the specific event in it, favorite it.
                            if (e == null && objects.contains(mEvent)) {
                                mFavoriteEventRelation.remove(mEvent);
                                ParseUser.getCurrentUser().saveEventually();
                                try {
                                    ParseUser.getCurrentUser().save();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                                // If the list does not contain the event, unfavorite it.
                            } else if (e == null && !objects.contains(mEvent)) {
                                mFavoriteEventRelation.add(mEvent);
                                ParseUser.getCurrentUser().saveEventually();
                                try {
                                    ParseUser.getCurrentUser().save();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                            } else {
                                // Error
                            }
                        }
                    });
                } else {
                    ParseQuery<ParseObject> favoriteQuery = ParseQuery.getQuery(ParseConstants.CLASS_EVENTS)
                            .fromLocalDatastore();

                    favoriteQuery.getInBackground(eventId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject event, ParseException e) {
                            if (e == null) {
                                System.out.println("Unpined");
                                event.unpinInBackground("FavoritedEvents");
                            } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                System.out.println("Pinned");
                                mEvent.pinInBackground("FavoritedEvents");
                            } else {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });

                }
            }
        });

        mEventImageView = (ImageView) findViewById(R.id.event_image);
        if (eventImageUri != null) {
            Picasso.with(this).load(eventImageUri.toString()).into(mEventImageView);
        } else {
            Log.d(TAG, "Image was null");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
