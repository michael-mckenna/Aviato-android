package acc.aviato;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {
    public static String TAG = CreateEventActivity.class.getSimpleName();

    //UI References
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;

    private SimpleDateFormat dateFormatter;

    int mYear, mMonthOfYear, mDayOfMonth;

    Calendar Date = Calendar.getInstance();

    String mCurrentPhotoPath;
    ImageView mImageView;
    EditText mEditText;
    Bitmap mBitmap;
    String[] tagArray;

    boolean mSubmitted = false;

    TextView mLocationText;
    Button mEventLocationButton;

    ParseObject mEventObject;
    ParseGeoPoint mGeoPoint;

    final int REQUEST_IMAGE_CAPTURE = 1;
    final int REQUEST_IMAGE_PICK = 2;
    final int PLACE_PICKER_REQUEST = 3;

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        mLocationText = (TextView) findViewById(R.id.location_text);
        mEditText = (EditText) findViewById(R.id.tagsInput);
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (mEditText.getText().toString().equals("")) {
                        mEditText.setText("#");
                    }
                } else if (mEditText.getText().toString().equals("#")) {
                    mEditText.setText("");
                }
            }
        });


        dateFormatter = new SimpleDateFormat(getString(R.string.month_date_format), Locale.US);
        findViewsById();
        setDateTimeField();

        mEventLocationButton = (Button) findViewById(R.id.location_button);
        final PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        mEventLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(builder.build(CreateEventActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        mImageView = (ImageView) findViewById(R.id.eventImage);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Add functionality of showImageOptions() so that a user can modify the photo
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void findViewsById() {
        btn = (Button) findViewById(R.id.eventDateInput);
        btn.setText(dateFormatter.format(Date.getTime()));
    }

    private void setDateTimeField() {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.show();
                mTimePickerDialog.show();

                mEditText = (EditText) findViewById(R.id.tagsInput);
                mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            if (mEditText.getText().toString().equals("")) {
                                mEditText.setText("#");
                            }
                        } else {
                            if (mEditText.getText().toString().equals("#")) {
                                mEditText.setText("");
                            }
                        }
                    }
                });
            }
        });

        Calendar newCalendar = Calendar.getInstance();
        mDatePickerDialog = new DatePickerDialog(CreateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonthOfYear = monthOfYear;
                mDayOfMonth = dayOfMonth;
                Date.set(year, monthOfYear, dayOfMonth);
                btn.setText(dateFormatter.format(Date.getTime()));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        mTimePickerDialog = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Date.set(mYear, mMonthOfYear, mDayOfMonth, hourOfDay, minute);
            }
        }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), false);
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
        EditText descriptionInput;

        switch (item.getItemId()) {
            case R.id.submit_event:
                eventInput = (EditText) findViewById(R.id.eventInput);
                tagsInput = (EditText) findViewById(R.id.tagsInput);
                descriptionInput = (EditText) findViewById(R.id.descriptionInput);

                String event = eventInput.getText().toString().trim();
                Date date = Date.getTime();
                String tag = tagsInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                int vote = 0;

                event = event.trim();
                tag = tag.trim();
                description = description.trim();

                if (ParseUser.getCurrentUser() == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.error_sign_in))
                            .setTitle(getString(R.string.error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else if (event.isEmpty() || tag.isEmpty() || description.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.error_name_tag))
                            .setTitle(getString(R.string.error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else if (description.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.error_description))
                            .setTitle(getString(R.string.error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else if (!validateTags()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.error_invalid_tags))
                            .setTitle(getString(R.string.error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else if (mGeoPoint == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.error_location))
                            .setTitle(getString(R.string.error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else if (!mSubmitted) {
                    mSubmitted = true;
                    mEventObject = new ParseObject(ParseConstants.CLASS_EVENTS);
                    mEventObject.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
                    mEventObject.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
                    mEventObject.put(ParseConstants.KEY_EVENT_NAME, event);
                    for (String s : tagArray) {
                        mEventObject.add(ParseConstants.KEY_EVENT_TAG_ID, getTagId(s));
                    }
                    mEventObject.put(ParseConstants.KEY_EVENT_VOTES, vote);
                    if (date != null) {
                        mEventObject.put(ParseConstants.KEY_EVENT_DATE_TIME, date);
                    }
                    mEventObject.put(ParseConstants.KEY_EVENT_DESCRIPTION, description);
                    mEventObject.put(ParseConstants.KEY_EVENT_VOTES, vote);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    if (mBitmap != null) {
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] image = stream.toByteArray();
                        ParseFile imageFile = new ParseFile("event_image.png", image);
                        imageFile.saveInBackground();
                        mEventObject.put(ParseConstants.KEY_EVENT_IMAGE, imageFile);
                    }

                    mEventObject.put(ParseConstants.KEY_EVENT_LOCATION, mGeoPoint);


                    mEventObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(CreateEventActivity.this, getString(R.string.success_uploaded), Toast.LENGTH_SHORT).show();
                                Intent returnIntent = new Intent();
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
                                builder.setMessage(e.getMessage() + "")
                                        .setTitle(getString(R.string.error_title))
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });

                    CreateEventActivity.this.finish();
                }
                return true;
            case R.id.upload_photo:
                showImageOptions();
                return true;
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showImageOptions() {
        // TODO: Modify this so that a delete option will be shown if mImageView exists
        final CharSequence[] items = {getString(R.string.photo_take), getString(R.string.photo_gallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.photo_title_add))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePicture.resolveActivity(getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
                                    builder.setMessage("Error: " + ex.getMessage() + ".")
                                            .setTitle(getString(R.string.error_title_photo_upload))
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                                if (photoFile != null) {
                                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(photoFile));
                                    startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
                                }
                            }
                        } else {
                            Intent pickPicture = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPicture, REQUEST_IMAGE_PICK);
                        }
                    }
                });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                galleryAddPic();
                shrinkPic();
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT < 19) {
                    mCurrentPhotoPath = getPath(selectedImageUri);
                    shrinkPic();
                } else {
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                        int targetW = mImageView.getWidth();
                        int targetH = mImageView.getHeight();

                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bmOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
                        int photoW = bmOptions.outWidth;
                        int photoH = bmOptions.outHeight;

                        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

                        bmOptions.inJustDecodeBounds = false;
                        bmOptions.inSampleSize = scaleFactor;
                        // This code is ignored on Lollipop devices
                        bmOptions.inPurgeable = true;

                        mBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
                        parcelFileDescriptor.close();
                        mImageView.setImageBitmap(mBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == PLACE_PICKER_REQUEST) {
                Place place = PlacePicker.getPlace(data, this);
                LatLng latLng = place.getLatLng();
                mGeoPoint = new ParseGeoPoint(latLng.latitude, latLng.longitude);
                if (place.getName() != null) {
                    mLocationText.setText(place.getName());
                } else if (place.getAddress() != null) {
                    mLocationText.setText(place.getAddress());
                } else {
                    mLocationText.setText(latLng.latitude + ", " + latLng.longitude);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String appName = getApplicationContext().getString(getApplicationContext()
                .getApplicationInfo().labelRes);
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + "/" + appName + "/");
        if (!storageDir.isDirectory()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void shrinkPic() {
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        // This code is ignored on Lollipop devices
        bmOptions.inPurgeable = true;

        mBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(mBitmap);
    }

    private String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    private int getTagId(String tag) {
        ParseQuery<ParseObject> tagList = new ParseQuery<ParseObject>(ParseConstants.CLASS_TAGS);
        tagList.whereEqualTo(ParseConstants.KEY_TAG_NAME, tag);
        List<ParseObject> list = null;
        try {
            list = tagList.find();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        if (list.isEmpty()) {
            //create new tag object
            ParseQuery<ParseObject> tagList2 = new ParseQuery<ParseObject>(ParseConstants.CLASS_TAGS);
            tagList2.addDescendingOrder(ParseConstants.KEY_TAG_ID);
            int newId;
            try {
                newId = Integer.parseInt(tagList2.find().get(0).get(ParseConstants.KEY_TAG_ID).toString()) + 1;
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }
            ParseObject obj = new ParseObject(ParseConstants.CLASS_TAGS);
            obj.put(ParseConstants.KEY_TAG_ID, newId);
            obj.put(ParseConstants.KEY_TAG_USAGE, 1);
            obj.put(ParseConstants.KEY_TAG_NAME, tag);
            try {
                obj.save();
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }
            return newId;
        } else {
            //use old tag object, add 1 to usage count
            int id = Integer.parseInt(list.get(0).get(ParseConstants.KEY_TAG_ID).toString());
            list.get(0).increment(ParseConstants.KEY_TAG_USAGE);
            list.get(0).saveInBackground();
            return id;
        }
    }

    private boolean validateTags() {
        String reg = "^#[a-zA-Z0-9]+$";
        //In the future, will want to split string by spaces and check each
        tagArray = mEditText.getText().toString().split(" ");
        for (String s : tagArray) {
            if (!s.matches(reg)) {
                return false;
            }
        }
        return true;
    }

}
