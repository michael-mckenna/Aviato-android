package acc.aviato;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateEventActivity extends AppCompatActivity {

    String mCurrentPhotoPath;
    ImageView mImageView;

    final int REQUEST_IMAGE_CAPTURE = 1;
    final int REQUEST_IMAGE_PICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        mImageView = (ImageView) findViewById(R.id.eventImage);
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

        switch (item.getItemId()) {
            case R.id.submit_event:
                eventInput = (EditText) findViewById(R.id.eventInput);
                tagsInput = (EditText) findViewById(R.id.tagsInput);

                String event = eventInput.getText().toString().trim();
                String tag = tagsInput.getText().toString().trim();
                int vote = 0;

                event = event.trim();
                tag = tag.trim();
                if (ParseUser.getCurrentUser() == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("You must be signed in to create an event.")
                            .setTitle("Error")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else if (event.isEmpty() || tag.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("You must include a name and a tag.")
                            .setTitle("Error")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    ParseObject eventObject = new ParseObject(ParseConstants.CLASS_EVENTS);
                    eventObject.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
                    eventObject.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
                    eventObject.put(ParseConstants.KEY_EVENT_NAME, event);
                    eventObject.put(ParseConstants.KEY_EVENT_TAG, tag);
                    eventObject.put(ParseConstants.KEY_EVENT_VOTES, vote);
                    eventObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(CreateEventActivity.this, "Event Uploaded", Toast.LENGTH_SHORT).show();
                                CreateEventActivity.this.finish();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
                                builder.setMessage(e.getMessage() + "")
                                        .setTitle("Error")
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });

                }
                return true;
            case R.id.upload_photo:
                final CharSequence[] items = {"Take Photo", "Choose from Library"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Add Photo")
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
                                                    .setTitle("Error uploading photo")
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
                // TODO: Handle new photos: shrink image, upload to Parse, create ImageView for this activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("FILE PATH: " + mCurrentPhotoPath);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                galleryAddPic();
                setPic();
            } else {
                return;
            }
        } else if (resultCode == REQUEST_IMAGE_PICK) {
            if (resultCode == RESULT_OK) {
                mImageView = (ImageView) findViewById(R.id.eventImage);
                Bitmap image = BitmapFactory.decodeFile(mCurrentPhotoPath);
                mImageView.setImageBitmap(image);
            } else {
                return;
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

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        // bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }
}
