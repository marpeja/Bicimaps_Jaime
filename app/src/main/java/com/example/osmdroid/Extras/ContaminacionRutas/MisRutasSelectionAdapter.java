package com.example.osmdroid.Extras.ContaminacionRutas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.osmdroid.R;

import java.util.ArrayList;

public class MisRutasSelectionAdapter extends RecyclerView.Adapter<MisRutasSelectionAdapter.ViewHolder> {

    private LayoutInflater inflador;
    private ArrayList<String> dates;
    private ArrayList<String> days;
    private View.OnClickListener onClickListener;

    public MisRutasSelectionAdapter(Context contexto, ArrayList<String> dates, ArrayList<String> days){
        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.dates = dates;
        this.days = days;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView dateView;
        public TextView dayView;

        public ViewHolder(View itemView){
            super(itemView);
            dateView = itemView.findViewById(R.id.route_day);
            dayView = itemView.findViewById(R.id.week_day);
        }
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = inflador.inflate(R.layout.mis_rutas_selection_adapter, null);
        v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String date = dates.get(position);
        String day = days.get(position);
        holder.dateView.setText(date);
        holder.dayView.setText(day);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }
}
