package acc.aviato;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class EventDetailActivity extends AppCompatActivity {

    TextView eventName, eventDescription;
    ImageView eventImage;
    private static String TAG = EventDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        Intent intent = getIntent();

        Uri imageUri = intent.getData();

        eventName = (TextView)findViewById(R.id.event_name);
        eventName.setText(intent.getStringExtra("EVENT_NAME"));

        eventDescription = (TextView)findViewById(R.id.eventDescription);
        eventDescription.setText(intent.getStringExtra("EVENT_DESCRIPTION"));

        eventImage = (ImageView)findViewById(R.id.event_image);
        if(imageUri != null) {
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
