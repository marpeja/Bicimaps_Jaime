package com.example.osmdroid.Extras.ContaminacionRutas;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.osmdroid.Modelo.DialogoSelectorFecha;
import com.example.osmdroid.Modelo.Punto;
import com.example.osmdroid.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MisRutasActivity extends AppCompatActivity implements  DatePickerDialog.OnDateSetListener{

    private MapView map;
    private IMapController mapController;
    private final long DAY_IN_MS = 86400000;
    DatabaseReference mRootReference;
    private FirebaseDatabase database;
    public static Handler handler;
    private long ms;
    private long ms_extra;
    private FloatingActionButton dateSelection;
    private String mode;

    //HEATMAP
    private ArrayList<Punto> greens;
    private ArrayList<Punto> yellows;
    private ArrayList<Punto> oranges;
    private ArrayList<Punto> reds;
    private Drawable green;
    private Drawable yellow;
    private Drawable orange;
    private Drawable red;

    private FolderOverlay contaminationLayer;
    private String user;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mis_rutas);

        map = findViewById(R.id.mis_rutas_map);
        map.setUseDataConnection(true);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        contaminationLayer = new FolderOverlay();
        map.getOverlays().add( contaminationLayer);

        mapController = map.getController();
        mapController.setCenter(new GeoPoint(40.4147,-3.7004));
        mapController.setZoom(14);
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlayManager().add(mRotationGestureOverlay);

        dateSelection = findViewById(R.id.dateSelection);
        dateSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogoSelectorFecha dialogo = new DialogoSelectorFecha();
                dialogo.setOnDateSetListener(MisRutasActivity.this);
                Bundle args = new Bundle();
                args.putLong("fecha", System.currentTimeMillis());
                dialogo.setArguments(args);
                dialogo.show(MisRutasActivity.this.getSupportFragmentManager(), "selectorFecha");
            }
        });

        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(System.currentTimeMillis());
        int hora = fecha.get(Calendar.HOUR_OF_DAY);
        int minutos = fecha.get(Calendar.MINUTE);
        long actualms = fecha.getTimeInMillis();
        long todaysms = actualms - hora*3600000-minutos*60000;

        /*ms = getIntent().getLongExtra("ms", todaysms);
        ms_extra = ms + DAY_IN_MS;*/

        mode = getIntent().getStringExtra("mode");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser().getEmail();


        greens = new ArrayList<>();
        yellows = new ArrayList<>();
        oranges = new ArrayList<>();
        reds = new ArrayList<>();

        green = ContextCompat.getDrawable(this, R.drawable.green_circle);
        yellow = ContextCompat.getDrawable(this, R.drawable.yellow_circle);
        orange = ContextCompat.getDrawable(this, R.drawable.orange_circle);
        red = ContextCompat.getDrawable(this, R.drawable.red_circle);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if (msg.arg1 == 14) {
                    int size = contaminationLayer.getItems().size();
                    for(int i = size - 1; i > -1; i--) {
                        contaminationLayer.remove(contaminationLayer.getItems().get(i));
                    }
                    if(msg.arg2 == 2) {
                        for (Punto v : greens) {
                            Marker marker = new Marker(map);
                            marker.setOnMarkerClickListener(null);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                            marker.setPanToView(false);
                            marker.setInfoWindow(null);
                            marker.setIcon(green);
                            marker.setPosition(new GeoPoint(v.getLatitud(), v.getLongitud()));
                            marker.setAlpha((float) 0.4);
                            contaminationLayer.add(marker);
                        }
                        for (Punto a : yellows) {
                            Marker marker = new Marker(map);
                            marker.setOnMarkerClickListener(null);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                            marker.setPanToView(false);
                            marker.setInfoWindow(null);
                            marker.setIcon(yellow);
                            marker.setPosition(new GeoPoint(a.getLatitud(), a.getLongitud()));
                            marker.setAlpha((float) 0.4);
                            contaminationLayer.add(marker);
                        }
                        for (Punto n : oranges) {
                            Marker marker = new Marker(map);
                            marker.setOnMarkerClickListener(null);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                            marker.setPanToView(false);
                            marker.setInfoWindow(null);
                            marker.setIcon(orange);
                            marker.setPosition(new GeoPoint(n.getLatitud(), n.getLongitud()));
                            marker.setAlpha((float) 0.4);
                            contaminationLayer.add(marker);
                        }
                        for (Punto r : reds) {
                            Marker marker = new Marker(map);
                            marker.setOnMarkerClickListener(null);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                            marker.setPanToView(false);
                            marker.setInfoWindow(null);
                            marker.setIcon(red);
                            marker.setPosition(new GeoPoint(r.getLatitud(), r.getLongitud()));
                            marker.setAlpha((float) 0.4);
                            contaminationLayer.add(marker);
                        }
                    } else if (msg.arg2 == 0) {
                        Toast.makeText(MisRutasActivity.this, "No hay rutas para la fecha seleccionada", Toast.LENGTH_SHORT).show();
                    } else if (msg.arg2 == 1){
                        Toast.makeText(MisRutasActivity.this, "No se registró contaminación en la fecha seleccionada", Toast.LENGTH_SHORT).show();

                    }
                    map.invalidate();
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        map.onPause();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(System.currentTimeMillis());
        calendario.set(Calendar.YEAR, year);
        calendario.set(Calendar.MONTH, month);
        calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        int hora = calendario.get(Calendar.HOUR_OF_DAY);
        int minutos = calendario.get(Calendar.MINUTE);
        long realtime = calendario.getTimeInMillis()- hora*3600000-minutos*60000;
        Calendar calendario2 = Calendar.getInstance();
        calendario2.setTimeInMillis(realtime);
        Log.i("DIA_DEL_CALEN", calendario2.getTimeInMillis()+" "+ calendario2.get(Calendar.HOUR_OF_DAY)+" "+ calendario2.get(Calendar.MINUTE)+" "+calendario2.get(Calendar.DAY_OF_WEEK));
        Log.i("DIA_DEL_CALEN_M", System.currentTimeMillis()+" ");
        //Log.i("DIA_DEL_CALEN", calendario.getTimeInMillis()+" "+ calendario.get(Calendar.HOUR_OF_DAY)+" "+ calendario.get(Calendar.MINUTE)+" "+calendario.get(Calendar.DAY_OF_WEEK));
        Log.i("DIA_DEL_CALEN", DateFormat.getDateInstance().format(new Date(realtime)));

        ms = realtime;
        ms_extra = ms + DAY_IN_MS;

        database = FirebaseDatabase.getInstance();
        mRootReference = database.getReference();
        if( mode.equals("User")) {
            mRootReference.child("Contaminacion").orderByChild("user").equalTo(user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    FireBaseThreadUser thread = new FireBaseThreadUser(dataSnapshot);
                    thread.run();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if(mode.equals("General")){
            mRootReference.child("Contaminacion").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    FireBaseThreadGeneral thread = new FireBaseThreadGeneral(dataSnapshot);
                    thread.run();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    public class FireBaseThreadUser extends Thread {

        DataSnapshot dataSnapshot;

        public FireBaseThreadUser(DataSnapshot dataSnapshot){
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        public void run(){
            greens.clear();
            yellows.clear();
            oranges.clear();
            reds.clear();
            int result = 0;
            for(DataSnapshot postSnap:dataSnapshot.getChildren()){
                //if(Long.parseLong(postSnap.getKey()) < 123416 //Tiempo por debajo del cual queremos borrar){
                //    postSnap.getRef().removeValue();
                //} else {
                if(Long.parseLong(postSnap.getKey()) > ms && Long.parseLong(postSnap.getKey()) < ms_extra) {
                    Punto a = postSnap.getValue(Punto.class);
                    if(a.getUser().equals(user)) {
                        result = 2;
                        if (a.getPm() <= 10) {
                            greens.add(a);
                        } else if (a.getPm() <= 20) {
                            yellows.add(a);
                        } else if (a.getPm() <= 30) {
                            oranges.add(a);
                        } else {
                            reds.add(a);
                        }
                    }
                }
                /*if(Long.parseLong(postSnap.getKey()) > ms_extra){
                    Message message = Message.obtain();
                    message.arg1 = 14;
                    handler.sendMessage(message);
                    return;
                }*/
                //}
            }
            Message message = Message.obtain();
            message.arg1 = 14;
            message.arg2 = result;
            handler.sendMessage(message);
        }

    }

    public class FireBaseThreadGeneral extends Thread {

        DataSnapshot dataSnapshot;

        public FireBaseThreadGeneral(DataSnapshot dataSnapshot){
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        public void run(){
            greens.clear();
            yellows.clear();
            oranges.clear();
            reds.clear();
            int result = 1;
            for(DataSnapshot postSnap:dataSnapshot.getChildren()){
                if(Long.parseLong(postSnap.getKey()) > ms && Long.parseLong(postSnap.getKey()) < ms_extra) {
                    Punto a = postSnap.getValue(Punto.class);
                    result = 2;
                    if (a.getPm() <= 10) {
                        greens.add(a);
                    } else if (a.getPm() <= 20) {
                        yellows.add(a);
                    } else if (a.getPm() <= 30) {
                        oranges.add(a);
                    } else {
                        reds.add(a);
                    }
                }
            }
            Message message = Message.obtain();
            message.arg1 = 14;
            message.arg2 = result;
            handler.sendMessage(message);
        }

    }
}
