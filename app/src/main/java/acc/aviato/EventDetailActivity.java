package acc.aviato;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

public class EventDetailActivity extends AppCompatActivity implements View.OnTouchListener {

    TextView mEventNameView, mEventDescriptionView, mEventAddressView, mEventDateTimeView;
    ImageView mEventImageView;
    ViewGroup mEventDetails;

    ParseObject mEvent;
    String eventId, eventName, eventDescription;
    Uri eventImageUri;
    Menu mMenu;

    private int yDelta, yLast;
    private double mLatitude, mLongitude;
    private boolean locked;

    ParseRelation mFavoriteEventRelation;

    Intent mIntent;

    private static String TAG = EventDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        mIntent = getIntent();

        eventImageUri = mIntent.getData();

        mEventNameView = (TextView) findViewById(R.id.detail_event_name);
        mEventNameView.setText(mIntent.getStringExtra("EVENT_NAME"));

        mEventDescriptionView = (TextView) findViewById(R.id.event_description);
        mEventDescriptionView.setText(mIntent.getStringExtra("EVENT_DESCRIPTION"));

        mEventAddressView = (TextView) findViewById(R.id.detail_address);
        mEventAddressView.setText(mIntent.getStringExtra("EVENT_ADDRESS"));
        final double latitude = mIntent.getDoubleExtra("EVENT_LATITUDE", 0);
        final double longitude =  mIntent.getDoubleExtra("EVENT_LONGITUDE", 0);
        mEventAddressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:" + latitude + longitude + "?q=" + mIntent.getStringExtra("EVENT_ADDRESS")));
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        mEventDateTimeView = (TextView) findViewById(R.id.detail_date_time);
        mEventDateTimeView.setText(mIntent.getStringExtra("EVENT_DATE_TIME"));

        eventId = mIntent.getStringExtra("EVENT_ID");

        mEventImageView = (ImageView) findViewById(R.id.detail_image);
        mEventDetails = (ViewGroup) findViewById(R.id.layout_event_details);

        // TODO: Remove uses of mIntent in favor of direct use of Parse queries
        // Look for an event object with the name of the event id that was passed
        ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery(ParseConstants.CLASS_EVENTS);
        eventQuery.getInBackground(eventId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                // Event found
                if (e == null) {
                    mEvent = parseObject;
                }
            }
        });
        if (eventImageUri != null) {
            if (mEventDetails.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mEventDetails.getLayoutParams();
                p.setMargins(0, dipToPixels(getApplicationContext(), 140), 0, 0);
                mEventDetails.requestLayout();
            }
            Picasso.with(getApplicationContext()).load(eventImageUri.toString()).into(mEventImageView);
            mEventDetails.setOnTouchListener(this);
        } else {
            Log.d(TAG, "Image was null");
        }
    }

    public static int dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_eventdetailactivity, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite_event:
                // Get the user's favoriteEvents, if the current event is in the relation, remove it
                // Otherwise, add it
                if (ParseUser.getCurrentUser() != null) {
                    mFavoriteEventRelation = ParseUser.getCurrentUser().getRelation(ParseConstants.KEY_FAVORITE_EVENTS_REALATION);
                    final ParseQuery<ParseObject> query = mFavoriteEventRelation.getQuery();  // Returns objects in relation
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            // If the list of favorite events has the specific event in it, unfavorite it.
                            if (e == null && objects.contains(mEvent)) {
                                mFavoriteEventRelation.remove(mEvent);
                                ParseUser.getCurrentUser().saveEventually();
                                try {
                                    ParseUser.getCurrentUser().save();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                                mMenu.findItem(R.id.favorite_event).setIcon(android.R.drawable.star_big_off);
                                // If the list does not contain the event, favorite it.
                            } else if (e == null && !objects.contains(mEvent)) {
                                mFavoriteEventRelation.add(mEvent);
                                ParseUser.getCurrentUser().saveEventually();
                                try {
                                    ParseUser.getCurrentUser().save();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                                mMenu.findItem(R.id.favorite_event).setIcon(android.R.drawable.star_big_on);
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
                                event.unpinInBackground("FavoritedEvents");
                            } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                mEvent.pinInBackground("FavoritedEvents");
                            } else {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });

                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams downParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                yDelta = Y - downParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                RelativeLayout.LayoutParams upParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                if (upParams.topMargin < 0) {
                    upParams.topMargin = 0;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams moveParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                if (moveParams.topMargin >= 0 && !locked) {
                    moveParams.topMargin = Y - yDelta;
                } else {
                    locked = true;
                    moveParams.topMargin = 0;
                    if (Y > yLast && Y - yDelta >= 0) {
                        locked = false;
                    }
                }
                moveParams.bottomMargin = -100;
                view.setLayoutParams(moveParams);
                break;
        }
        yLast = Y;
        view.getRootView().invalidate();
        return true;
    }
}
