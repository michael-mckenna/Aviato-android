package acc.aviato;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Michael on 11/14/15.
 */
public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "DbvGqfHzV81S1p7iY62FOpsuQZmQgHoguXf1425D", "8Ozdvy2M7eqdfSvVBBo17I6fDXy4Ly6KwvCIxNj7");
    }
}
