package acc.aviato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class FeedFragment extends ListFragment {

    public static final String ARG_SECTION_NUMBER = "section_number";
    public static final String TAG = FeedFragment.class.getSimpleName();

    private List<ParseObject> parseEvents;

    public FeedFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_feed_activity, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_feed);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser() == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("You must be signed in to create an event.")
                            .setTitle("Not signed in")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Intent intent = new Intent(getActivity(), CreateEventActivity.class);
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_EVENTS);
        query.whereExists(ParseConstants.KEY_EVENT_NAME);
        query.addAscendingOrder(ParseConstants.KEY_EVENT_VOTES);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null) {
                    parseEvents = list;
                    System.out.println(list.size() + " LIST SIZE"); // this equals 69! so that's working...
                    if(getListView().getAdapter() == null) {
                        FeedAdapter adapter = new FeedAdapter(
                                getListView().getContext(),
                                parseEvents);
                        setListAdapter(adapter);
                    } else {
                        //refill adapter here
                    }
                }
            }
        });

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

    public class FeedAdapter extends ArrayAdapter<ParseObject> {

        private LayoutInflater inflater;
        private List<ParseObject> mEvents;
        private Context mContext;

        public FeedAdapter(Context context, List<ParseObject> events) {
            super(context, R.layout.list_item_event, events);
            inflater = LayoutInflater.from(context);
            mEvents = events;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mEvents.size();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_event, null);
                holder = new ViewHolder();
                holder.event = (TextView)convertView.findViewById(R.id.eventItemText);
                holder.date = (TextView)convertView.findViewById(R.id.dateItemText);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.event.setText(mEvents.get(position).getString(ParseConstants.KEY_EVENT_NAME));
            //holder.date.setText(mEvents.get(position).getString(ParseConstants.KEY_EVENT_DATE))

            return convertView;
        }

        public class ViewHolder {
            TextView event, date;
        }

    }

}