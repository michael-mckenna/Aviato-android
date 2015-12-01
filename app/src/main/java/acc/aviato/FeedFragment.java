package acc.aviato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class FeedFragment extends ListFragment {

    public static final String ARG_SECTION_NUMBER = "section_number";
    public static final String TAG = FeedFragment.class.getSimpleName();
    private int[] voteArray;

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
        query.addDescendingOrder(ParseConstants.KEY_EVENT_VOTES);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null) {
                    parseEvents = list;
                    voteArray = new int[parseEvents.size()];
                    for(int i = 0; i < parseEvents.size(); i++) {
                        voteArray[i] = parseEvents.get(i).getInt(ParseConstants.KEY_EVENT_VOTES);
                    }
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent  = new Intent(getActivity(), EventDetailActivity.class);
        if(parseEvents.get(position).getParseFile(ParseConstants.KEY_EVENT_IMAGE) != null) {
            ParseFile file = parseEvents.get(position).getParseFile(ParseConstants.KEY_EVENT_IMAGE);
            Uri fileUri = Uri.parse(file.getUrl());
            intent.setData(fileUri);
        } else {
            Log.d(TAG, "Null image found");
        }
        intent.putExtra("EVENT_NAME", parseEvents.get(position).getString(ParseConstants.KEY_EVENT_NAME));
        intent.putExtra("EVENT_DESCRIPTION", parseEvents.get(position).getString(ParseConstants.KEY_EVENT_DESCRIPTION));
        //needs address
        intent.putExtra("EVENT_TAG", parseEvents.get(position).getString(ParseConstants.KEY_EVENT_TAG));
        startActivity(intent);
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_event, null);
                holder = new ViewHolder();
                holder.event = (TextView)convertView.findViewById(R.id.eventItemText);
                holder.date = (TextView)convertView.findViewById(R.id.dateItemText);
                holder.downArrow = (ImageView)convertView.findViewById(R.id.downArrow);
                holder.votes = (TextView)convertView.findViewById(R.id.votes);
                holder.upArrow = (ImageView)convertView.findViewById(R.id.upArrow);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.votes.setText(mEvents.get(position).getInt(ParseConstants.KEY_EVENT_VOTES) + "");
            holder.event.setText(mEvents.get(position).getString(ParseConstants.KEY_EVENT_NAME));
            //holder.date.setText(mEvents.get(position).getString(ParseConstants.KEY_EVENT_DATE))

            holder.downArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    voteArray[position] -= 1;
                    mEvents.get(position).put(ParseConstants.KEY_EVENT_VOTES, voteArray[position]);
                    mEvents.get(position).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                Log.d(TAG, "SUCCESSFULLY VOTED");
                                notifyDataSetChanged();
                            } else {
                                Toast.makeText(getActivity(), "Can't vote while offline!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            holder.upArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    voteArray[position] += 1;
                    mEvents.get(position).put(ParseConstants.KEY_EVENT_VOTES, voteArray[position]);
                    mEvents.get(position).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                Log.d(TAG, "SUCCESSFULLY VOTED");
                                notifyDataSetChanged();
                            } else {
                                Toast.makeText(getActivity(), "Can't vote while offline!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            return convertView;
        }

        public class ViewHolder {
            TextView event, date, votes;
            ImageView upArrow, downArrow;
        }

    }

}