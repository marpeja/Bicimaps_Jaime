package com.example.osmdroid.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.osmdroid.R;

import java.util.ArrayList;

public class BTAdapter extends RecyclerView.Adapter<BTAdapter.ViewHolder> {
    private ArrayList<BluetoothDevice> mDataset;
    private View.OnClickListener mListener;


    public void setOnItemClickListener( View.OnClickListener listener) {
        mListener=listener;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public TextView mac;

        public ViewHolder(View itemView) {
            super(itemView);
            name= itemView.findViewById(R.id.bt_Name);
            mac= itemView.findViewById(R.id.bt_MAC);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public BTAdapter(ArrayList<BluetoothDevice> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BTAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_adapter, parent, false);
        v.setOnClickListener(mListener);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        BluetoothDevice device = mDataset.get(position);

        if (device.getName() == null) {
            holder.name.setText("Sin nombre");
        } else {
            holder.name.setText(device.getName());
        }
        holder.mac.setText(device.getAddress());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}
