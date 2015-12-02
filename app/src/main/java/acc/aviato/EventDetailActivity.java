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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    TextView eventName, eventDescription;
    ImageView eventImage;

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

        eventName = (TextView) findViewById(R.id.event_name);
        eventName.setText(mIntent.getStringExtra("EVENT_NAME"));

        eventDescription = (TextView) findViewById(R.id.eventDescription);
        eventDescription.setText(mIntent.getStringExtra("EVENT_DESCRIPTION"));

        mFavoriteButton = (Button) findViewById(R.id.favorite_button);
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Modify this so that it searches for an event ID instead of name to avoid conflicts
                mFavoriteEventRelation = ParseUser.getCurrentUser().getRelation(ParseConstants.KEY_FAVORITE_EVENTS_REALATION);
                ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_EVENTS);
                query.whereEqualTo(ParseConstants.KEY_EVENT_NAME, mIntent.getStringExtra("EVENT_NAME"));
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e == null) {
                            // TODO: Add logic to remove a favorite if it's already an existing relation
                            mFavoriteEventRelation.add(list.get(0));
                            ParseUser.getCurrentUser().saveEventually();
                            try {
                                ParseUser.getCurrentUser().save();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            Log.i(TAG, "Worked!");
                        } else {
                            Log.i(TAG, "Did not find event.");
                        }
                    }
                });
            }
        });

        eventImage = (ImageView) findViewById(R.id.event_image);
        if (imageUri != null) {
            Picasso.with(this).load(imageUri.toString()).into(eventImage);
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
