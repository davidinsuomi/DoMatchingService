package ee.ut.cs.domatching;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by weiding on 02/04/15.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // create server
        Intent intent = new Intent(this,CoapBackgroundService.class);
        startService(intent);
    }
}
