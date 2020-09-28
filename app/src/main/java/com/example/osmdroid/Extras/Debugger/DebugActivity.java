package com.example.osmdroid.Extras.Debugger;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.osmdroid.Bluetooth.BluetoothActivity;
import com.example.osmdroid.Bluetooth.BluetoothService;
import com.example.osmdroid.Modelo.Punto;
import com.example.osmdroid.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import static java.lang.System.currentTimeMillis;

public class DebugActivity extends AppCompatActivity implements LocationListener {
    private MapView map;
    private IMapController mapController;
    private Bitmap icon;

    DatabaseReference mRootReference;
    private FirebaseDatabase database;

    private LocationManager locationManager;
    private GpsMyLocationProvider myLocationProvider;
    private GeoPoint myLocation;
    private GeoPoint myLastLocation;
    private GeoPoint getLastLocation;
    private MyLocationNewOverlay myLocationNewOverlay;

    private FloatingActionButton myLocationBtn;
    private FloatingActionButton bluetoothSelection;
    private FloatingActionButton observations;
    private FloatingActionButton followLocation;
    private FloatingActionButton publishCSV;
    private Button track;


    private FirebaseUser user;

    boolean followLoc = false;
    boolean tracking = false;
    private final int BLUETOOTH_ACTIVITY = 66;
    private final int OBSERVATIONS_ACTIVITY = 4;
    private String MAC="";
    private char start_DB = 'd';
    private char finish_PM='s';
    private String observationsString ="";

    private double lastPM = 0;

    private String filePath="";

    private long time = 0;
    private long time_prev = 0;

    private ConnectivityManager networkManager;


