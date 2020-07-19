package com.example.osmdroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.osmdroid.Datos.MisLugaresAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.StringTokenizer;

public class MisLugaresActivity extends AppCompatActivity {

    private RecyclerView misLugares;
    private RecyclerView.LayoutManager layoutManager;
    private MisLugaresAdapter lugaresAdapter;
    private FloatingActionButton addPlace;
    private int[] placesType;
    private String[] placesAddress;
    private String[] placesNames;
    private int MAX_PLACES;

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_lugares);

        misLugares = findViewById(R.id.recycler_misLugares);
        updateMisLugaresAdapter();

        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        MAX_PLACES = Integer.parseInt(config.getString("max_places", "4"));
        Log.i("MAAAAX_LUG", MAX_PLACES+" ");
        layoutManager = new LinearLayoutManager(this);
        misLugares.setLayoutManager(layoutManager);

        addPlace = findViewById(R.id.addPlace);

        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lugaresAdapter.getItemCount() < MAX_PLACES) {
                    Intent intent = new Intent(MisLugaresActivity.this, MisLugaresEditActivity.class);
                    intent.putExtra("new", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(MisLugaresActivity.this, "MÁXIMOS LUGARES FAVORITOS ALCANZADOS, POR FAVOR ELIMINE UNO", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateMisLugaresAdapter (){
        SharedPreferences app = getSharedPreferences("app",Context.MODE_PRIVATE);
        String placesDir = app.getString("placesDir", null);
        Log.i("PROFIIIILEEE", placesDir+" FUERA ");
        if(placesDir != null  && !placesDir.equals("") ) {
            Log.i("SAVE_PREFERENCES", "ENTRO");
            placesAddress = placesDir.split("_");
            placesNames = app.getString("placesNames", null).split("_");
            String placesTypeString = app.getString("placesType", "");
            StringTokenizer st = new StringTokenizer(placesTypeString, ",");
            placesType = new int[placesAddress.length];
            for (int i = 0; i < placesAddress.length; i++) {
                placesType[i] = Integer.parseInt(st.nextToken());
                Log.i("SAVE_PREFERENCES", placesType[i]+" "+ placesAddress[i]);
            }
        } else {
            placesType = new int[0];
            placesAddress = new String[0];
            placesNames = new String[0];
        }

        for (int i = 0; i < placesAddress.length; i++) {
            Log.i("SAVE_PREFERENCES_ADAP", i+" " +placesType[i]+" "+ placesAddress[i]);
        }

        lugaresAdapter = new MisLugaresAdapter(this, placesType, placesAddress, placesNames);
        lugaresAdapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = misLugares.getChildAdapterPosition(v);
                Intent intent = new Intent(MisLugaresActivity.this, MisLugaresEditActivity.class);
                intent.putExtra("placeDir", placesAddress[position]);
                intent.putExtra("placeType", placesType[position]);
                intent.putExtra("placeName", placesNames[position]);
                intent.putExtra("position", position);
                intent.putExtra("new", false);
                startActivity(intent);
            }
        });
        misLugares.setAdapter(lugaresAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();

        updateMisLugaresAdapter();
    }
}
