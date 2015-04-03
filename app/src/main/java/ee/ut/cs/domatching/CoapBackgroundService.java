package ee.ut.cs.domatching;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.net.SocketException;

import endpoint.Endpoint;

/**
 * Created by weiding on 30/03/15.
 */
public class CoapBackgroundService extends IntentService{
    public CoapBackgroundService(){
        super("CoapBackgroundService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            Endpoint server = new CoapServer();
            Log.e("coapService", "SampleServer listening at port %d." + server.port());

        } catch (SocketException e) {

            Log.e("coapService", "Failed to create SampleServer: %s" + e.getMessage());
        }
    }
}

