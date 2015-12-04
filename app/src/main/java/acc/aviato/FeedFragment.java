package acc.aviato;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AutoCompleteTextView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;

public class FeedFragment extends ListFragment {

    public static final String ARG_SECTION_NUMBER = "section_number";
    public static final String TAG = FeedFragment.class.getSimpleName();
    private int[] voteArray;

    private List<ParseObject> parseEvents;
    private FeedAdapter adapt;

    private LayoutInflater inflater;

    private ArrayList<Integer> filters = new ArrayList<>();
    //private int filters = -1;

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

        refreshEvents();

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
        inflater.inflate(R.menu.menu_feedactivity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
            case R.id.filter_events:
                createFilterDialog(); return true;
            case R.id.remove_filters:
                filters = new ArrayList<>(); refreshEvents(); return true;
        }
    }

    public void createFilterDialog(){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.content_filter_dialog,null);
        // Add the buttons
        builder.setPositiveButton(R.string.filter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                AutoCompleteTextView ta = (AutoCompleteTextView)view.findViewById(R.id.filter_autocomplete);
                String tag = ta.getText().toString();
                int tid = tagExists(tag);
                if(tid==-1)
                {
                    //You screwed up
                    return;
                }
                filters.add(tid);
                refreshEvents();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Set other dialog properties
        builder.setTitle("Filter Events");
        builder.setView(view);

        final android.support.v7.app.AlertDialog dialog = builder.create();


        ParseQuery<ParseObject> comp = new ParseQuery<ParseObject>(ParseConstants.CLASS_TAGS);
        comp.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    //If I weren't lazy I would make a sorting algorithm here to sort this list by tag popularity, but I'm not even sure that would make a difference so I won't do it for now
                    String[] tagFilters = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        tagFilters[i]=list.get(i).get(ParseConstants.KEY_TAG_NAME).toString();
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line,tagFilters);
                    AutoCompleteTextView textView = (AutoCompleteTextView)dialog.findViewById(R.id.filter_autocomplete);
                    textView.setAdapter(adapter);

                }
            }
        });

        // Create the AlertDialog

        dialog.show();
    }

    public int tagExists(String s){
        ParseQuery<ParseObject> q = new ParseQuery<ParseObject>(ParseConstants.CLASS_TAGS);
        q.whereEqualTo(ParseConstants.KEY_TAG_NAME, s);
        List<ParseObject> list = null;
        try {
            list = q.find();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        if(list.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(list.get(0).get(ParseConstants.KEY_TAG_ID).toString());
    }

    public void refreshEvents(){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_EVENTS);
        query.whereExists(ParseConstants.KEY_EVENT_NAME);
        if(!filters.isEmpty()) {
            query.whereContainsAll(ParseConstants.KEY_EVENT_TAG_ID, filters);
        }
        query.addDescendingOrder(ParseConstants.KEY_EVENT_VOTES);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    parseEvents = list;
                    voteArray = new int[parseEvents.size()];
                    for (int i = 0; i < parseEvents.size(); i++) {
                        voteArray[i] = parseEvents.get(i).getInt(ParseConstants.KEY_EVENT_VOTES);
                    }
                    System.out.println(list.size() + " LIST SIZE"); // this equals 69! so that's working...
                    if (getListView().getAdapter() == null) {
                        adapt = new FeedAdapter(
                                getListView().getContext(),
                                parseEvents);
                        setListAdapter(adapt);
                    } else {
                        //refill adapter here
                        adapt = new FeedAdapter(
                                getListView().getContext(),
                                parseEvents);
                        setListAdapter(adapt);
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
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