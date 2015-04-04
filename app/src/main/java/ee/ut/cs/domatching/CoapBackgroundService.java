package ee.ut.cs.domatching;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.net.SocketException;

import endpoint.Endpoint;

/**
 * Created by weiding on 30/03/15.
 */
public class CoapBackgroundService extends IntentService implements  CoapDebugInfo{
    public CoapBackgroundService(){
        super("CoapBackgroundService");
    }
    private final String TAG = "CoapBackgroundService";
    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            Endpoint server = new CoapServer(this);
            Log.e("coapService", "SampleServer listening at port %d." + server.port());

        } catch (SocketException e) {

            Log.e("coapService", "Failed to create SampleServer: %s" + e.getMessage());
        }
    }

    @Override
    public void printDebugInfo(String outPut) {
        Log.e(TAG,outPut);
    }
}

