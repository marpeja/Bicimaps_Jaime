package com.example.osmdroid.Extras.Lugares;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.osmdroid.R;

public class MisLugaresAdapter extends RecyclerView.Adapter<MisLugaresAdapter.ViewHolder> {

    private LayoutInflater inflador;
    private int[] placesType;
    private String[] placesAddress;
    private String[] placesNames;
    protected View.OnClickListener onClickListener;

    public MisLugaresAdapter(Context contexto, int[] placesType, String[] placesDirections, String[] placesNames){
        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.placesType = placesType;
        this.placesAddress = placesDirections;
        this.placesNames = placesNames;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView placeIconView;
        public TextView placeNameView;
        public TextView placeAddressView;

        public ViewHolder(View itemView){
            super(itemView);
            placeIconView = itemView.findViewById(R.id.placeIcon);
            placeNameView = itemView.findViewById(R.id.place_name);
            placeAddressView = itemView.findViewById(R.id.place_address);
        }
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
    @NonNull
    @Override
    public MisLugaresAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = inflador.inflate(R.layout.mis_lugares_adapter, null);
        v.setOnClickListener(onClickListener);
        return new MisLugaresAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MisLugaresAdapter.ViewHolder holder, int position) {
        int placeType = placesType[position];
        String placeDirection = placesAddress[position];
        String placeName = placesNames[position];
        holder.placeIconView.setImageResource(getTypeIcon(placeType));
        holder.placeNameView.setText(placeName);
        holder.placeAddressView.setText(placeDirection);
    }

    @Override
    public int getItemCount() {
        if(placesAddress != null) {
            //Log.i("ITEM_COUNT_EDIT", placesAddress+"");
            return placesAddress.length;
        } else {
            //Log.i("ITEM_COUNT_EDIT_2", placesAddress+"");
            return 0;
        }
    }

    private int getTypeIcon(int i){
        int resourceId;
        switch (i){
            case 0:
                resourceId = R.drawable.ic_home_black;
                break;
            case 1:
                resourceId = R.drawable.ic_work_black;
                break;
            default:
                resourceId = R.drawable.ic_location_city_black;
        }
        return resourceId;
    }
}
