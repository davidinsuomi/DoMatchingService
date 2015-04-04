package ee.ut.cs.domatching;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
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
    LocalBroadcastManager localBroadcastManager;
    public static final String COAP_BROADCAST_Result = "ee.ut.cs.domatching.coap.broadcast.result";
    public static final String COAP_BROADCAST_Message = "ee.ut.cs.domatching.coap.broadcast.message";
    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

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

        Intent intent = new Intent(COAP_BROADCAST_Result);
        if(outPut != null){
            intent.putExtra(COAP_BROADCAST_Message,outPut);
        }
        localBroadcastManager.sendBroadcast(intent);
    }
}

