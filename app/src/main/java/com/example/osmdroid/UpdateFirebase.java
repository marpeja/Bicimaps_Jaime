package com.example.osmdroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.osmdroid.Modelo.Punto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

public class UpdateFirebase extends AppCompatActivity {

    DatabaseReference mRootReference;
    private FirebaseDatabase database;
    private EditText latitud;
    private EditText longitud;
    private EditText contaminacion;
    private EditText mac;
    private Button enviar;
    private Button recibir;
    private Button stop;
    private EnvioFirebase envioThread = new EnvioFirebase();
    private String user;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.firebase);

        database = FirebaseDatabase.getInstance();
        mRootReference = database.getReference();
        mRootReference.child("Contaminacion").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UpdateFirebase.FireBaseThread thread = new UpdateFirebase.FireBaseThread(dataSnapshot);
                thread.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        latitud = findViewById(R.id.latitud);
        longitud = findViewById(R.id.longitud);
        contaminacion = findViewById(R.id.pm);
        mac = findViewById(R.id.mac);

        enviar = findViewById(R.id.enviar);
        recibir = findViewById(R.id.recibir);
        stop = findViewById(R.id.stop);

        user = getIntent().getStringExtra("user");

        consultar();


        envioThread.start();

        recibir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(UpdateFirebase.this, MisRutasActivity.class);
                startActivity(intent);*/
                Intent intent = new Intent(UpdateFirebase.this, MisRutasSelectionActivity.class);
                startActivity(intent);
            }
        });

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                envioThread.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        envioFirebase();
                    }
                });
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                envioThread.handler.getLooper().quit();
            }
        });




    }

    public class FireBaseThread extends Thread {

        DataSnapshot dataSnapshot;

        public FireBaseThread(DataSnapshot dataSnapshot){
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        public void run(){
            for(DataSnapshot postSnap:dataSnapshot.getChildren()){
                //if(Long.parseLong(postSnap.getKey()) < 123416 //Tiempo por debajo del cual queremos borrar){
                //    postSnap.getRef().removeValue();
                //} else {
                Punto a = postSnap.getValue(Punto.class);
                if (a.getPm() <= 10) {

                } else if (a.getPm() <= 20) {

                } else if (a.getPm() <= 30) {

                } else {

                }
                //}
            }
        }

    }

    class EnvioFirebase extends Thread{

        Handler handler;

        @Override
        public void run(){
            Looper.prepare();

            handler = new Handler();

            Looper.loop();

            Log.i("END_OF_THREAD", "end of envioFirebase");


        }
    }

    public void envioFirebase(){
        Punto aux = new Punto();
        aux.setLatitud(Double.parseDouble(latitud.getText().toString()));
        aux.setLongitud(Double.parseDouble(longitud.getText().toString()));
        aux.setPm(Double.parseDouble(contaminacion.getText().toString()));
        aux.setMac(mac.getText().toString());
        aux.setUser(user);

        Map<String, Object> datos = new HashMap<>();
        datos.put("latitud", aux.getLatitud());
        datos.put("longitud", aux.getLongitud());
        datos.put("pm", aux.getPm());
        datos.put("mac", aux.getMac());
        datos.put("user", aux.getUser());

        long time = currentTimeMillis();
        String string_time = String.valueOf(time);
        mRootReference.child("Contaminacion").child(string_time).setValue(datos);

        //mRootReference.child("Contaminacion").push().setValue(datos);
    }

    public void consultar(){

        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(System.currentTimeMillis());
        int hora = fecha.get(Calendar.HOUR_OF_DAY);
        int hora2 = fecha.get(Calendar.HOUR);
        int minutos = fecha.get(Calendar.MINUTE);
        Log.i("CONSULTAR_HORA", hora+"  "+hora2+" "+minutos+" "+ DateFormat.getDateInstance().format(new Date(fecha.getTimeInMillis())));
        long actualms = fecha.getTimeInMillis();
        long todaysms = actualms - hora*3600000-minutos*60000;
        Log.i("CONSULTAR_HORA_N", DateFormat.getTimeInstance().format(new Date(todaysms)));
        for(int i = 1; i<7; i++){
            todaysms = todaysms - 86400000;
            long dayms = todaysms;
            Log.i("CONSULTAR_HORA_F", DateFormat.getDateInstance().format(new Date(dayms)));
            Log.i("CONSULTAR_HORA_F", DateFormat.getTimeInstance().format(new Date(dayms)));
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!envioThread.isAlive()){
            envioThread.start();
        }
    }
}
