package com.example.osmdroid.Extras.Debugger;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.example.osmdroid.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static java.lang.Math.sqrt;

public class ObservacionesActivity extends AppCompatActivity implements  SensorEventListener {


    private int CONEXION_WIFI=1;
    private int CONEXION_DATOS_MOVILES=2;
    private int SIN_CONEXION=0;

    private int N_PM=1;

    //sensor
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private double aceleracion=0;
    private int cont_ac=0;

    private int contador_locs = 0;
    private RecyclerView recycler;
    private DatabaseReference dbReference;

    private boolean adapter_Flag=false;


    private Calendar calendar; //Para recoger fecha y hora
    private SimpleDateFormat df;
    private String formattedDate;

    private String dateMaps="Sin datos al escribir";
    private TextView numberPM;
    private TextView numberSpeed;


    private long count;

    private String filePath="";

    private String MAC_ADDRESS="";
    private Context mContext;

    //fecha
    private Calendar myCalendar;
    private String today;
    private SimpleDateFormat sdf;

    //progress bar
    ProgressBar myProgressBar;

    //observaciones
    private CheckBox humos, olores, trafico, semaforos, viento, lluvia, vegetacion, cruce, obras;
    private EditText otros;
    private String observaciones = "";
    private Button btn_otros;
    private Button btn_end;

    private final int OBSERVATIONS_ACTIVITY = 4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observaciones);

        mContext= ObservacionesActivity.this;

        //Progress bar
        myProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        numberPM=(TextView)findViewById(R.id.number_pm);
        numberSpeed=(TextView)findViewById(R.id.speed);

        //formato velocidad
        DecimalFormat precision = new DecimalFormat("0.00");

        //declacion de checkbox
        humos=(CheckBox)findViewById(R.id.humos);
        olores=(CheckBox)findViewById(R.id.olor);
        trafico=(CheckBox)findViewById(R.id.trafico);
        semaforos=(CheckBox)findViewById(R.id.semaforo);
        viento=(CheckBox)findViewById(R.id.viento);
        lluvia=(CheckBox)findViewById(R.id.lluvia);
        vegetacion=(CheckBox)findViewById(R.id.vegetacion);
        cruce=(CheckBox)findViewById(R.id.cruce);
        obras=(CheckBox)findViewById(R.id.obras);
        otros=(EditText)findViewById(R.id.otros);

        btn_otros=(Button)findViewById(R.id.btn_otros);
        btn_end=(Button)findViewById(R.id.btn_end);


        //Listeners
        btn_otros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                observaciones = observaciones + otros.getText();
                otros.setText("");
            }
        });
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkObservaciones();
                Intent intent = new Intent();
                intent.putExtra("OBS", observaciones);
                setResult(OBSERVATIONS_ACTIVITY, intent);
                finish();

            }
        });

        //Sensor
        /*mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);*/



        //Recibo el intent al crear la actividad
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            Bundle b = intent.getExtras();
            int pm = b.getInt("PMData");
            double vel = b.getInt("Speed");
            myProgressBar.setProgress(pm);
            numberPM.setText(String.valueOf(pm));
            numberSpeed.setText(precision.format(vel));
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }
    @Override
    protected void onResume(){
        super.onResume();
       // mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



    private void checkObservaciones(){

        if (trafico.isChecked()) {
            observaciones = observaciones + "trafico ";
        }
        if (semaforos.isChecked()) {
            observaciones = observaciones + "semaforos ";
        }
        if (obras.isChecked()) {
            observaciones = observaciones + "obras ";
        }
        if (humos.isChecked()) {
            observaciones = observaciones + "humos ";
        }
        if (olores.isChecked()) {
            observaciones = observaciones + "olores ";
        }
        if (cruce.isChecked()) {
            observaciones = observaciones + "cruce ";
        }
        if (vegetacion.isChecked()) {
            observaciones = observaciones + "vegetacion ";
        }
        if (lluvia.isChecked()) {
            observaciones = observaciones + "lluvia ";
        }
        if (viento.isChecked()) {
            observaciones = observaciones + "viento ";
        }

        trafico.setChecked(false);
        semaforos.setChecked(false);
        obras.setChecked(false);
        humos.setChecked(false);
        olores.setChecked(false);
        cruce.setChecked(false);
        vegetacion.setChecked(false);
        lluvia.setChecked(false);
        viento.setChecked(false);

            /*    String otras_obs = otros.getText().toString();
                if(otras_obs!="Otros"){
                    observaciones = observaciones + otras_obs;
                    otros.setText("Otros");
                }
                */
    }


    //SENSORES
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (cont_ac == 0) {
            aceleracion = sqrt(event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2]);
            cont_ac++;
        }else{
            aceleracion = (aceleracion + sqrt(event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2]))/(cont_ac+1);
            cont_ac++;

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}

