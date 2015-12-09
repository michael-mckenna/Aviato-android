package acc.aviato;

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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    TextView mEventNameView, mEventDescriptionView;
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
        eventId = mIntent.getStringExtra(ParseConstants.KEY_EVENT_ID);

        // Look for an event object with the name of the event id that was passed
        ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery(ParseConstants.CLASS_EVENTS);
        eventQuery.getInBackground(eventId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    mEvent = parseObject;
                    eventName = mEvent.getString(ParseConstants.KEY_EVENT_NAME);
                    eventDescription = mEvent.getString(ParseConstants.KEY_EVENT_DESCRIPTION);
                    mEventNameView = (TextView) findViewById(R.id.event_name);
                    mEventNameView.setText(eventName);

                    mEventDescriptionView = (TextView) findViewById(R.id.event_description);
                    mEventDescriptionView.setText(eventDescription);
                    if (mEvent.getParseFile(ParseConstants.KEY_EVENT_IMAGE) != null) {
                        eventImageUri = Uri.parse(parseObject.getParseFile(ParseConstants.KEY_EVENT_IMAGE).getUrl());
                    }
                } else {
                    // Failure (cannot find requested event of FeedFragment's listView)
                }
            }
        });

        // TODO: Remove uses of mIntent in favor of direct use of Parse queries

        mFavoriteButton = (Button) findViewById(R.id.favorite_button);
        // Get the user's favoriteEvents, if the current event is in the relation, remove it
        // Otherwise, add it
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFavoriteEventRelation = ParseUser.getCurrentUser().getRelation(ParseConstants.KEY_FAVORITE_EVENTS_REALATION);
                final ParseQuery<ParseObject> query = mFavoriteEventRelation.getQuery();  // Returns objects in relation
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            // If the list of favorite events has the specific event in it...
                            if (e == null && objects.contains(mEvent)) {
                                // TODO: Add logic to remove a favorite if it's already an existing relation
                                mFavoriteEventRelation.remove(mEvent);
                                ParseUser.getCurrentUser().saveEventually();
                                try {
                                    ParseUser.getCurrentUser().save();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                                Log.i(TAG, "Event unfavorited!");
                                // If the list does not contain the event...
                            } else if (e == null && !objects.contains(mEvent)) {
                                mFavoriteEventRelation.add(mEvent);
                                ParseUser.getCurrentUser().saveEventually();
                                try {
                                    ParseUser.getCurrentUser().save();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                                Log.i(TAG, "Event favorited!");
                            } else {
                                Log.i(TAG, "Error in favorite process.");
                            }
                        }
                    });
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
