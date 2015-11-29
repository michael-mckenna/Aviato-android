package acc.aviato;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.w3c.dom.Text;

import java.util.List;

public class FeedAdapter extends ArrayAdapter<ParseObject> {

    public FeedAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public FeedAdapter(Context context, int resource, List<ParseObject> items) {
        super(context, resource, items);
    }

    // Used to inflate the layout for each row
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(getContext());
            v = layoutInflater.inflate(R.layout.row_event_list, null);
        }

        ParseObject event = getItem(position);

        if (event != null) {
            TextView eventName = (TextView) v.findViewById(R.id.eventName);
            TextView eventDate = (TextView) v.findViewById(R.id.eventDate);
            TextView eventCreator = (TextView) v.findViewById(R.id.eventCreator);
            TextView eventDescription = (TextView) v.findViewById(R.id.eventDescription);
            ImageView eventImage = (ImageView) v.findViewById(R.id.eventImage);

            if (eventName != null) {
                eventName.setText(event.getString("eventName"));
            }

            if (eventCreator != null) {
                eventCreator.setText(event.getString("senderName"));
            }

            if (eventDate != null) {
                eventDate.setText(event.getString("eventDate"));
            }

            if (eventDescription != null) {
                eventDescription.setText(event.getString("eventDescription"));
            }

            if (eventImage != null) {
                ParseFile eventImageFile = event.getParseFile("eventImage");
                loadImages(eventImageFile, eventImage);
            }
        }
        return v;
    }

    private void loadImages(ParseFile thumbnail, final ImageView img) {

        if (thumbnail != null) {
            thumbnail.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        img.setImageBitmap(bmp);
                    } else {
                    }
                }
            });
        } else {
            img.setImageResource(R.drawable.splash);
        }
    }
}
