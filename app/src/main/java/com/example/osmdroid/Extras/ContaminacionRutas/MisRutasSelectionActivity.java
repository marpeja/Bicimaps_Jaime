package com.example.osmdroid.Extras.ContaminacionRutas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.osmdroid.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MisRutasSelectionActivity extends AppCompatActivity {

    private RecyclerView misRutas;
    private RecyclerView.LayoutManager layoutManager;
    private MisRutasSelectionAdapter adapter;
    private ArrayList<String> dates = new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();
    private long[] ms_dates = new long[7];
    private final long DAY_IN_MS = 86400000;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_rutas_selection);

        fechas();
        misRutas = findViewById(R.id.recycler_misrutas);
        adapter = new MisRutasSelectionAdapter(this, dates, days);
        adapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = misRutas.getChildAdapterPosition(v);
                long ms = ms_dates[pos];
                Intent intent = new Intent(MisRutasSelectionActivity.this, MisRutasActivity.class);
                intent.putExtra("ms", ms);
                startActivity(intent);
            }
        });
        misRutas.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        misRutas.setLayoutManager(layoutManager);


    }

    public void fechas(){

        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(System.currentTimeMillis());
        int hora = fecha.get(Calendar.HOUR_OF_DAY);
        int minutos = fecha.get(Calendar.MINUTE);
        Log.i("CONSULTAR_HORA", hora+" "+minutos+" "+ DateFormat.getDateInstance().format(new Date(fecha.getTimeInMillis())));
        long actualms = fecha.getTimeInMillis();
        long todaysms = actualms - hora*3600000-minutos*60000;
        Log.i("CONSULTAR_HORA_N", DateFormat.getDateInstance().format(new Date(todaysms)));
        dates.add(DateFormat.getDateInstance().format(new Date(todaysms)));
        getDays(fecha.get(Calendar.DAY_OF_WEEK));
        ms_dates[0] = todaysms;
        for(int i = 1; i<7; i++){
            todaysms = todaysms - DAY_IN_MS;
            dates.add(DateFormat.getDateInstance().format(new Date(todaysms)));
            ms_dates[i] = todaysms;
            Log.i("CONSULTAR_HORA_F", DateFormat.getDateInstance().format(new Date(todaysms)));
            Log.i("CONSULTAR_HORA_F", DateFormat.getTimeInstance().format(new Date(todaysms)));
        }
    }

    private void getDays(int day){
        for(int i = 0; i < 7; i++){
            addStringDay(day);
            day--;
            if(day == 0){
                day = 7;
            }
        }
    }

    private void addStringDay(int day){
        switch(day){
            case 1:
                days.add("Domingo");
                break;
            case 2:
                days.add("Lunes");
                break;
            case 3:
                days.add("Martes");
                break;
            case 4:
                days.add("Miércoles");
                break;
            case 5:
                days.add("Jueves");
                break;
            case 6:
                days.add("Viernes");
                break;
            case 7:
                days.add("Sábado");
                break;
            default:
        }
    }
}
