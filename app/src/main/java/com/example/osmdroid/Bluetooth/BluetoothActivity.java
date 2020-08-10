package com.example.osmdroid.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.osmdroid.MainActivity;
import com.example.osmdroid.R;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter;
    Button btn_find;
    ArrayList<BluetoothDevice> MyDevicesFound=new ArrayList<>();
    private RecyclerView recyclerBluetooth;
    private BTAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    Context mcontext;
    private String MAC_ADDRESS="";
    private final int BLUETOOTH_ACTIVITY = 66;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity);
        mcontext=this;

        recyclerBluetooth = (RecyclerView) findViewById(R.id.lst_BTDevices);
        recyclerBluetooth.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerBluetooth.setLayoutManager(layoutManager);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter != null) {

            //Enciendo Bluetooth
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, RESULT_OK);
            }

            IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            registerReceiver(mReceiver, filter);


            //boton buscar dispositivos
            btn_find = findViewById(R.id.btn_find);
            btn_find.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBluetoothAdapter.startDiscovery();

                }
            });

        }else{
            Toast.makeText(this, "El dispositivo no cuenta con bluetooth", Toast.LENGTH_SHORT).show();
        }

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Bluetooth", "got action " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                boolean isAlready = false;
                for(BluetoothDevice dev : MyDevicesFound){
                    if(device.getAddress().equals(dev.getAddress())){
                        isAlready = true;
                    }
                }
                if(!isAlready) {
                    MyDevicesFound.add(device);
                    //String[] devicesFound = MyDevicesFound.toArray(new String[0]);

                    mAdapter = new BTAdapter(MyDevicesFound);

                    //listener de Recycler View
                    mAdapter.setOnItemClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = recyclerBluetooth.getChildAdapterPosition(v);
                            MAC_ADDRESS = MyDevicesFound.get(position).getAddress();
                            mBluetoothAdapter.cancelDiscovery();
                            Toast.makeText(BluetoothActivity.this, "Conectado a "+MyDevicesFound.get(position).getName(), Toast.LENGTH_SHORT).show();
                            finishActivity();
                        }
                    });

                    recyclerBluetooth.setAdapter(mAdapter);

                    Log.i("Bluetooth", "got device " + deviceName);
                }
            }
        }
    };

    public void finishActivity(){
        Intent intent = new Intent();
        intent.putExtra("MAC", MAC_ADDRESS);
        setResult(BLUETOOTH_ACTIVITY, intent);
        finish();

    }



}