    //fecha
    private Calendar myCalendar;
    private String today;
    private SimpleDateFormat sdf;
    private SimpleDateFormat df;


    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debug);

        myLocationBtn = findViewById(R.id.myLocationDebugger);
        bluetoothSelection = findViewById(R.id.btSelectorDebugger);
        observations = findViewById(R.id.observations);
        followLocation = findViewById(R.id.followLocation);
        publishCSV = findViewById(R.id.publishCSV);
        track = findViewById(R.id.track);

        map = findViewById(R.id.debugger_map);
        map.setUseDataConnection(true);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(14);

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlayManager().add(mRotationGestureOverlay);

        database = FirebaseDatabase.getInstance();
        mRootReference = database.getReference();


        SharedPreferences app = getSharedPreferences("app", Context.MODE_PRIVATE);
        filePath = app.getString("CSV", null);
        MAC = app.getString("MAC", null);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);

            myLocationProvider = new GpsMyLocationProvider(this);
            myLocationNewOverlay = new MyLocationNewOverlay(myLocationProvider, map);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.enableFollowLocation();
            map.getOverlays().add( myLocationNewOverlay);
            myLastLocation = new GeoPoint(0.0,0.0);
            Log.i("LOCALIZACION_INICIAL", locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)+"");
            Log.i("LOCALIZACION_INICIAL_2", myLocationNewOverlay.getMyLocation()+"");
            if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                myLocation = new GeoPoint(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                mapController.setCenter(myLocation);
            } else {
                myLocation = new GeoPoint(40.416724,-3.703493); //En caso de que no se conozca última ubicación,
                mapController.setCenter(myLocation);                                //se fija esta en el centro de Madrid
            }
            //Cambiamos el icono de ubicación

            icon = BitmapFactory.decodeResource(
                    DebugActivity.this.getResources(), R.drawable.ic_bike);
            myLocationNewOverlay.setDirectionArrow(icon, icon);
            myLocationNewOverlay.setPersonHotspot(24.0f * map.getContext().getResources().getDisplayMetrics().density + 0.5f,39.0f * map.getContext().getResources().getDisplayMetrics().density + 0.5f);
        }

        myLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myLocationNewOverlay.getMyLocation() != null) {
                    mapController.setCenter(myLocationNewOverlay.getMyLocation());
                } else {
                    mapController.setCenter(myLocation);
                }
                if(map.getZoomLevelDouble() < 14)
                    mapController.setZoom(14);
                map.setMapOrientation(0.0f);
            }
        });

        followLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(followLoc){
                    followLoc = false;
                    followLocation.setSupportBackgroundTintList(ContextCompat.getColorStateList(DebugActivity.this, R.color.colorPrimary));
                } else {
                    followLoc = true;
                    followLocation.setSupportBackgroundTintList(ContextCompat.getColorStateList(DebugActivity.this, R.color.colorPrimaryDark));
                }
            }
        });

        observations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DebugActivity.this, ObservacionesActivity.class);
                intent.putExtra("PMData", (int)lastPM);
                intent.putExtra("Speed", getVelocidad());
                startActivityForResult(intent, OBSERVATIONS_ACTIVITY);
            }
        });

        bluetoothSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DebugActivity.this, BluetoothActivity.class);
                startActivityForResult(intent, BLUETOOTH_ACTIVITY);

            }
        });

        publishCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CSVtoFB();
            }
        });

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tracking){
                    tracking = false;
                    track.setBackgroundTintList(ContextCompat.getColorStateList(DebugActivity.this, R.color.colorPrimary));
                    track.setText("Iniciar Recorrido");
                    Intent newIntent = new Intent(DebugActivity.this, BluetoothService.class);
                    newIntent.putExtra("PM", finish_PM);
                    startService(newIntent);
                    observationsString = null;
                } else {
                    myCalendar = Calendar.getInstance();
                    String myFormat = "dd-MM-yyyy";
                    sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
                    today = sdf.format(myCalendar.getTime());

                    //Obtiene ruta de sdcard
                    File pathToExternalStorage = Environment.getExternalStorageDirectory();
                    //agrega directorio /myFiles
                    File appDirectory = new File(pathToExternalStorage.getPath()+"/BiciMaps/");
                    SharedPreferences app = getSharedPreferences("app", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = app.edit();
                    if(!appDirectory.exists()) {
                        //Si no existe el directorio, se crea usando mkdirs()
                        appDirectory.mkdirs();
                        Log.i("FILES", "No Exists");
                    } else {
                        Log.i("FILES", "exists");
                    }

                    File saveFilePath = new File(appDirectory, today+ ".csv");
                    filePath = saveFilePath.toString();
                    if(!saveFilePath.exists()) {
                        editor.putString("CSV", filePath);
                        editor.apply();
                    }

                    tracking = true;
                    track.setBackgroundTintList(ContextCompat.getColorStateList(DebugActivity.this, R.color.colorPrimaryDark));
                    track.setText("Finalizar Recorrido");
                    Intent newIntent = new Intent(DebugActivity.this, BluetoothService.class);
                    newIntent.putExtra("PM", start_DB);
                    /*long a = 30000;
                    newIntent.putExtra("Duration", a);*/
                    newIntent.putExtra("Debug", true);
                    startService(newIntent);
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("PM_Data_Debug"));

        user = FirebaseAuth.getInstance().getCurrentUser();


        //long time = currentTimeMillis();
        //myCalendar.setTimeInMillis(time);
        //String string_time = String.valueOf(time);
        //Log.i("TIEMPO_CALENDAR",myCalendar.getTimeInMillis() + ""+ myCalendar.getTime());
        //Log.i("TIEMPO_SYSTEM", time+"");
        df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");






    }


    @Override
    public void onLocationChanged(Location location) {
        //Log.i("LOCALIZACION_INICIAL", "onLocation");
        //Para centrar el mapa en la ubi del usuario al principio.
            //Log.i("LOCALIZACION_INICIAL_2", "onLocation");
        if(myLocation != new GeoPoint(location)) {
            myLastLocation = myLocation;
            time_prev = time;
            myLocation = new GeoPoint(location);
            time = System.currentTimeMillis();
            if (followLoc) {
                mapController.setCenter(myLocation);
                map.setMapOrientation(-location.getBearing());
            }
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BLUETOOTH_ACTIVITY){
            if(data != null) {
                if (data.getStringExtra("MAC") != null) {
                    if (!data.getStringExtra("MAC").isEmpty()) {
                        MAC = data.getStringExtra("MAC");
                        Log.i("DIRECCION_MAC", MAC);
                        SharedPreferences app = getSharedPreferences("app", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = app.edit();
                        editor.putString("MAC", MAC);
                        editor.apply();
                    } else {
                        Log.i("DIRECCION_MAC", "VACIA");
                    }
                } else {
                    Log.i("DIRECCION_MAC", "NULA");
                }
            } else {
                Log.i("DIRECCION_MAC", "DATA NULA");
            }
        }

        if(requestCode == OBSERVATIONS_ACTIVITY){
            if(data != null) {
                if (data.getStringExtra("OBS") != null) {
                    observationsString = data.getStringExtra("OBS");
                }
            }
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            ArrayList<Integer> PMData = intent.getIntegerArrayListExtra("TestData");
            if (PMData != null) {
                lastPM = PMData.get(0);
                Punto aux = new Punto();
                aux.setLatitud(myLocation.getLatitude());
                aux.setLongitud(myLocation.getLongitude());
                aux.setPm(lastPM);
                aux.setMac(MAC);
                aux.setUser(user.getEmail());
                aux.setSpeed(getVelocidad());
                if(observationsString != null ) {
                    if(! observationsString.equals("")) {
                        aux.setObservations(observationsString);
                        observationsString = null;
                    }
                }

                long time = currentTimeMillis();

                Toast.makeText(DebugActivity.this, "Dato "+PMData.get(0)+ " "+PMData.get(1) , Toast.LENGTH_SHORT).show();

                networkManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if( networkManager.getActiveNetwork() != null){
                    writeFirebase(aux, time);
                    writeCSV(aux, time);
                } else {
                    writeCSV(aux, time);
                }


            }

           /* Toast.makeText(DebugActivity.this, "Dato " , Toast.LENGTH_SHORT).show();
            if(observationsString != null){
                Toast.makeText(DebugActivity.this, "NO " , Toast.LENGTH_SHORT).show();

            }*/
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        //lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //setLocationManager();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                //on API15 AVDs,network provider fails. no idea why
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);
            } catch (Exception ex) {
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up

        //Dejar de escuchar cambios de localización.
        locationManager.removeUpdates(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(tracking) {
            Intent newIntent = new Intent(DebugActivity.this, BluetoothService.class);
            newIntent.putExtra("PM", finish_PM);
            startService(newIntent);
        }
    }

    private void writeFirebase(Punto punto, long time){

        String string_time = String.valueOf(time);

        Map<String, Object> datos = new HashMap<>();
        datos.put("latitud", punto.getLatitud());
        datos.put("longitud", punto.getLongitud());
        datos.put("pm", punto.getPm());
        datos.put("mac", punto.getMac());
        datos.put("user", punto.getUser());
        datos.put("speed", punto.getSpeed());
        datos.put("observaciones", punto.getObservations());


        mRootReference.child("Contaminacion").child(string_time).setValue(datos);
        //Toast.makeText(DebugActivity.this, "Dato "+PMData.get(0)+ " "+PMData.get(1) , Toast.LENGTH_SHORT).show();
    }

    private void CSVtoFB() {
        String time = "";
        String lat = "";
        String lon = "";
        String mac = "";
        String user = "";
        String vel = "";
        String acel = "";
        String obs = "";
        String pm = "";
        /*List<Integer> PMlist = new ArrayList<>();
        for (int i = 0; i < N_PM; i++) {
            PMlist.add(i, 0);
        }*/
        String aux = "";
        int c;
        int contador = 0;
        try {

            //BufferedReader reader = new BufferedReader(new FileReader(filePath));
            FileReader reader = new FileReader(filePath);
            while ((c = reader.read()) != -1) {
                if (!(((char) c) == ('\n'))) {
                    if (((char) c) == (',')) {
                        if (contador == 0) {
                            time = aux;
                            aux = "";
                        } else if (contador == 1) {
                            aux = "";
                        } else if (contador == 2) {
                            aux = "";
                        } else if (contador == 3) {
                            lat = aux;
                            aux = "";
                        } else if (contador == 4) {
                            lon = aux;
                            aux = "";
                        } else if (contador == 5) {
                            mac = aux;
                            aux = "";
                        } else if (contador == 6) {
                            user = aux;
                            aux = "";
                        }else if (contador == 7) {
                            vel = aux;
                            aux = "";
                        } else if (contador == 8) {
                            obs = aux;
                            aux = "";
                        }
                        contador++;
                    } else {
                        aux = aux + (char) c;
                    }
                } else {
                    pm = aux;
                    aux = "";
                    /*for (int i = 0; i < N_PM; i++) {
                        PMlist.set(i, Integer.parseInt(pm));
                    }*/
                    Punto punto = new Punto();
                    punto.setLatitud(Double.parseDouble(lat));
                    punto.setLongitud(Double.parseDouble(lon));
                    punto.setMac(mac);
                    punto.setUser(user);
                    punto.setSpeed(Double.parseDouble(vel));
                    //punto.setAceleracion(Double.parseDouble(acel));
                    if(!obs.equals("null")) {
                        punto.setObservations(obs);
                    }
                    punto.setPm(Double.parseDouble(pm));

                    writeFirebase(punto, Long.parseLong(time));

                    contador = 0;
                }
            }

            //File file = new File(filePath);
            //file.delete();

            Toast.makeText(getApplicationContext(), "Tu información ha sido añadida a la base de datos",
                    Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            Log.i("File not found", e.toString());
            Toast.makeText(getApplicationContext(), "No hay información nueva para añadir a la base de datos",
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.i("Error", e.toString());
        }
    }

    private void writeCSV(Punto punto, long time) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            String fDate = df.format(calendar.getTime());
            Log.i("FILES", filePath);

            FileOutputStream fos = new FileOutputStream(filePath, true);
            OutputStreamWriter file = new OutputStreamWriter(fos);
           /* try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                fos.write(fileContents.toByteArray());
            }*/
            file.append(String.valueOf(time));
            file.append(",");
            file.append(fDate.substring(0,10));
            file.append(",");
            file.append(fDate.substring(11,19));
            file.append(",");
            file.append(String.valueOf(punto.getLatitud()));
            file.append(",");
            file.append(String.valueOf(punto.getLongitud()));
            file.append(",");
            file.append(punto.getMac());
            file.append(",");
            file.append(punto.getUser());
            file.append(",");
            file.append(String.valueOf(getVelocidad()));
            file.append(",");
            file.append(punto.getObservations());
            file.append(",");
            file.append(String.valueOf(punto.getPm()));
            file.append("\n");
            file.flush();
            file.close();
            Log.i("File found","DataBase edited");
        } catch (FileNotFoundException e) {
            Log.i("File not found",e.toString());
        } catch (IOException e) {
            Log.i("Error",e.toString());
        }
    }

    private double getVelocidad(){
        if((myLastLocation != null) && (myLastLocation != getLastLocation)) {
            getLastLocation = myLastLocation;
            double r = 6378100;
            double v_lat = toRadians(myLocation.getLatitude()-myLastLocation.getLatitude());
            double v_lon = toRadians( myLocation.getLongitude()- myLastLocation.getLongitude());
            double a  = sin(v_lat/2)*sin(v_lat/2) + cos(toRadians(myLocation.getLatitude())) * cos(toRadians(myLastLocation.getLatitude())) *sin(v_lon/2)*sin(v_lon/2);
            double c= 2 * Math.atan2(sqrt(a), sqrt(1-a));
            double distancia = r * c;
            double velocidad = distancia / ((time - time_prev) * 0.001);

            return velocidad ;
        }else{
            return 0;}

    }

}
