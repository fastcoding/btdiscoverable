package com.test.btdiscoverable;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    final String TAG="MainActivity";
    private BluetoothAdapter mAdaptor=null;
    private final int REQUEST_CODE_BT_PERMISSION=1;
    private final int REQUEST_CODE_BT_DISCOVERABLE=2;
    private final int REQUEST_CODE_BT_ENABLE=3;
    private TextView mTextView=null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)){
                updateStatus();
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (checkSelfPermission(Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH
            },REQUEST_CODE_BT_PERMISSION);
        }
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_ADMIN
            },REQUEST_CODE_BT_PERMISSION);
        }

        mTextView=(TextView)findViewById(R.id.status);

        mAdaptor=BluetoothAdapter.getDefaultAdapter();

        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));

        updateStatus();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Toggling BT discoverable...", Snackbar.LENGTH_LONG)
                       .show();
                toggleDiscoverable();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_BT_DISCOVERABLE:
                Log.i(TAG,"receive result code="+resultCode+" for discoverable request");
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void toggleDiscoverable()
    {
        int mode=mAdaptor.getScanMode();
        if (mode!=BluetoothAdapter.SCAN_MODE_NONE){
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN)!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"No permission for bt admin",Toast.LENGTH_LONG).show();
            }else {
                mAdaptor.disable();
            }
        }else{
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN)!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"No permission for bt_admin",Toast.LENGTH_LONG).show();
            }else{
                mAdaptor.enable();
                requestDiscoverable(15);
            }
        }
    }

    protected void updateStatus(){

        int mode=mAdaptor.getScanMode();
        String status;
        switch (mode){
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                status="Discoverable in 15 seconds!";
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                status="Connectable only - not discoverable yet";
                break;
            case BluetoothAdapter.SCAN_MODE_NONE:
                status="Neitther connectable nor discoverable";
                break;
            default:
                status="unknown mode:"+mode;
                break;
        }
        mTextView.setText(status);
    }

    protected void requestDiscoverable(int duration){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration); //discoverable forever
        Log.d(TAG, "request BT Discoverable...");
        startActivityForResult(discoverableIntent, REQUEST_CODE_BT_DISCOVERABLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
