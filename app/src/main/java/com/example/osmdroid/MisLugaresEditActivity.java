package com.example.osmdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.StringTokenizer;

public class MisLugaresEditActivity extends AppCompatActivity {

    private Spinner placeType;
    private EditText placeAddress;
    private EditText placeName;
    private Button savePlace;
    //private Button deletePlace;
    private int[] placesType;
    private String[] placesAddress;
    private String[] placesNames;
    private int position;
    private static String[] types = {"Casa", "Trabajo", "Otro"};
    private boolean newPlace;

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_lugares_edit);

        placeType = findViewById(R.id.placeTypeSpinner);
        placeAddress = findViewById(R.id.place_address_edit);
        placeName = findViewById(R.id.place_name_edit);

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placeType.setAdapter(adaptador);

        newPlace = getIntent().getBooleanExtra("new", true);
        if(newPlace){
            placeType.setSelection(0);
        } else {
            placeType.setSelection(getIntent().getIntExtra("placeType",  0));
            placeAddress.setText(getIntent().getStringExtra("placeDir"));
            placeName.setText(getIntent().getStringExtra("placeName"));
            position = getIntent().getIntExtra("position", 0);
        }

        savePlace = findViewById(R.id.savePlace);
        savePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });

        /*deletePlace = findViewById(R.id.deletePlace);
        deletePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });*/

        getInfoPreferences();

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.edicion_lugar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch(item.getItemId()){
            case R.id.deletePlace:
                delete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void guardar(){
        String direction = placeAddress.getText().toString();
        String name = placeName.getText().toString();
        int type = placeType.getSelectedItemPosition();
        if(!direction.equals("") && !name.equals("")){
            if(newPlace){
                String[] newDirections = new String[placesAddress.length + 1];
                String[] newNames = new String[placesAddress.length + 1];
                int[] newTypes = new int[placesAddress.length + 1];
                int i;
                for(i = 0 ; i < placesAddress.length; i++){
                    newDirections[i] = placesAddress[i];
                    newNames[i] = placesNames[i];
                    newTypes[i] = placesType[i];
                    //Log.i("SAVE_PREFERENCES_EDIT", i+" "+newTypes[i]+" "+ newDirections[i]);
                }
                newDirections[i] = direction;
                newNames[i] = name;
                newTypes[i] = type;
                savePreferences(newTypes, newDirections, newNames);
                //Log.i("SAVE_PREFERENCES_EDIT", i+" "+newTypes[i]+" "+ newDirections[i]);
            } else {
                String[] newDirections = placesAddress;
                String[] newNames = placesNames;
                int[] newTypes = placesType;
                Log.i("SIZES_EDIT", placesAddress.length+" "+newDirections.length );
                newDirections[position] = direction;
                newNames[position] = name;
                newTypes[position] = type;
                /*for(int i = 0 ; i < newDirections.length; i++){
                    Log.i("SAVE_PREFERENCES_EDIT", i+" "+newTypes[i]+" "+ newDirections[i]);
                }*/
                savePreferences(newTypes, newDirections, newNames);
            }
            finish();
        } else {
            Toast.makeText(MisLugaresEditActivity.this, "INTRODUCE UN NOMBRE Y DIRECCIÓN", Toast.LENGTH_SHORT).show();
        }
    }

    public void getInfoPreferences(){
        SharedPreferences app = getSharedPreferences("app",Context.MODE_PRIVATE);
        String placesDir = app.getString("placesDir", null);
        if(placesDir != null && !placesDir.equals("")) {
            placesAddress = placesDir.split("_");
            placesNames = app.getString("placesNames", null).split("_");
            String placesTypeString = app.getString("placesType", "");
            StringTokenizer st = new StringTokenizer(placesTypeString, ",");
            placesType = new int[placesAddress.length];
            for (int i = 0; i < placesAddress.length; i++) {
                placesType[i] = Integer.parseInt(st.nextToken());
            }
            for (int i = 0; i < placesAddress.length; i++) {
                Log.i("SAVE_PREFERENCES_EDIT", i+" " +placesType[i]+" "+ placesAddress[i]);
            }
        } else {
            placesAddress = new String[0];
            placesNames = new String[0];
            placesType = new int[0];
        }
    }

    public void savePreferences(int[] newTypes, String[] newDirections, String[] newNames){
        //Log.i("SAVE_PREFERENCES", newTypes[0]+" "+ newDirections[0]);
        SharedPreferences app = getSharedPreferences("app",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = app.edit();
        StringBuilder sbDir = new StringBuilder();
        StringBuilder sbName = new StringBuilder();
        StringBuilder sbInt = new StringBuilder();
        for (int i = 0; i < newDirections.length; i++) {
            sbDir.append(newDirections[i]).append("_");
            sbName.append(newNames[i]).append("_");
            sbInt.append(newTypes[i]).append(",");
        }
        editor.putString("placesDir", sbDir.toString());
        editor.putString("placesNames", sbName.toString());
        editor.putString("placesType", sbInt.toString());
        editor.apply();
    }

    public void delete(){
        if(newPlace){
            finish();
        } else {
            String[] newDirections = new String[placesAddress.length-1];
            String[] newNames = new String[placesAddress.length-1];
            int[] newTypes = new int[placesAddress.length-1];
            Log.i("SIZES_EDIT", placesAddress.length+" "+newDirections.length );
            for(int i = 0; i < newDirections.length; i++){
                if(placesAddress.length > 1) {
                    if (i >= position) {
                        newDirections[i] = placesAddress[i + 1];
                        newNames[i] = placesNames[i + 1];
                        newTypes[i] = placesType[i + 1];
                    } else {
                        newDirections[i] = placesAddress[i];
                        newNames[i] = placesNames[i];
                        newTypes[i] = placesType[i];

                    }
                } else {
                    newDirections[i] = null;
                    newNames[i] = null;
                    newTypes[i] = 0;
                    i = 100;
                }
            }
            savePreferences(newTypes, newDirections, newNames);
        }
        finish();
    }
}
