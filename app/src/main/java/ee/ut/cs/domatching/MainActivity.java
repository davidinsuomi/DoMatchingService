package ee.ut.cs.domatching;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by weiding on 02/04/15.
 */
public class MainActivity extends Activity {
    BroadcastReceiver receiver;

    ListView listview;
    ArrayList<String> debugLists = new ArrayList<String>();
    private final String TAG = "MainActivity";
    ArrayAdapter<String> listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // create server
        Intent intent = new Intent(this,CoapBackgroundService.class);
        startService(intent);
        listview = new ListView(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, debugLists);
        listview.setAdapter(listAdapter);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(CoapBackgroundService.COAP_BROADCAST_Message);
                // do something here.
                Log.e(TAG,"onReceive called");
                UpdateUI(s);
            }
        };

        setContentView(listview);
    }

    private void UpdateUI(String s){
        debugLists.add(s);
        listAdapter.notifyDataSetChanged();
        listview.invalidate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(CoapBackgroundService.COAP_BROADCAST_Result));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

}
