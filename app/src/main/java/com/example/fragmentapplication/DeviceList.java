package com.example.fragmentapplication;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class DeviceList extends AppCompatActivity {
    public static String EXTRA_DEVICE_ADDRESS="device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String>mPairedDevicesArrayAdapter;
    private ArrayAdapter<String>mNewDevicesArrayAdapter;
    private IntentFilter filter = new IntentFilter();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
        setResult(Activity.RESULT_CANCELED);

        progressBar = (ProgressBar)findViewById(R.id.processbar);
        Button scanButton=(Button)findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
                view.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        mPairedDevicesArrayAdapter=new ArrayAdapter<String>(this,
                R.layout.list_item);
        mNewDevicesArrayAdapter=new ArrayAdapter<String>(this,
                R.layout.list_item);

        ListView pairedListView=(ListView)findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListen);

        ListView newDeviceListView=(ListView)findViewById(R.id.new_devices);
        newDeviceListView.setAdapter(mNewDevicesArrayAdapter);
        newDeviceListView.setOnItemClickListener(mDeviceClickListen);

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver,filter);

        mBtAdapter=BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice>pairedDevices=mBtAdapter.getBondedDevices();
        if(pairedDevices.size()>0){
            for(BluetoothDevice device : pairedDevices){
                mPairedDevicesArrayAdapter.add(device.getName()+"\n"
                        +device.getAddress());
            }
        }else{
            String noDevices=getResources().getText(R.string.none_paired)
                    .toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }
    @Override protected void onDestroy(){
        super.onDestroy();
        if(mBtAdapter!=null){
            mBtAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery(){
        if(mBtAdapter.isDiscovering())
            mBtAdapter.cancelDiscovery();
        mBtAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListen=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView,
                                View view, int i, long l) {
            mBtAdapter.cancelDiscovery();

            String info=((TextView) view).getText().toString();
            String address=info.substring(info.length()-17);
            Intent intent =new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS,address);

            setResult(Activity.RESULT_OK,intent);
            finish();
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice  device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    mNewDevicesArrayAdapter.add(device.getName()+"\n" +
                            device.getAddress());
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)){
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DeviceList.this,"搜索完毕",Toast.LENGTH_SHORT).show();
                if(mNewDevicesArrayAdapter.getCount()==0){
                    String noDevices=getResources().getText(
                            R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
}
