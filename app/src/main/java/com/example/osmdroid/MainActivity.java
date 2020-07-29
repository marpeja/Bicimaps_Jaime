package com.example.osmdroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.osmdroid.Bluetooth.BluetoothActivity;
import com.example.osmdroid.Datos.AutoSuggestAdapter;
import com.example.osmdroid.Modelo.GeocoderNominatimMod;
import com.example.osmdroid.Modelo.Punto;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
//import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity implements MapEventsReceiver,
                                                                LocationListener,
                                                                SensorEventListener,
                                                                MapView.OnFirstLayoutListener,
                                                                NavigationView.OnNavigationItemSelectedListener{
    //////////////
    //ACTIVITY
    //////////////

    //CONTROL
    static final String userAgent = "com.example.osmdroid";
    private Handler handler;
    private enum Mode{EMPTY, VIEW, ROUTES, NAVIGATION};
    private enum HandlerInstruction{
        UPDATE_DISTANCE (1),
        APPROACH_NODE (2),
        REACH_NODE (3),
        RECALCULATE (4),
        REROUTE (5),
        HEATMAP (6),
        UPDATE_TIME (7),
        REACH_END (8);

        private int value;

        private HandlerInstruction (int value){
            this.value = value;
        }

        public int getValue(){
            return value;
        }
    }
    private Mode mode;
    private int able = 0;
    private DecimalFormat formato;
    private boolean search = false; //Para no escribir dos veces en historico la busqueda (al crear ruta y al buscar)
                                    //ya que ya se añade al buscar.
    private boolean first_location = true;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean lost = false;
    private final int PERFIL_ACTIVITY = 33;
    private final int ERROR_RESULT = 77;

    private boolean firstFocus = true; //La primera vez que pulsamos sobre origin detecta ENFOQUE, luego ya detecta clickado
                                       //A veces cuando se cambia de usuario, detecta ENFOQUE, y despliega el historial
                                       //para evitarlo, hacemos que unicamente se despliegue con el primer enfoque.


    //INTERFACE
    private TextView userEmailView;
    private TextView userNameView;
    private DrawerLayout drawer;
    private AutoCompleteTextView origin;
    private AutoCompleteTextView destination;
    private ImageButton clear_origin;
    private ImageButton clear_destination;
    private Button swap;
    private TextView road1;
    private TextView road2;
    private AutoSuggestAdapter<String> adapterStreets;
    private ArrayAdapter<String> adapterHistory;
    private FloatingActionButton modeChanger;
    private FloatingActionButton myLocationBtn;
    private FloatingActionButton bluetoothSelection;
    private TextView.OnEditorActionListener editorListener;
    private TextView instructions;
    private TextView distance;
    private TextView time;
    private ImageView signal;
    private String[] favPlacesAddresses;
    private String[] historyPlacesAddresses;
    private String[] favPlacesNames;

    //PERMISSIONS
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    //////////////
    //MAP
    //////////////

    //MAP AND CUSTOM
    private MapView map;
    private IMapController mapController;
    private Bitmap icon;
    private NavigationView navigationView;

    //OVERLAYS
    private MapEventsOverlay mEventsOverlay;
    private MyLocationNewOverlay myLocationNewOverlay;
    private FolderOverlay contaminationLayer;
    private FolderOverlay markers;

    //LOCATION
    private LocationManager locationManager;
    private GpsMyLocationProvider myLocationProvider;
    private GeoPoint myLocation;
    private GeoPoint myLastLocation;

    //HEATMAP
    private ArrayList<Punto> points;
    private ArrayList<Punto> greens;
    private ArrayList<Punto> yellows;
    private ArrayList<Punto> oranges;
    private ArrayList<Punto> reds;
    private Drawable green;
    private Drawable yellow;
    private Drawable orange;
    private Drawable red;
    private final String[] profile = {"ligero", "moderado", "conservador"};
    private int profileNumber = 0;

    //ROADS
    public static Road[] mRoads;
    protected Polyline[] mRoadOverlays;
    private ArrayList<GeoPoint> waypoints;
    private Marker originMarker;
    private boolean originMarkerON = false;
    private Marker destinationMarker;
    private boolean destinationMarkerON = false;
    protected final int START_INDEX=2, DEST_INDEX=3;
    private int selectedRoad;
    private UpdateInstructions instructionsThread;
    private double mTime;

    /////////////
    //FIREBASE
    /////////////
    DatabaseReference mRootReference;
    private FirebaseDatabase database;
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private static int RC_SIGN_IN = 44;
    private FirebaseUser user;
    private String userEmail;
    private String userName;
    private final int timeUpdate = 7200000; //Dos horas

    //////////////////
    //CONNECTIVITY
    //////////////////
    private ConnectivityManager networkManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private AlertDialog networkDialog;


    //////////////////
    //BLUETOOTH
    //////////////////
    private char start_PM='p';
    private char read_PM = 'r';
    private char finish_PM='s';
    private String MAC="";
    private final int BLUETOOTH_ACTIVITY = 66;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here

        requestPermissionsIfNecessary(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);

        /////////////
        //TOOLBAR
        /////////////
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toogle);
        toogle.syncState();

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        //////////////////
        //MAP AND LAYERS
        //////////////////
        //Creamos mapa. Habilitamos multiTouch, creamos un controlador del mapa y fijamos un zoom de inicio.
        map = findViewById(R.id.map);
        map.setUseDataConnection(true);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(14);

        //Capa con los puntos de contaminacion. La ponemos la primera, para que este por debajo siempre. Si la
        //pusieramos por encima de la de eventos, detectaria pulsación en los circulos y no en el propio mapa
        //por lo que no se ejcutaría onLongPress, y sí el onClick asociado al circulo si hubiera alguno
        contaminationLayer = new FolderOverlay();


        //Se mostrará la capa de contaminación en caso de que el usuario tenga marcada su visualización (la cual
        //está marcada por defecto). Si la desactiva, entonces ya no se mostrará hasta que el usuario lo decida.
        SharedPreferences app = getSharedPreferences("app", Context.MODE_PRIVATE);
        boolean isHeatMapEnabled = app.getBoolean("contaminationMap", true);
        if(isHeatMapEnabled) {
            map.getOverlays().add(0, contaminationLayer);
            if(!navigationView.getMenu().findItem(R.id.contaminacionmap).isChecked()){
                navigationView.getMenu().findItem(R.id.contaminacionmap).setChecked(true);
            }
        } else {
            if(navigationView.getMenu().findItem(R.id.contaminacionmap).isChecked()){
                navigationView.getMenu().findItem(R.id.contaminacionmap).setChecked(false);
            }

        }

        //Capa de rotación, para poder rotar el mapa,
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlayManager().add(mRotationGestureOverlay);

        //Capa de eventos sobre el mapa, para detectar pulsaciones,...
        mEventsOverlay = new MapEventsOverlay(this);
        map.getOverlays().add(mEventsOverlay);

        //Capa de Marcadores en el mapa. Contendrá el marcador de origen y destino.
        markers = new FolderOverlay();
        map.getOverlays().add(markers);


        //Capa de ubicación del usuario. La última en añadirse, para que siempre sea visible nuestra ubicación.
        //La localización se obtiene usando las herramientas de OSMDroid, pero tambien se podría usar FusedLocation de Google
        //cuyo código esta comentado. En caso de que el nuevo programador prefiera esta opción, descomentar dicho código y comentar
        //el de osmdroid.
        ////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////
        /*locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i("FUSED_LOCATION", "RECIBO");
                if (locationResult == null) {
                    return;
                }
                Log.i("FUSED_LOCATION", locationResult.getLastLocation()+"");
                mapController.setCenter(new GeoPoint(locationResult.getLastLocation()));
                for (Location location : locationResult.getLocations()) {
                    //Log.i("FUSED_LOCATION", location+"");
                    // Update UI with location data
                    // ...
                }
            }
        };*/
        ////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            /////////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////
            /*fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            LocationRequest locationRequest = new LocationRequest();

            locationRequest.setInterval(6000);// 3s, es como si estuviera dividido por 2
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper *///);
            ////////////////////////////////////////
            ////////////////////////////////////////
            myLocationProvider = new GpsMyLocationProvider(this);
            myLocationNewOverlay = new MyLocationNewOverlay(myLocationProvider, map);
            myLocationNewOverlay.enableMyLocation();
            //myLocationNewOverlay.enableFollowLocation();
            map.getOverlays().add( myLocationNewOverlay);
            myLastLocation = new GeoPoint(0.0,0.0);
            ////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////
            /*fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.i("FUSED_LOCATION_1", location + "");
                                // Logic to handle location object
                            }
                        }
                    });*/
            ////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////
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
                    MainActivity.this.getResources(), R.drawable.ic_bike);
            myLocationNewOverlay.setDirectionArrow(icon, icon);
            myLocationNewOverlay.setPersonHotspot(24.0f * map.getContext().getResources().getDisplayMetrics().density + 0.5f,39.0f * map.getContext().getResources().getDisplayMetrics().density + 0.5f);
        }

        //////////////
        //INTERFAZ
        //////////////

        mode = Mode.EMPTY; //Modo de la aplicación del que se parte

        userEmailView = navigationView.getHeaderView(0).findViewById(R.id.userEmail); //Correo y nombre de usuario que se verá
        userNameView = navigationView.getHeaderView(0).findViewById(R.id.userName);   //en el menú lateral.

        origin = findViewById(R.id.origin);
        destination = findViewById(R.id.destination);
        clear_origin = findViewById(R.id.clear_origin);
        clear_destination = findViewById(R.id.clear_destination);
        swap = findViewById(R.id.swap);
        signal = findViewById(R.id.signal);
        instructions = findViewById(R.id.instructions);
        distance = findViewById(R.id.distance);
        time = findViewById(R.id.time);
        modeChanger = findViewById(R.id.modeChanger);
        myLocationBtn = findViewById(R.id.myLocation);
        bluetoothSelection = findViewById(R.id.btSelector);
        road1 = findViewById(R.id.road1);
        road2 = findViewById(R.id.road2);

        clear_origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                origin.setText("");
            }
        });

        clear_destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destination.setText("");
            }
        });

        //Inicialización de adaptadores AutoSuggestTextView
        String[] streets = getResources().getStringArray(R.array.madrid_streets);
        adapterStreets = new AutoSuggestAdapter<String>(this, R.layout.autocompletestreets_layout, R.id.autoCompleteStreets, new LinkedList<String>(Arrays.asList(streets)));
        String[] newAddresses = updatePlaces(new String[]{"Tu ubicación"});
        adapterHistory = new ArrayAdapter<>(this, R.layout.autocompletehistory_layout, R.id.autoCompleteHistory, newAddresses);


        origin.setAdapter(adapterHistory);
        destination.setAdapter(adapterStreets);


        modeChanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mode){
                    case VIEW:
                        if(origin.getText().toString() != null && myLocationNewOverlay.getMyLocation() != null && destinationMarker.getSnippet() != null) {
                            modeChanger.setImageResource(R.drawable.ic_directions);
                            mode = Mode.ROUTES;

                            if (!search)
                                updateHistory(destinationMarker.getSnippet());

                            destination.setVisibility(View.VISIBLE);
                            destination.setText(origin.getText().toString());
                            origin.setText("Tu ubicación");
                            road1.setVisibility(VISIBLE);
                            road2.setVisibility(VISIBLE);
                            swap.setVisibility(VISIBLE);
                            getRoadAsync(myLocationNewOverlay.getMyLocation(), destinationMarker.getPosition());
                        } else {
                            Toast.makeText(MainActivity.this, "TERMINANDO DE CONFIGURAR, ESPERE UN MOMENTO", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case ROUTES:
                        if(mRoadOverlays != null){
                            if(MAC != null && !MAC.isEmpty()) {
                                Log.i("ENTROOOOOOOOOOOO", "1");
                                Intent newIntent = new Intent(MainActivity.this,BluetoothService.class);
                                newIntent.putExtra("PM", start_PM);
                                startService(newIntent);
                            } /*else {
                            Toast.makeText(MainActivity.this, "Conéctese al sensor via Bluetooth, por favor", Toast.LENGTH_SHORT).show();
                            break;
                        }*/
                            mode = Mode.NAVIGATION;

                            getSupportActionBar().hide();
                            modeChanger.setVisibility(View.INVISIBLE);
                            myLocationBtn.setVisibility(View.INVISIBLE);
                            bluetoothSelection.setVisibility(View.INVISIBLE);

                            myLocationNewOverlay.enableFollowLocation();
                            if(map.getOverlays().contains(contaminationLayer)) {
                                map.getOverlays().remove(contaminationLayer);
                            }
                            mEventsOverlay.setEnabled(false);

                            for(int i = 0; i < mRoadOverlays.length; i++){
                                if(mRoadOverlays[i] != null) {
                                    mRoadOverlays[i].setInfoWindow(null);
                                    mRoadOverlays[i].setOnClickListener(null);
                                    if (i != selectedRoad) {
                                        mRoadOverlays[i].setVisible(false);
                                    }
                                }
                            }

                            mapController.animateTo(myLocationNewOverlay.getMyLocation(), (double)20, (long)5);


                            signal.setVisibility(View.VISIBLE);
                            instructions.setVisibility(View.VISIBLE);
                            distance.setVisibility(View.VISIBLE);
                            time.setVisibility(VISIBLE);

                            instructions.setText(mRoads[selectedRoad].mNodes.get(1).mInstructions);
                            signal.setImageResource(signalSelection(mRoads[selectedRoad].mNodes.get(1).mManeuverType));
                            distance.setText(distanceText(mRoads[selectedRoad].mNodes.get(0).mLength));
                            mTime = mRoads[selectedRoad].mDuration;
                            time.setText(getRoadTime(mTime));

                            Log.i("DURACION_RUTA", Math.round(mTime)+"");

                            instructionsThread = new UpdateInstructions();
                            instructionsThread.start();
                        } else {
                            Toast.makeText(MainActivity.this, "Calculando rutas. Espere por favor.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                }
            }
        });

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

        bluetoothSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivityForResult(intent, BLUETOOTH_ACTIVITY);
                /*Intent intent = new Intent(MainActivity.this, UpdateFirebase.class);
                intent.putExtra("user", userEmail);
                startActivity(intent);
                for(int i = 0; i< map.getOverlays().size(); i++){
                    Log.i("CAAAAAPAAAAS", i+" "+map.getOverlays().get(i));
                }*/
            }
        });

        configureListeners(origin);
        configureListeners(destination);

        origin.setInputType(EditorInfo.TYPE_TEXT_VARIATION_FILTER);
        origin.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        destination.setInputType(EditorInfo.TYPE_TEXT_VARIATION_FILTER);
        destination.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        editorListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Oculta el teclado cuando se le da al enter
                if(event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }

                //Entrada de texto en origen
                if(v == origin && actionId == EditorInfo.IME_ACTION_SEARCH){
                    //Toast.makeText(MainActivity.this, "PULSANDO EN INICIO", Toast.LENGTH_SHORT).show();

                    String locationAddress = origin.getText().toString();

                    locationAddress = isFavPlace(locationAddress);//Comprueba si se ha pulsado un lugar favorito, para obtener su dirección.

                    if (locationAddress.equals("")){
                        Toast.makeText(MainActivity.this, "Por favor, introduzca una dirección", Toast.LENGTH_SHORT).show();
                        return false;
                    }


                    if(destination.getVisibility() == GONE){
                        new GeocodingTask().execute(locationAddress, DEST_INDEX, map.getBoundingBox());
                        search = true;
                    } else if (destination.getVisibility() == View.VISIBLE){
                        if(!locationAddress.equals("Tu ubicación")) {
                            new GeocodingTask().execute(locationAddress, START_INDEX, map.getBoundingBox());
                        } else {
                            if(originMarkerON){
                                markers.remove(originMarker);
                                originMarkerON = false;
                            }
                            getRoadAsync(myLocationNewOverlay.getMyLocation(), destinationMarker.getPosition());
                        }

                    }
                }

                //Entrada de texto en destino
                if(v == destination && actionId == EditorInfo.IME_ACTION_SEARCH){
                    //Toast.makeText(MainActivity.this, "PULSANDO EN FINAL", Toast.LENGTH_SHORT).show();

                    String locationAddress = destination.getText().toString();

                    locationAddress = isFavPlace(locationAddress);

                    if (locationAddress.equals("")){
                        Toast.makeText(MainActivity.this, "FINAL VACIO", Toast.LENGTH_SHORT).show();
                        return false;
                    }


                    if(!locationAddress.equals("Tu ubicación")) {
                        new GeocodingTask().execute(locationAddress, DEST_INDEX, map.getBoundingBox());
                    } else {
                        if(destinationMarkerON){
                            markers.remove(destinationMarker);
                            destinationMarkerON = false;
                        }
                        if(originMarkerON)
                            getRoadAsync(originMarker.getPosition(), myLocationNewOverlay.getMyLocation());
                    }

                }
                return false;
            }
        };

        road1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CLIIIICKES", "ROAD1");
                selectedRoad = 0;
                selectRoad(0);
            }
        });

        road2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CLIIIICKES", "ROAD2");
                selectedRoad = 1;
                selectRoad(1);
            }
        });

        swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                if(origin.getText().toString().equals("Tu ubicación")){
                    originMarker.setPosition(myLocation);
                    Log.i("COMPROBACION_SWAP", "ENTRO");
                }

                if(destination.getText().toString().equals("Tu ubicación")){
                    destinationMarker.setPosition(myLocation);
                }
                //Cambiamos textos.
                String text_aux = origin.getText().toString();
                origin.setText(destination.getText().toString());
                destination.setText(text_aux);

                //Cambiamos posiciones de los marcadores.
                GeoPoint point_aux = originMarker.getPosition();
                originMarker.setPosition(destinationMarker.getPosition());
                destinationMarker.setPosition(point_aux);

                Log.i("COMPROBACION_SWAP", myLocation +" "+ destinationMarker.getPosition());
                Log.i("COMPROBACION_SWAP", destinationMarker.getPosition().getLatitude()+ " "+destinationMarker.getPosition().getLongitude());
                if(!originMarkerON) {
                    markers.add(originMarker);
                    originMarkerON = true;
                }
                if(originMarkerON && originMarker.getPosition().getLatitude() == myLocation.getLatitude() &&
                        originMarker.getPosition().getLongitude() == myLocation.getLongitude()){
                    markers.remove(originMarker);
                    originMarkerON = false;
                }
                if(!destinationMarkerON) {
                    markers.add(destinationMarker);
                    destinationMarkerON = true;
                }
                if (destinationMarkerON && destinationMarker.getPosition().getLatitude() == myLocation.getLatitude() &&
                        destinationMarker.getPosition().getLongitude() == myLocation.getLongitude()){
                    markers.remove(destinationMarker);
                    destinationMarkerON = false;
                }

                getRoadAsync(originMarker.getPosition(), destinationMarker.getPosition());
            }
        });

        origin.setOnEditorActionListener(editorListener);
        destination.setOnEditorActionListener(editorListener);


        destinationMarker = new Marker(map);
        destinationMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_location, null));
        destinationMarker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);

        originMarker = new Marker(map);
        originMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_location, null));
        originMarker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);

        formato = new DecimalFormat("#.##");



        //////////////
        //HANDLER
        //////////////
        //Handler de mensajes de MainActivity.
        //Permitirá actualizar la interfaz desde los distintos hilos.
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if(msg.arg1 == HandlerInstruction.UPDATE_DISTANCE.getValue()){
                    Log.i("MAPAAAS_HANDLER", "DURACION");
                    distance.setText(updateDistance(distance.getText().toString(), msg.arg2));
                }

                if(msg.arg1 == HandlerInstruction.UPDATE_TIME.getValue()){
                    Log.i("MAPAAAS_HANDLER", "TIEMPO PRE " + mTime);
                    //mTime = mTime - (double)msg.obj;
                    //int time = (int) Math.round(mTime);
                    if(msg.arg2 == 60) {
                        mTime = mTime - 1;
                        Log.i("MAPAAAS_HANDLER", "TIEMPO - 1min");
                    } else {
                        mTime = (mTime*60 - msg.arg2)/60;
                        Log.i("MAPAAAS_HANDLER", "TIEMPO - no 1 min");
                    }
                    time.setText(getRoadTime(mTime));
                    Log.i("MAPAAAS_HANDLER", "TIEMPO POST " + mTime);
                }

                if(msg.arg1 == HandlerInstruction.APPROACH_NODE.getValue()){
                    Log.i("MAPAAAS_HANDLER", "APROXIMACION");

                    distance.setText("");
                }

                if(msg.arg1 == HandlerInstruction.REACH_NODE.getValue()){
                    Log.i("MAPAAAS_HANDLER", "LLEGADA");
                    instructions.setText(mRoads[selectedRoad].mNodes.get(msg.arg2 + 2).mInstructions);
                    signal.setImageResource(signalSelection(mRoads[selectedRoad].mNodes.get(msg.arg2 + 2).mManeuverType));
                    distance.setText(distanceText(mRoads[selectedRoad].mNodes.get(msg.arg2 + 1).mLength));
                }

                if(msg.arg1 == HandlerInstruction.REACH_END.getValue()){
                    Toast.makeText(MainActivity.this, "HA LLEGADO A SU DESTINO", Toast.LENGTH_SHORT).show();
                    distance.setText("0 m");
                    time.setText("0 min");
                }

                if(msg.arg1 == HandlerInstruction.RECALCULATE.getValue()){
                    Toast.makeText(MainActivity.this, "RECALCULANDO RECORRIDO "+ instructionsThread.isAlive(), Toast.LENGTH_SHORT).show();
                    lost = true;
                    getRoadAsync(myLocation, destinationMarker.getPosition());
                }

                if(msg.arg1 == HandlerInstruction.REROUTE.getValue()){
                    Log.i("MAPAAAS_HANDLER", "REROUTE InstructionsThread"+ instructionsThread.isAlive());
                    instructions.setText(mRoads[selectedRoad].mNodes.get(1).mInstructions);
                    signal.setImageResource(signalSelection(mRoads[selectedRoad].mNodes.get(1).mManeuverType));
                    distance.setText(distanceText(mRoads[selectedRoad].mNodes.get(0).mLength));
                    mTime = mRoads[selectedRoad].mDuration;
                    time.setText(getRoadTime(mTime));

                    instructionsThread = new UpdateInstructions();
                    instructionsThread.start();
                }

                if(msg.arg1 == HandlerInstruction.HEATMAP.getValue()){
                    int size = contaminationLayer.getItems().size();
                    for(int i = size - 1; i > -1; i--){
                        contaminationLayer.remove(contaminationLayer.getItems().get(i));
                    }
                    for(Punto v : greens){
                        Marker marker = new Marker(map);
                        marker.setOnMarkerClickListener(null);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        marker.setPanToView(false);
                        marker.setInfoWindow(null);
                        marker.setIcon(green);
                        marker.setPosition(new GeoPoint(v.getLatitud(), v.getLongitud()));
                        marker.setAlpha((float)0.4);
                        contaminationLayer.add(marker);
                    }
                    for(Punto a : yellows){
                        Marker marker = new Marker(map);
                        marker.setOnMarkerClickListener(null);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        marker.setPanToView(false);
                        marker.setInfoWindow(null);
                        marker.setIcon(yellow);
                        marker.setPosition(new GeoPoint(a.getLatitud(), a.getLongitud()));
                        marker.setAlpha((float)0.4);
                        contaminationLayer.add(marker);
                    }
                    for(Punto n : oranges){
                        Marker marker = new Marker(map);
                        marker.setOnMarkerClickListener(null);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        marker.setPanToView(false);
                        marker.setInfoWindow(null);
                        marker.setIcon(orange);
                        marker.setPosition(new GeoPoint(n.getLatitud(), n.getLongitud()));
                        marker.setAlpha((float)0.4);
                        contaminationLayer.add(marker);
                    }
                    for(Punto r : reds){
                        Marker marker = new Marker(map);
                        marker.setOnMarkerClickListener(null);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        marker.setPanToView(false);
                        marker.setInfoWindow(null);
                        marker.setIcon(red);
                        marker.setPosition(new GeoPoint(r.getLatitud(), r.getLongitud()));
                        marker.setAlpha((float)0.4);
                        contaminationLayer.add(marker);
                    }
                    Log.i("MAPAAAS_HANDLER", "TAMAÑO_CONTAMINACION " + contaminationLayer.getItems().size());
                    /*for(int i=0; i< map.getOverlays().size(); i++){
                        Log.i("MAPAAAS", map.getOverlays().get(i).toString());
                    }*/
                    map.invalidate();

                }

                return false;
            }
        });



        ////////////
        //HEATMAP
        ////////////

        points = new ArrayList<>();
        greens = new ArrayList<>();
        yellows = new ArrayList<>();
        oranges = new ArrayList<>();
        reds = new ArrayList<>();

        green = ContextCompat.getDrawable(this, R.drawable.green_circle);
        yellow = ContextCompat.getDrawable(this, R.drawable.yellow_circle);
        orange = ContextCompat.getDrawable(this, R.drawable.orange_circle);
        red = ContextCompat.getDrawable(this, R.drawable.red_circle);

        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        profileNumber = Integer.parseInt(config.getString("profile", "0"));

        //////////////
        //DATABASE
        //////////////
        database = FirebaseDatabase.getInstance();
        mRootReference = database.getReference();
       // mRootReference.child("Prueba").addValueEventListener(new ValueEventListener() {
        mRootReference.child("Contaminacion").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FireBaseThread thread = new FireBaseThread(dataSnapshot);
                thread.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //////////////////////
        //USUARIO
        //////////////////////

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() == null){
            showSignInOptions();
        } else {
            updateFirebaseUser(auth.getCurrentUser());
        }

        //////////////////////
        //INTERNET
        //////////////////////

        networkManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkManager.getActiveNetwork();
        if( networkManager.getActiveNetwork() == null){
            createAlertNetworkDialog();
        }
        networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onLost(@NonNull Network network){
                createAlertNetworkDialog();
            }

            @Override
            public void onAvailable(@NonNull Network network){
                if(networkDialog != null){
                    networkDialog.dismiss();
                }
            }
        };

        networkManager.registerDefaultNetworkCallback(networkCallback);


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("PM_Data"));


        //Actualizar todos los cambios aplicados al mapa
        map.invalidate();

    }

    ////////////////////////////////
    //CICLO DE ACTIVIDAD Y EVENTOS
    ////////////////////////////////

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
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                //on API15 AVDs,network provider fails. no idea why
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            } catch (Exception ex){}
        }

        //Actualizar el histórico de lugares, por si se han añadido o modificado los lugares favoritos.
        String[] newAddresses = updatePlaces(new String[]{"Tu ubicación"});
        for(int i = 0; i < newAddresses.length; i++){
            Log.i("NEW_ADDRESSES", newAddresses[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.autocompletehistory_layout, R.id.autoCompleteHistory, newAddresses);
        adapterHistory = adapter;

        //Actualizar el perfil de contmaninación por si este ha sido cambiado
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        profileNumber = Integer.parseInt(config.getString("profile", "0"));
        Log.i("PROOOFIIILEEE", profileNumber+"");

        //Actualizar usuario si este se ha cambiado.
        FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
        if(user != newUser || !userName.equals(newUser.getDisplayName())) {
            updateFirebaseUser(newUser);
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

        //Almacenar el histórico de lugares.
        StringBuilder sb = new StringBuilder();
        int favPlacesSize;
        if(favPlacesNames == null){
            favPlacesSize = 0;
        } else {
            favPlacesSize = favPlacesNames.length;
        }
        for (int i = 1 + favPlacesSize; i < adapterHistory.getCount(); i++) {
            sb.append(adapterHistory.getItem(i)).append("_");
        }
        SharedPreferences app = getSharedPreferences("app",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = app.edit();
        editor.putString("history", sb.toString());
        editor.apply();
        Log.i("GUARDANDO_HISTORIAL", sb.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        //Para que si damos a "atrás" con el mapa lateral abierto, se cierre el mapa y no se salga de la app
        if( drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            switch(mode){
                case VIEW:
                    mode = Mode.EMPTY;
                    if(destinationMarkerON){
                        markers.remove(destinationMarker);
                        destinationMarkerON = false;
                    }
                    modeChanger.setVisibility(View.INVISIBLE);
                    mapController.setCenter(myLocationNewOverlay.getMyLocation());
                    mapController.setZoom(14);
                    map.setMapOrientation(0.0f);
                    origin.setText("");
                    break;
                case ROUTES:
                    mode = Mode.EMPTY;
                    modeChanger.setImageResource(R.drawable.ic_distance);
                    modeChanger.setVisibility(View.INVISIBLE);
                    destination.setVisibility(GONE);
                    clear_destination.setVisibility(GONE);
                    clear_origin.setVisibility(GONE);
                    road1.setVisibility(GONE);
                    road2.setVisibility(GONE);
                    swap.setVisibility(GONE);

                    if (mRoadOverlays != null) {
                        for(Polyline road : mRoadOverlays)
                            map.getOverlays().remove(road);
                        mRoadOverlays = null;
                    }
                    if (destinationMarkerON || originMarkerON){
                        if(destinationMarkerON){
                            markers.remove(destinationMarker);
                            destinationMarkerON = false;
                        }
                        if(originMarkerON){
                            map.getOverlays().remove(originMarker);
                            markers.remove(originMarker);
                            originMarkerON = false;
                        }
                    }

                    mapController.setCenter(myLocationNewOverlay.getMyLocation());
                    mapController.setZoom(14);
                    map.setMapOrientation(0.0f);
                    origin.setText("");
                    break;

                case NAVIGATION:


                    Intent newIntent = new Intent(MainActivity.this,BluetoothService.class);
                    newIntent.putExtra("PM", finish_PM);
                    startService(newIntent);

                    //Solo si el usuario tiene seleccionado que se muestre el heatmap, lo mostramos, si no, no.
                    SharedPreferences app = getSharedPreferences("app", Context.MODE_PRIVATE);
                    boolean isHeatMapEnabled = app.getBoolean("contaminationMap", false);
                    if(isHeatMapEnabled) {
                        map.getOverlays().add(0, contaminationLayer);
                    }

                    mode = Mode.ROUTES;
                    getSupportActionBar().show();
                    modeChanger.setVisibility(View.VISIBLE);
                    myLocationBtn.setVisibility(VISIBLE);
                    bluetoothSelection.setVisibility(VISIBLE);
                    signal.setVisibility(GONE);
                    instructions.setVisibility(GONE);
                    distance.setVisibility(GONE);
                    time.setVisibility(GONE);

                    mEventsOverlay.setEnabled(true);
                    mapController.setZoom(14);
                    myLocationNewOverlay.disableFollowLocation();
                    map.setMapOrientation(0.0f);
                    //modeChanger.setImageResource(R.drawable.ic_directions);

                    if(instructionsThread.isAlive())
                        instructionsThread.end();

                    getRoadAsync(myLocationNewOverlay.getMyLocation(), destinationMarker.getPosition());
                    break;

                default:
                    super.onBackPressed();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            Log.i("PERMISOOOOs", permissions[i]+"");
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                permissionsToRequest.add(permissions[i]);
            } else {
                Toast.makeText(this, "ACEPTADO", Toast.LENGTH_SHORT).show();
                if(permissions[i].contains("ACCESS_FINE_LOCATION")){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    myLocationProvider = new GpsMyLocationProvider(this);
                    myLocationNewOverlay = new MyLocationNewOverlay(myLocationProvider, map);
                    myLocationNewOverlay.enableMyLocation();
                    //myLocationNewOverlay.enableFollowLocation();
                    map.getOverlays().add( myLocationNewOverlay);
                    myLastLocation = new GeoPoint(0.0,0.0);
                    if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                        myLocation = new GeoPoint(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                        mapController.setCenter(myLocation);
                    } else {
                        myLocation = new GeoPoint(40.416724,-3.703493); //En caso de que no se conozca última ubicación,
                        mapController.setCenter(myLocation);                                //se fija esta en el centro de Madrid
                    }

                    //Cambiamos el icono de ubicación

                    icon = BitmapFactory.decodeResource(
                            MainActivity.this.getResources(), R.drawable.ic_bike);
                    myLocationNewOverlay.setDirectionArrow(icon, icon);
                    myLocationNewOverlay.setPersonHotspot(24.0f * map.getContext().getResources().getDisplayMetrics().density + 0.5f,39.0f * map.getContext().getResources().getDisplayMetrics().density + 0.5f);
                    
                }
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.i("LOCALIZACION_INICIAL", "onLocation");
        //Para centrar el mapa en la ubi del usuario al principio.
        if(first_location){
            //Log.i("LOCALIZACION_INICIAL_2", "onLocation");
            myLocation = new GeoPoint(location);
            first_location = false;
            mapController.setCenter(myLocation);
        }

        GeoPoint auxiliar = new GeoPoint(location);
        Log.i("TIEEEEEMPO", myLocation.getLatitude()+" "+myLocation.getLongitude());
        if(myLocation.getLatitude() != auxiliar.getLatitude() ||
           myLocation.getLongitude() != auxiliar.getLongitude()) {
            Log.i("TIEEEEEMPO_IN", myLocation.getLatitude()+" "+myLocation.getLongitude());
            if (myLocation != null) {
                myLastLocation = myLocation;
            }
            myLocation = new GeoPoint(location);
            //location.getSpeed();
            Log.i("TIEEEEEMPO", location.getLatitude() + " " + location.getLongitude() + " " + location.getBearing() + " " + location.getAccuracy());

            //Solo si se está en modo navegación, se orienta el mapa según la orientación del usuario y se sigue su ubicación.
            if (mode == Mode.NAVIGATION) {
                map.setMapOrientation(-location.getBearing());
                Log.i("FOLLOW_ENABLE", myLocationNewOverlay.isFollowLocationEnabled()+"");
                if(!myLocationNewOverlay.isFollowLocationEnabled())
                    myLocationNewOverlay.enableFollowLocation();
                Log.i("EEEEEO_LocChg", location.getLatitude() + " " + location.getLongitude()+ " SPEED "+location.getSpeed());
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
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        if(mode != Mode.NAVIGATION) {
            search = false;
            destinationMarker.setPosition(p);

            if(!destinationMarkerON)
                markers.add(destinationMarker);

            destinationMarkerON = true;
            map.invalidate();
            new ReverseGeocodingTask().execute(destinationMarker);
            if (modeChanger.getVisibility() == View.INVISIBLE) {
                mode = Mode.VIEW;
                modeChanger.setVisibility(View.VISIBLE);
            }
            if (mode == Mode.ROUTES) {
                if (!originMarkerON) {
                    getRoadAsync(myLocationNewOverlay.getMyLocation(), p);
                } else {
                    getRoadAsync(originMarker.getPosition(), p);
                }
            }
        }
        return true;
    }

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {

    }

    //////////////////////
    //MÉTODOS PROPIOS
    //////////////////////


    ////////////////
    //RUTAS
    ////////////////
    //////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    //Crea los JSON correspondientes para solicitar la ruta más rápida y la de menor contaminación
    //Posteriormente ejecuta el hilo que hace la consulta a la API
    public void getRoadAsync(GeoPoint start, GeoPoint end){

        JSONObject postData = new JSONObject();
        JSONObject fastData = new JSONObject();
        JSONArray coordenadas = new JSONArray();
        waypoints = new ArrayList<>();
        try {
            coordenadas.put(new JSONArray().put(start.getLongitude()).put(start.getLatitude()));
            coordenadas.put(new JSONArray().put(end.getLongitude()).put(end.getLatitude()));
            postData.put("coordinates", coordenadas);
            postData.put("language", "es-es");
            fastData.put("coordinates", coordenadas);
            fastData.put("language", "es-es");
            JSONArray bloqueo = new JSONArray();
            if(reds.size() != 0){
                Log.i("ROADMAPAAAS", "LIGERO");
                for(int i=0; i< reds.size(); i++){
                    bloqueo.put(createPolygon(new GeoPoint(reds.get(i).getLatitud(), reds.get(i).getLongitud())));
                }
                if(profile[profileNumber].equals("moderado") || profile[profileNumber].equals("conservador")){
                    Log.i("ROADMAPAAAS", "MODERADO");
                    if(oranges.size() != 0){
                        for(int i=0; i< oranges.size(); i++){
                            bloqueo.put(createPolygon(new GeoPoint(oranges.get(i).getLatitud(), oranges.get(i).getLongitud())));
                        }

                    }
                }
                if(profile[profileNumber].equals("conservador")){
                    Log.i("ROADMAPAAAS", "CONSERVADOR");
                    if(yellows.size() != 0){
                        for(int i=0; i< yellows.size(); i++){
                            bloqueo.put(createPolygon(new GeoPoint(yellows.get(i).getLatitud(), yellows.get(i).getLongitud())));
                        }

                    }
                }
                JSONObject avoid_polygons = new JSONObject();
                avoid_polygons.put("type", "MultiPolygon");
                avoid_polygons.put("coordinates", bloqueo);
                JSONObject options = new JSONObject();
                options.put("avoid_polygons", avoid_polygons);
                postData.put("options", options);
            }
            waypoints.add(start);
            waypoints.add(end);

            new AskForRoutes().execute("https://api.openrouteservice.org/v2/directions/cycling-regular", fastData.toString(), postData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("MAPAAAS", "getRoadAsync");
        }
    }


    //Hilo que hace la petición a la API de las rutas.
    //La respuesta recibida será tratada en el onPostExecute para crear las Rutas en forma de objeto Road
    @SuppressLint("StaticFieldLeak")
    private class AskForRoutes extends AsyncTask<String, Void, ArrayList<String>> {

        protected ArrayList<String> doInBackground(String... params) {

            String data = "";
            String data2 = "";

            HttpURLConnection httpURLConnection = null;
            HttpURLConnection httpURLConnection2 = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestProperty("Authorization", getString(R.string.open_route_service_API));
                httpURLConnection.setRequestProperty("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setUseCaches(false);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                Log.i("MAPAAAS", params[1]);
                wr.flush();
                wr.close();

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);


                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }

                httpURLConnection2 = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection2.setRequestProperty("Authorization", getString(R.string.open_route_service_API));
                httpURLConnection2.setRequestProperty("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");
                httpURLConnection2.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                httpURLConnection2.setRequestMethod("POST");

                httpURLConnection2.setDoOutput(true);
                httpURLConnection2.setDoInput(true);
                httpURLConnection2.setUseCaches(false);

                DataOutputStream wrt = new DataOutputStream(httpURLConnection2.getOutputStream());
                wrt.writeBytes(params[2]);
                Log.i("MAPAAAS", params[2]);
                wr.flush();
                wr.close();

                InputStream inS = httpURLConnection2.getInputStream();
                InputStreamReader inputStreamReader2 = new InputStreamReader(inS);


                int inputStreamData2 = inputStreamReader2.read();
                while (inputStreamData2 != -1) {
                    char current = (char) inputStreamData2;
                    inputStreamData2 = inputStreamReader2.read();
                    data2 += current;
                }
                Log.e("ERROR_ROAD", "HOOOLAAA");

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR_ROAD", e.getMessage(), e);
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
            }

            ArrayList<String> result = new ArrayList<>();
            result.add(data);
            result.add(data2);
            Log.i("MAPAAAS_JSON_1", data2+" EO");
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            Road[] roads = new Road[2];
            Log.i("result_Length", result.size()+"");
            try {

                JSONObject consulta = new JSONObject(result.get(0));
                Log.i("MAPAAAS_JSON", consulta.toString());
                JSONArray jPaths = consulta.optJSONArray("routes");
                if (jPaths != null && jPaths.length() != 0) {
                    JSONObject jPath = jPaths.getJSONObject(0);
                    String route_geometry = jPath.getString("geometry");
                    Road road = new Road();
                    road.mRouteHigh = PolylineEncoder.decode(route_geometry, 10, false);
                    JSONObject summary = jPath.getJSONObject("summary");
                    road.mDuration = summary.getDouble("duration") / 60; //En min // + summary.getDouble("duration") % 60 * 60 / 100;
                    road.mLength = summary.getDouble("distance") / 1000.0;//En Km
                    JSONArray bbox = jPath.getJSONArray("bbox");
                    road.mBoundingBox = new BoundingBox(bbox.getDouble(3), bbox.getDouble(2), bbox.getDouble(1), bbox.getDouble(0));
                    JSONArray segments = jPath.getJSONArray("segments");
                    JSONArray steps =  (segments.getJSONObject(0)).getJSONArray("steps");
                    for(int i = 0; i < steps.length(); i++){
                        JSONObject jInstruction = steps.getJSONObject(i);
                        RoadNode node = new RoadNode();
                        JSONArray jInterval = jInstruction.getJSONArray("way_points");
                        int positionIndex = jInterval.getInt(0);
                        node.mLocation = (GeoPoint)road.mRouteHigh.get(positionIndex);
                        node.mLength = jInstruction.getDouble("distance");//En m
                        node.mDuration = (double)jInstruction.getInt("duration");//En sec// 1000.0D;
                        //int direction = jInstruction.getInt("type");
                        node.mManeuverType = jInstruction.getInt("type");
                       // node.mManeuverType = this.getManeuverCode(direction);
                        node.mInstructions = jInstruction.getString("instruction");
                        road.mNodes.add(node);
                        Log.i("NODES_LENGTH_0: ", node.mLength+"");

                    }
                    road.buildLegs(waypoints);
                    road.mStatus = 0;
                    roads[0] = road;
                    setRoadTime(0, road);
                }

                if(!result.get(1).equals("")) {
                    JSONObject consulta2 = new JSONObject(result.get(1));
                    Log.i("MAPAAAS_JSON_1", result.get(1) + "");
                    Log.i("MAPAAAS_JSON", consulta2.toString());
                    JSONArray jPaths2 = consulta2.optJSONArray("routes");
                    if (jPaths2 != null && jPaths2.length() != 0) {
                        JSONObject jPath2 = jPaths2.getJSONObject(0);
                        String route_geometry = jPath2.getString("geometry");
                        Road road = new Road();
                        road.mRouteHigh = PolylineEncoder.decode(route_geometry, 10, false);
                        JSONObject summary = jPath2.getJSONObject("summary");
                        road.mDuration = summary.getDouble("duration") / 60.0;//En min //+ summary.getDouble("duration") % 60 * 60 / 100;
                        road.mLength = summary.getDouble("distance") / 1000.0;//En Km
                        JSONArray bbox = jPath2.getJSONArray("bbox");
                        road.mBoundingBox = new BoundingBox(bbox.getDouble(3), bbox.getDouble(2), bbox.getDouble(1), bbox.getDouble(0));
                        JSONArray segments = jPath2.getJSONArray("segments");
                        JSONArray steps = (segments.getJSONObject(0)).getJSONArray("steps");
                        for (int i = 0; i < steps.length(); i++) {
                            JSONObject jInstruction = steps.getJSONObject(i);
                            RoadNode node = new RoadNode();
                            JSONArray jInterval = jInstruction.getJSONArray("way_points");
                            int positionIndex = jInterval.getInt(0);
                            node.mLocation = (GeoPoint) road.mRouteHigh.get(positionIndex);
                            node.mLength = jInstruction.getDouble("distance");//En m
                            node.mDuration = (double) jInstruction.getInt("duration");//En sec // 1000.0D;
                            //int direction = jInstruction.getInt("type");
                            node.mManeuverType = jInstruction.getInt("type");
                            //node.mManeuverType = this.getManeuverCode(direction);
                            node.mInstructions = jInstruction.getString("instruction");
                            road.mNodes.add(node);
                            Log.i("NODES_LENGTH_1: ", node.mLength + "");

                        }
                        road.buildLegs(waypoints);
                        road.mStatus = 0;
                        roads[1] = road;
                        setRoadTime(1, road);
                    }
                } else{
                    roads[1] = null;
                    setRoadTime(2, null);
                }
                MainActivity.mRoads = roads;
                Log.i("MAPAAAS", "N roads "+ MainActivity.mRoads.length);
            } catch (Exception e) {
                Log.e("ERROR_ROAD", e.getMessage(), e);
                Log.i("MAPAAAS", "postExecute");
            }

            updateUIWithRoads(mRoads);
        }
    }

    public String getRoadTime(double roadDuration){
        String duration="";
        if (roadDuration >= 60) {
            if (roadDuration % 60 == 0) {
                duration = roadDuration / 60 + "h";
            } else {
                duration = (int) (roadDuration / 60) + "h " + Math.round(roadDuration % 60) + "min";
            }
        } else {
            if (roadDuration < 1) {
                duration = "1min";
            } else {
                duration = Math.round(roadDuration) + "min";
            }
        }
        return duration;
    }

    public void setRoadTime(int i, Road road){
        String duration;
        if(road != null) {
            duration = getRoadTime(road.mDuration);
            switch (i) {
                case 0:
                    road1.setText(duration);
                    Log.i("DURACIOOON", duration);
                    break;
                case 1:
                    road2.setText(duration);
                    Log.i("DURACIOOON", duration);
                    if(!road2.isClickable()){
                        road2.setClickable(true);
                    }

                    break;
                default:
            }
        } else {
            road2.setText("Imposible");
            road2.setClickable(false);

        }
    }

    //Método que recibe las 2 rutas obtenidas en UpdateRouteTask, para pintarlas en el mapa en forma de Polylines
    //De primeras queda preseleccionada la ruta más rápida, en lugar de la de menos contaminación
    @SuppressLint("DefaultLocale")
    void updateUIWithRoads(Road[] roads){
        List<Overlay> mapOverlays = map.getOverlays();
        if (mRoadOverlays != null){
            for (int i=0; i<mRoadOverlays.length; i++)
                mapOverlays.remove(mRoadOverlays[i]);
            mRoadOverlays = null;
        }
        if (roads == null)
            return;
        if (roads[0].mStatus == Road.STATUS_TECHNICAL_ISSUE)
            Toast.makeText(map.getContext(), "Technical issue when getting the route", Toast.LENGTH_SHORT).show();
        else if (roads[0].mStatus > Road.STATUS_TECHNICAL_ISSUE) //functional issues
            Toast.makeText(map.getContext(), "No possible route here", Toast.LENGTH_SHORT).show();
        mRoadOverlays = new Polyline[roads.length];
        for (int i=0; i<roads.length; i++) {
            if(roads[i] != null) {
                Polyline roadPolyline = RoadManager.buildRoadOverlay(roads[i]);
                mRoadOverlays[i] = roadPolyline;
                roadPolyline.setTitle(getString(R.string.route) + " " + i + " - " + String.format("%.2f", roads[i].mLength) + " km " + String.format("%.2f", roads[i].mDuration) + " min");
                roadPolyline.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
                roadPolyline.setRelatedObject(i);
                roadPolyline.setWidth((float) 8.0);
                Log.i("MAPAAAS", roadPolyline.getWidth() + "");
                roadPolyline.setOnClickListener(new RoadOnClickListener());
                try {
                    Log.i("NUMERO_CAPAS", map.getOverlays().size()+" "+map.getOverlays().get(0));
                    mapOverlays.add(3, roadPolyline);
                } catch (Exception e) {
                    Log.i("MAPAAAS", "ERRRROOOOOORRR");
                    Log.e("ERROR", e.getMessage(), e);
                }
                //we insert the road overlays at the "bottom", just above the MapEventsOverlay,
                //to avoid covering the other overlays.
            }
        }
        if(!lost){
            selectedRoad = 0;
            selectRoad(0);
        } else {
            selectRoad(selectedRoad);
            lost = false;
            Message msg = Message.obtain();
            msg.arg1 = HandlerInstruction.REROUTE.getValue();
            handler.sendMessage(msg);
        }
    }

    //Listener de las rutas. Cuando pulsamos en una, se cambiará su color (indicando que es la seleccionada),
    //y se abre una ventana donde se muestra la distancia y el tiempo de la misma.
    class RoadOnClickListener implements Polyline.OnClickListener{
        @Override public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos){
            int selectedRoad = (Integer)polyline.getRelatedObject();
            MainActivity.this.selectedRoad = selectedRoad;
            selectRoad(selectedRoad);
            polyline.setInfoWindowLocation(eventPos);
            polyline.showInfoWindow();
            return true;
        }
    }

    //Método que cambia el color de la ruta en función de si es, o no, la seleccionada
    void selectRoad(int roadIndex){
        for (int i=0; i<mRoadOverlays.length; i++){
            if(mRoadOverlays[i] != null) {
                Paint p = mRoadOverlays[i].getPaint();
                if (i == roadIndex) {
                    Log.i("EEEEEEEEEEOOOOOO", ""+mRoadOverlays[i]);
                    p.setColor(0x800000FF); //blue
                    if(!lost) {
                        setViewOn(mRoads[i].mBoundingBox);
                    }
                    for (int j = 0; j < mRoads[i].mNodes.size(); j++) {
                        Log.i("MAPAAAAs", "nodo " + j + " " + mRoads[i].mNodes.get(j).mInstructions + " " + mRoads[i].mNodes.get(j).mManeuverType);
                    }
                } else {
                    p.setColor(0x90666666); //grey
                }
            }
        }
        switch (roadIndex){
            case 0:
                road1.setBackgroundTintList(getColorStateList(R.color.colorButton));
                road2.setBackgroundTintList(getColorStateList(R.color.colorPrimary));
                break;
            case 1:
                road2.setBackgroundTintList(getColorStateList(R.color.colorButton));
                road1.setBackgroundTintList(getColorStateList(R.color.colorPrimary));
                break;
            default:

        }
        map.invalidate();
    }

    //Método que crea la zona a evitar en torno a un punto de alta contaminación. Será llamado tantas veces
    //como puntos de alta contaminación haya.
    protected JSONArray createPolygon(GeoPoint p){
        try {
            JSONArray bloqueo = new JSONArray();
            JSONArray array = new JSONArray();
            array.put(new JSONArray().put(p.getLongitude() - 0.00004).put(p.getLatitude() + 0.00004));
            array.put(new JSONArray().put(p.getLongitude() + 0.00004).put(p.getLatitude() + 0.00004));
            array.put(new JSONArray().put(p.getLongitude() + 0.00004).put(p.getLatitude() - 0.00004));
            array.put(new JSONArray().put(p.getLongitude() - 0.00004).put(p.getLatitude() - 0.00004));
            array.put(new JSONArray().put(p.getLongitude() - 0.00004).put(p.getLatitude() + 0.00004));
            bloqueo.put(array);

            return bloqueo;
        }catch(Exception e){
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    //Centra la vista del mapa en el BoundingBox pasado como parámtetro. Será llamado al seleccionar un punto al que ir
    //para que la cámara del map englobe tanto a la posición inicial como a la final. Ese Boundingbox pasado será
    //tratado para que la ruta se sitúe en las 3/4 partes inferiores del mapa y así que no interfiera con el toolbar
    void setViewOn(BoundingBox bb){
        if (bb != null){
            // bb.increaseByScale((float)10);
            double lat1 = bb.getLatNorth();
            double lat2 = bb.getLatSouth();
            lat1 = lat1 - lat2;
            BoundingBox bb_aux = new BoundingBox();
            bb_aux.set(bb.getLatNorth()+lat1, bb.getLonEast(), bb.getLatSouth(), bb.getLonWest());
            map.zoomToBoundingBox(bb_aux, true, 300);
            map.setMapOrientation(0.0f);
            //startPlace.setText("Tu ubicacion");
            //endPlace.setText(destinationMarker.getSnippet());

        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////


    //////////////
    //HEATMAP
    //////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    //Hilo que consulta la base de datos para recoger los nuevos puntos de contamincación. Enviará un mensaje a la
    //actividad (tratado por el handler) para que actualice la vista del map.
    public class FireBaseThread extends Thread {

        DataSnapshot dataSnapshot;

        public FireBaseThread(DataSnapshot dataSnapshot){
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        public void run(){
            points.clear();
            greens.clear();
            yellows.clear();
            oranges.clear();
            reds.clear();
            Log.i("TAMAÑO_COLORES", greens.size()+" "+yellows.size()+" "+oranges.size()+" "+reds.size());
            long time_ms = System.currentTimeMillis();
            long hours_before = time_ms - timeUpdate;
            for(DataSnapshot postSnap:dataSnapshot.getChildren()){
                //if(Long.parseLong(postSnap.getKey()) > hours_before) {
                    Punto a = postSnap.getValue(Punto.class);
                    a.setTime(postSnap.getKey());
                    //Log.i("TIME_PUNTO", a.getTime());
                    points.add(a);
                //}
            }
            discardPoints(points);
        }

    }

    public void discardPoints(ArrayList<Punto> puntos){

        for(int i = 0; i < puntos.size(); i++){
            Punto punto = puntos.get(i);
            BoundingBox bbox = createBoundingBox(punto);
            boolean result = true;
            for(int j = i+1; j < puntos.size() && result; j++){
                Punto punto2 = puntos.get(j);
                //if(Long.parseLong(punto2.getTime()) > Long.parseLong(punto.getTime())){
                    if(isInsideBoundingBox(punto2, bbox)){
                        if(Long.parseLong(punto2.getTime()) > Long.parseLong(punto.getTime()) + 120000) {
                            result = false;
                        }
                    }
                //}
            }
            if(result){
                if (punto.getPm() <= 15) {
                    greens.add(punto);
                } else if (punto.getPm() <= 25) {
                    yellows.add(punto);
                } else if (punto.getPm() <= 35) {
                    oranges.add(punto);
                } else {
                    reds.add(punto);
                }
            }
        }

        Message message = Message.obtain();
        message.arg1 = HandlerInstruction.HEATMAP.getValue();
        handler.sendMessage(message);
    }

    public BoundingBox createBoundingBox(Punto punto){
        return new BoundingBox(punto.getLatitud()+0.00004, punto.getLongitud()+0.00004, punto.getLatitud()-0.00004, punto.getLongitud()-0.00004);
    }

    public boolean isInsideBoundingBox(Punto punto, BoundingBox bbox){
        boolean result = false;

        if(punto.getLatitud() <= bbox.getLatNorth() && punto.getLatitud() >= bbox.getLatSouth()
            && punto.getLongitud() <= bbox.getLonEast() && punto.getLongitud() >= bbox.getLonWest()){
            result = true;
        }

        return result;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////
    //GEOCODING
    ////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    //REVERSE GEOCODING

    //Método ejecutado en el hilo ReverseGeocodingTask para solicitar la dirección del punto geográfico
    //pasado como parámetro.
    public String getAddress(GeoPoint p){
        GeocoderNominatimMod geocoder = new GeocoderNominatimMod(BuildConfig.APPLICATION_ID);
        //GeocoderGraphHopper geocoder = new GeocoderGraphHopper(Locale.getDefault(), graphHopperApiKey);
        String theAddress;
        try {
            double dLatitude = p.getLatitude();
            double dLongitude = p.getLongitude();
            Log.e("MAPAAAS", dLatitude +" "+ dLongitude);
            List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
            StringBuilder sb = new StringBuilder();
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                int n = address.getMaxAddressLineIndex();
                Log.i("REVERSE_GEO", n+" "+ address);
                for (int i=0; i<=n; i++) {
                    if (i != 1) {
                        if (i != 0)
                            sb.append(", ");
                        sb.append(address.getAddressLine(i));
                        if (i == 0 && address.getPhone() != null) {
                            sb.append(" ");
                            sb.append(address.getPhone());
                        }
                    }
                    Log.i("REVERSE_GEO", sb.toString());
                }
                theAddress = sb.toString();
            } else {
                theAddress = null;
            }
        } catch (IOException e) {
            theAddress = null;
            Log.e("MAPAAAS", e.getMessage(), e);
        }
        if (theAddress != null) {
            return theAddress;
        } else {
            return "";
        }
    }

    //Hilo que ejecuta la llamada a la Api para oportuna para conseguir la dirección a partir del punto geográfico
    //donde se ubica el marker pasado. Posteriormente se añade esa dirección al marker como un Snippet.
    private class ReverseGeocodingTask extends AsyncTask<Marker, Void, String> {
        Marker marker;
        protected String doInBackground(Marker... params) {
            marker = params[0];
            return getAddress(marker.getPosition());
        }
        protected void onPostExecute(String result) {
            Log.i("MAPAAASS", result);
            if(mode == Mode.ROUTES) {
                updateHistory(result);
            }
            marker.setSnippet(result);
            if(destination.getVisibility() == GONE) {
                origin.setText(marker.getSnippet(), false);
            } else if (destination.getVisibility() == View.VISIBLE){
                destination.setText(marker.getSnippet(), false);
            }
        }
    }


    //DIRECT GEOCODING

    //Hilo que ejecuta la llamada a la Api oportuna para conseguir el punto geográfico a partir de la dirección enviada. Luego, en función
    //de si era la dirección de origen o destino, colocará el marker que toque en dicho punto geográfico.
    private class GeocodingTask extends AsyncTask<Object, Void, List<Address>> {
        int mIndex;
        protected List<Address> doInBackground(Object... params) {
            String locationAddress = (String)params[0];
            Log.i("LOCATION_SEND", locationAddress);
            mIndex = (Integer)params[1];
            GeocoderNominatimMod geocoder = new GeocoderNominatimMod(BuildConfig.APPLICATION_ID);
            geocoder.setOptions(true); //ask for enclosing polygon (if any)
            //GeocoderGraphHopper geocoder = new GeocoderGraphHopper(Locale.getDefault(), graphHopperApiKey);
            try {
                BoundingBox viewbox = (BoundingBox) params[2];
                List<Address> foundAdresses = geocoder.getFromLocationName(locationAddress, 1,
                        viewbox.getLatSouth(), viewbox.getLonEast(),
                        viewbox.getLatNorth(), viewbox.getLonWest(), false);
                return foundAdresses;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(List<Address> foundAdresses) {
            if (foundAdresses == null) {
                Toast.makeText(getApplicationContext(), "Geocoding error", Toast.LENGTH_SHORT).show();
            } else if (foundAdresses.size() == 0) { //if no address found, display an error
                Toast.makeText(getApplicationContext(), "Address not found.", Toast.LENGTH_SHORT).show();
            } else {
                Address address = foundAdresses.get(0); //get first address
                String addressDisplayName = address.getExtras().getString("display_name");
                Log.i("GEOOOOOCOOODING", address.getThoroughfare()+", "+address.getLocality()+", "+address.getCountryName()+", "+ address.getPhone());
                if(address.getPhone() != null){
                    updateHistory(address.getThoroughfare()+ " "+ address.getPhone()+", "+address.getLocality()+", "+address.getCountryName());

                } else {
                    updateHistory(address.getThoroughfare() + ", " + address.getLocality() + ", " + address.getCountryName());
                }
                //int index = map.getOverlays().indexOf(markers);
                if (mIndex == START_INDEX){

                    originMarker.setPosition(new GeoPoint(address.getLatitude(), address.getLongitude()));
                    //if(markers.getItems().size() == 1){
                    if(!originMarkerON)
                        markers.add(originMarker);
                    //}
                    originMarkerON = true;
                    originMarker.setSnippet(addressDisplayName);
                    map.getController().setCenter(originMarker.getPosition());
                    map.invalidate();

                } else if (mIndex == DEST_INDEX){

                    destinationMarker.setPosition(new GeoPoint(address.getLatitude(), address.getLongitude()));
                    //if(markers.getItems().size() == 0){
                    if(!destinationMarkerON)
                        markers.add(destinationMarker);
                    //}
                    destinationMarkerON = true;
                    destinationMarker.setSnippet(addressDisplayName);
                    map.getController().setCenter(destinationMarker.getPosition());
                    //Toast.makeText(MainActivity.this, "PONIENDO DESTINO", Toast.LENGTH_SHORT).show();
                    map.invalidate();
                }

                if(mode == Mode.EMPTY){
                    mode = Mode.VIEW;
                    modeChanger.setVisibility(View.VISIBLE);
                }

                if(mode == Mode.ROUTES) {
                    if (originMarkerON) {
                        Log.i("GEOCODING_MARKERS", "con origen");
                        getRoadAsync(originMarker.getPosition(), destinationMarker.getPosition());
                    } else {
                        Log.i("GEOCODING_MARKERS", "sin origen");
                        getRoadAsync(myLocationNewOverlay.getMyLocation(), destinationMarker.getPosition());
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////
    //NAVEGACIÓN
    /////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Hilo que se encarga de ir actualizando la información mostrada al usuario durante el modo navegación.
    private class UpdateInstructions extends Thread{
        GeoPoint lastLoc;
        RoadNode nodo;
        boolean approach;
        boolean key = false;
        UpdateTime timeThread;
        double distance_myLoc_node;
        int tenth_deviation = 0;
        int hundread_deviation = 0;

        public void end(){
            key = false;
        }

        @Override
        public void run(){
            Log.i("CAMBIOS", "DIST 0 ");
            key = true;
            if(key) {
                //timeThread = new UpdateTime();
                //timeThread.start();
                for (int i = 0; i < mRoads[selectedRoad].mNodes.size()-1; i++) {
                    timeThread = new UpdateTime();
                    timeThread.start();
                    if(key) {
                        int j = i+1;
                        approach = false;
                        if(myLocation == null) {
                            myLocation = myLocationNewOverlay.getMyLocation();
                        }
                        lastLoc = myLocation;
                        tenth_deviation = 0;
                        nodo = mRoads[selectedRoad].mNodes.get(j);
                        Log.i("CAMBIOS", "DIST 1 ");
                        while (distance(myLocation, nodo.mLocation) > 8 && key) {
                            while (distance(myLocation, nodo.mLocation) > 20 && key) {
                                lastLoc = myLocation;
                                distance_myLoc_node = distance(myLocation, nodo.mLocation);
                                Log.i("CAMBIOS", "DIST 3 " + distance(myLocation, lastLoc));
                                if(distance(myLocation, nodo.mLocation) - 100 > 100) {
                                    Log.i("CAMBIOS", "DIST 31 " + distance(myLocation, nodo.mLocation));
                                    int hundread_count = 0;
                                    hundread_deviation = 0;
                                    while(hundread_count < 10 && hundread_deviation < 2 && key ){
                                        while (distance(myLocation, lastLoc) < 10 && key) {

                                        }
                                        hundread_count++;
                                        if(distance(myLocation, nodo.mLocation) > distance_myLoc_node){
                                            hundread_deviation++;
                                        } else {
                                            hundread_deviation = 0;
                                        }
                                        distance_myLoc_node = distance(myLocation, nodo.mLocation);
                                    }
                                } else {  //if(distanceText(myLocation, nodo.mLocation) > 20){
                                    Log.i("CAMBIOS", "DIST 31 " + distance(myLocation, nodo.mLocation));
                                    while (distance(myLocation, lastLoc) < 10 && key) {

                                    }
                                    if (distance(myLocation, nodo.mLocation) > distance_myLoc_node) {
                                        tenth_deviation++;
                                    } else {
                                        tenth_deviation = 0;
                                    }
                                    distance_myLoc_node = distance(myLocation, nodo.mLocation);
                                }
                                if(hundread_deviation == 2 || tenth_deviation == 2){
                                    Log.i("SEGUNDERO_UPDATE", timeThread.getSeconds()+"");
                                    Message msg = Message.obtain();
                                    msg.arg1 = HandlerInstruction.RECALCULATE.getValue();
                                    handler.sendMessage(msg);
                                    timeThread.end();
                                    return;
                                }
                                if(key && distance(myLocation, lastLoc) < 200) {
                                    Log.i("DISTANCIA_RL", distance(myLocationNewOverlay.getMyLocation(), nodo.mLocation) + "");
                                    Log.i("DISTANCIA_REAL", distance(myLocation, nodo.mLocation) + " " + mRoads[selectedRoad].mNodes.get(i).mLength);
                                    Message msg = Message.obtain();
                                    msg.arg1 = HandlerInstruction.UPDATE_DISTANCE.getValue();
                                    msg.arg2 = (int) Math.round(distance(myLocation, nodo.mLocation));
                                    handler.sendMessage(msg);
                                }
                            }
                            if (distance(myLocation, nodo.mLocation) < 12 && !approach && key) {
                                Log.i("CAMBIOS", "DIST 6 " + distance(myLocation, nodo.mLocation));
                                approach = true;
                                Message msg2 = Message.obtain();
                                msg2.arg1 = HandlerInstruction.APPROACH_NODE.getValue();
                                handler.sendMessage(msg2);
                            }
                        }
                        if(key && i != mRoads[selectedRoad].mNodes.size()-2) {
                            Log.i("CAMBIOS", "DIST 7 " + distance(myLocation, nodo.mLocation));
                            Message msg = Message.obtain();
                            msg.arg1 = HandlerInstruction.REACH_NODE.getValue();
                            msg.arg2 = i;
                            handler.sendMessage(msg);

                            checkCorrectTime(timeThread.getMinutes(), timeThread.getSeconds(), mRoads[selectedRoad].mNodes.get(i).mDuration);
                            timeThread.end();
                        }
                    } else {
                        timeThread.end();
                    }
                    timeThread.end();
                }
                if(key) {
                    Message msg = Message.obtain();
                    msg.arg1 = HandlerInstruction.REACH_END.getValue();
                    handler.sendMessage(msg);
                }
            }
        }
    }

    //Comprueba que el tiempo tardado en llegar de un nodo a otro sea el estimado. Si se ha hecho dicho tramo más rápido,
    //se actualizará el tiempo restante de la ruta con el tiempo recortado, mientras que, si se ha tardado más, se actualizará
    //el tiempo con el retraso experimentado.
    public void checkCorrectTime(int minutes, int seconds, double duration){
        double quit1 = duration - minutes*60; //Diferencia entre lo que habría que haber quitado y lo que se ha quitado
        double quit2 = duration - (minutes*60 + seconds); //Diferencia entre lo que se debería haber tardado y lo que se ha tardado
        double quit = quit1 + quit2;
        Message msg = Message.obtain();
        msg.arg1 = HandlerInstruction.UPDATE_TIME.getValue();
        msg.arg2 = (int)quit; //Si he tardado más, "quit" será negativo y por tanto se sumará al tiempo que quedaba
        handler.sendMessage(msg);
    }

    //Hilo que se encarga de ir midiendo el tiempo que tarda el usuario en realizar el recorrido entre dos nodos consecutivos.
    private class UpdateTime extends Thread{
        private boolean key;
        private int seconds;
        private int minutes;
        private int mseconds;



        public UpdateTime(){
            minutes = 0;
            seconds = 0;
            mseconds = 0;
            key = true;
        }


        public void end(){
            key = false;
        }

        @Override
        public void run(){
            if(Math.round(mTime) > 1){
                int size = mRoads[selectedRoad].mNodes.size();
                while(mTime > 1 && distance(myLocation, mRoads[selectedRoad].mNodes.get(size - 1).mLocation) > 8 && key){
                    seconds = 0;
                    while(seconds < 60 && key){
                        mseconds = 0;
                        while(mseconds < 10 && key) {
                            try {
                                //Log.i("SEGUNDERO_UPDATE", "ESPERANDO");
                                sleep(100);
                            } catch (Exception e) {
                                Log.e("TIME_ERROR", e.getMessage(), e);
                            }
                            mseconds++;
                        }
                        Log.i("SEGUNDERO_UPDATE", myLocation+" "+myLastLocation);
                        if(myLocation != myLastLocation){
                            Log.i("SEGUNDERO_UPDATE", "DISTINTOS");
                            seconds++;

                            Log.i("SEGUNDERO_UPDATE", seconds+"");
                        }
                    }
                    if(key) {
                        minutes ++ ;
                        Message msg = Message.obtain();
                        msg.arg1 = HandlerInstruction.UPDATE_TIME.getValue();
                        msg.arg2 = 60;
                        handler.sendMessage(msg);
                    }
                }
            }
        }

        public int getSeconds () {
            return seconds;
        }

        public int getMinutes () {
            return minutes;
        }
    }

    //Asigna la señal de la instrucción a realizar en el nodo que corresponda.
    public int signalSelection (int type){
        int id;
        switch(type){
            case 0:
                id = R.drawable.ic_turn_left;
                break;
            case 1:
                id = R.drawable.ic_turn_right;
                break;
            case 2:
                id = R.drawable.ic_sharp_left;
                break;
            case 3:
                id = R.drawable.ic_sharp_right;
                break;
            case 4:
                id = R.drawable.ic_slight_left;
                break;
            case 5:
                id = R.drawable.ic_slight_right;
                break;
            case 6:
                id = R.drawable.ic_continue;
                break;
            case 7:
                id = R.drawable.ic_roundabout;
                break;
            case 10:
                id = R.drawable.ic_arrived;
                break;
            case 11:
                id = R.drawable.ic_continue;
                break;
            case 12:
                id = R.drawable.ic_slight_left;
                break;
            case 13:
                id = R.drawable.ic_slight_right;
                break;
            default:
                id = R.drawable.ic_empty;
        }
        return id;
    }

    //Fórmula de Haversine. Permite obtener la distancia entre dos puntos geográficos.
    public double distance (GeoPoint p1, GeoPoint p2){
        final double RADIO_TIERRA = 6371000;
        Log.i("DISTANCIAAAA", p1+" "+p2);
        double dLat = Math.toRadians(p1.getLatitude() - p2.getLatitude());
        double dLon = Math.toRadians(p1.getLongitude() - p2.getLongitude());
        double lat1 = Math.toRadians(p1.getLatitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)+ Math.sin(dLon/2)*Math.sin(dLon/2)*Math.cos(lat1)*Math.cos(lat2);
        double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return c*RADIO_TIERRA;
    }

    //Determina la distancia entre nodos a poner en la Navegación.
    public String distanceText(Double d){
        String result;
        if(d > 1000){
            result = d/1000.0D+" Km";
        } else {
            result = d+" m";
        }
        return result;
    }

    //Actualiza la distancia mostrada en la Navegación.
    public String updateDistance(String dString, int n){
        String result = "";
        //Double length = Double.parseDouble((dString.split(" "))[0]);
        //Toast.makeText(this, "UPDATE "+length, Toast.LENGTH_SHORT).show();
        if(dString.contains("Km")){
            if(n >= 1000){
                result = formato.format(n/1000) + " Km";
            } else {
                result = n + " m";
            }
        } else if(dString.contains("m")){
            result = n + " m";
        }
        return result;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ///////////////////////////////////////
    //AUTOCOMPLETETEXTVIEW Y LUGARES
    ///////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    //Permite añadir la dirección buscada al historial.
    public void updateHistory(String text){
        Log.i("MAPAAASS", "actualizaH");
        if(text.equals("Tu ubicación")){
            return;
        }
        
        int size = adapterHistory.getCount()+1;
        if(size > 9){
            size = 9;
        }
        String[] placesDirections = new String[size];
        String[] favPlaces = addFavPlaces(new String[]{"Tu ubicación"});
        int i;
        for(i = 0; i < favPlaces.length; i++){
            placesDirections[i] = favPlaces[i];
        }
        placesDirections[i] = text;
        i++;
        while(i < size){
            placesDirections[i] = adapterHistory.getItem(i-1);
            i++;
        }
        adapterHistory = new ArrayAdapter<String>(this, R.layout.autocompletehistory_layout, R.id.autoCompleteHistory, placesDirections);
    }


    

    //Configura todos los listeners necesarios de los AutcompleteTextView: destination y origin.
    public void configureListeners(AutoCompleteTextView autocomplete){
        autocomplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(((autocomplete == destination && autocomplete.getVisibility() == VISIBLE) || autocomplete == origin) && !autocomplete.enoughToFilter()) {
                    //Toast.makeText(MainActivity.this, "FOCUS " + hasFocus, Toast.LENGTH_SHORT).show();
                    if (hasFocus) {
                      if(firstFocus) {
                        Log.i("AUTOCOMPLETE_CONF", "ENFOCADO ");
                        origin.setAdapter(adapterHistory);
                        autocomplete.showDropDown();
                      } else {
                        firstFocus = false;
                      }
                    }
                }
            }
        });
        autocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("AUTOCOMPLETE_CONF", "BEFORE " + adapterHistory.getCount() + " ");

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if((autocomplete == destination && autocomplete.getVisibility() == VISIBLE) || autocomplete == origin) {
                    if (able > 0) {
                        Log.i("AUTOCOMPLETE_CONF", "TEXT LENGTH " + s.toString().length());
                        if (s.toString().length() < 1) {
                            if(autocomplete == origin){
                                clear_origin.setVisibility(GONE);
                            } else {
                                clear_destination.setVisibility(GONE);
                            }
                            //Toast.makeText(MainActivity.this, "ADAPTADOR 2", Toast.LENGTH_SHORT).show();
                            Log.i("AUTOCOMPLETE_CONF", "SIN BUSQ " + adapterHistory.getCount() + " ");
                            if(autocomplete.getAdapter() != adapterHistory && autocomplete.getAdapter() != adapterHistory)
                                autocomplete.setAdapter(adapterHistory);

                            if (autocomplete.getAdapter() == adapterStreets)
                                autocomplete.setAdapter(adapterHistory);
                            autocomplete.showDropDown();
                            Log.i("AUTOCOMPLETE_CONF", "Streets "+adapterStreets.getCount() + " ");
                            /*for (int i = 0; i < adapterHistory.getCount(); i++) {
                                Log.i("MAPAAASS", "SIN BUSQU " + adapterHistory.getCount() + adapterHistory.getItem(i));
                            }*/
                            //origin.showDropDown();
                        } else {
                            if(autocomplete == origin){
                                clear_origin.setVisibility(VISIBLE);
                            } else {
                                clear_destination.setVisibility(VISIBLE);
                            }

                            //Toast.makeText(MainActivity.this, "ADAPTADOR 1", Toast.LENGTH_SHORT).show();
                            boolean prueba = autocomplete == destination;
                            Log.i("AUTOCOMPLETE_CONF", "CON BUSQ " + adapterHistory.getCount() + " "+ prueba+" "+ autocomplete.getAdapter());
                            Log.i("AUTOCOMPLETE_CONF", "HISTORY " + adapterHistory);
                            Log.i("AUTOCOMPLETE_CONF", "STREETS " + adapterStreets);
                            if(autocomplete.getAdapter() != adapterHistory && autocomplete.getAdapter() != adapterHistory)
                                autocomplete.setAdapter(adapterStreets);

                            if (autocomplete.getAdapter() == adapterHistory)
                                autocomplete.setAdapter(adapterStreets);
                           // Log.i("MAPAAASS", "CON BUSQU " + adapterHistory.getCount() + " ");
                        }
                    } else {
                        able++;
                    }
                }
            }
        });
        autocomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((autocomplete == destination && autocomplete.getVisibility() == VISIBLE) || autocomplete == origin) {
                    if (!autocomplete.enoughToFilter()) {
                        Log.i("AUTOCOMPLETE_CONF", "CLICKADO");
                        autocomplete.setAdapter(adapterHistory);
                        autocomplete.showDropDown();
                    }
                }
            }
        });
    }


    public String[] addFavPlaces(String[] previous){
        String[] result;
        favPlacesNames = getFavPlacesAddresses();
        if(favPlacesNames != null){
            String[] aux = new String[favPlacesNames.length + previous.length];
            int i;
            for(i = 0; i < previous.length; i++){
                aux[i] = previous[i];
            }
            for(String name : favPlacesNames){
                Log.i("FAV_PLACES", name);
                aux[i] = name;
                i++;
            }
            result = aux;
        } else {
            result = previous;
        }
        return result;
    }

    public String[] getFavPlacesAddresses() {
        SharedPreferences app = getSharedPreferences("app", Context.MODE_PRIVATE);
        String placesDir = app.getString("placesDir", null);
        if (placesDir != null && !placesDir.equals("")) {
            favPlacesAddresses = placesDir.split("_");
            return app.getString("placesNames", null).split("_");
        } else {
            return null;
        }
    }

    public String[] addHistoryAddresses(String[] previous){
        String[] result;
        historyPlacesAddresses = getHistoryAddresses();
        if(historyPlacesAddresses != null){
            String[] aux = new String[previous.length + historyPlacesAddresses.length];
            int i;
            for(i = 0; i < previous.length; i++){
                aux[i] = previous[i];
            }
            for(String text : historyPlacesAddresses){
                aux[i] = text;
                i++;
            }
            result = aux;
        } else {
            result = previous;
        }
        return result;
    }

    public String[] getHistoryAddresses() {
        SharedPreferences app = getSharedPreferences("app", Context.MODE_PRIVATE);
        String history = app.getString("history", null);
        if (history != null &&!history.equals("")) {
            return history.split("_");
        } else {
            return null;
        }
    }

    public String[] updatePlaces(String[] previous){
        return addHistoryAddresses(addFavPlaces(previous));
    }

    public String isFavPlace (String text){
        String result = text;
        for(int i = 0; i < favPlacesNames.length; i++){
            if(favPlacesNames[i].equals(text)){
                result = favPlacesAddresses[i];
            }
        }
        return result;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////
    //MENÚ LATERAL
    ///////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.contaminacionmap){
            if(item.isChecked()){
                map.getOverlays().remove(contaminationLayer);
                SharedPreferences app = getSharedPreferences("app", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = app.edit();
                editor.putBoolean("contaminationMap", false);
                editor.commit();
                item.setChecked(false);
            } else {
                map.getOverlays().add(0, contaminationLayer);
                SharedPreferences app = getSharedPreferences("app", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = app.edit();
                editor.putBoolean("contaminationMap", true);
                editor.commit();
                item.setChecked(true);
            }
        }
        if(item.getItemId() == R.id.contamination_history){
            Intent intent = new Intent(MainActivity.this, MisRutasActivity.class);
            intent.putExtra("mode", "General");
            startActivity(intent);
        }
        if(item.getItemId() == R.id.tutorial_menu){
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            intent.putExtra("from_home", false);
            startActivity(intent);
        }

        if(item.getItemId() == R.id.mis_rutas){
            /*Intent intent = new Intent(MainActivity.this, MisRutasSelectionActivity.class);
            startActivity(intent);*/
            Intent intent = new Intent(MainActivity.this, MisRutasActivity.class);
            intent.putExtra("mode", "User");
            startActivity(intent);
        }

        if(item.getItemId() == R.id.sign_out){
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                            showSignInOptions();
                        }
                    });
        }

        if(item.getItemId() == R.id.perfil){
            Intent intent = new Intent(MainActivity.this, MiPerfilActivity.class);
            startActivityForResult(intent,PERFIL_ACTIVITY);
        }

        if(item.getItemId() == R.id.configuración){
            Intent intent = new Intent(MainActivity.this, ConfiguracionActivity.class);
            startActivity(intent);
        }

        if(item.getItemId() == R.id.fav){
            Intent intent = new Intent(MainActivity.this, MisLugaresActivity.class);
            startActivity(intent);
        }
        return false;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////
    // SIGN IN Y USUARIO
    //////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme_NoActionBar_Background)
                        .setLogo(R.drawable.ic_bicimap)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }
    public void updateFirebaseUser(FirebaseUser newUser){
        user = newUser;
        userEmail = user.getEmail();
        userName = user.getDisplayName();
        userEmailView.setText(userEmail);
        userNameView.setText(userName);
        Log.i("FIREBASE_AUTH", newUser.getDisplayName());
        Log.i("FIREBASE_AUTH", newUser.getEmail());
    }

    ///////////////////////////
    //RESULTADO DE ACTIVIDADES
    ///////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
                updateFirebaseUser(newUser);
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                finish();
            }
        }
        if(requestCode == PERFIL_ACTIVITY && resultCode == ERROR_RESULT){
            finish();
        }

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
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////
    //ALERT NETWORK DIALOG
    /////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void createAlertNetworkDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle("Sin internet")
                .setMessage("Reconéctese o pulse OK para cerrar la app")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        networkDialog = builder.show();
        networkDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                networkDialog.dismiss();
                MainActivity.this.finish();
            }
        });
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////
    //UPLOAD TO FIREBASE
    //////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            ArrayList<Integer> PMData = intent.getIntegerArrayListExtra("TestData");
            if (PMData != null) {
                /*Punto aux = new Punto();
                aux.setLatitud(myLocation.getLatitude());
                aux.setLongitud(myLocation.getLongitude());
                aux.setPm(PMData.get(0));
                aux.setMac(MAC);
                aux.setUser(user.getEmail());

                Map<String, Object> datos = new HashMap<>();
                datos.put("latitud", aux.getLatitud());
                datos.put("longitud", aux.getLongitud());
                datos.put("pm", aux.getPm());
                datos.put("mac", aux.getMac());
                datos.put("user", aux.getUser());

                long time = currentTimeMillis();
                String string_time = String.valueOf(time);
                mRootReference.child("Contaminacion").child(string_time).setValue(datos);*/
                Toast.makeText(MainActivity.this, "Dato "+PMData.get(0), Toast.LENGTH_SHORT).show();
            }

        }

    };

}